package top.techmczs.videoloading.client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;

import com.mojang.blaze3d.platform.NativeImage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.Util;

import net.neoforged.fml.loading.FMLPaths;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import top.techmczs.videoloading.mixin.client.NativeImageAccessor;

/**
 * 启动视频播放器：在 {@code SplashOverlay}（Mojang logo 加载界面）上
 * 播放 mod 内置的启动视频，替代 Mojang logo。
 *
 * <p>架构：视频时长可能远超资源加载时间，无法把帧序列预先放进内存，
 * 因此运行时用 mod 自带的 ffmpeg.exe 把 assets 里的 video.mp4 解码成
 * 原始 RGBA 像素流（后台线程读管道），渲染线程逐帧上传纹理绘制。
 * 音频不经过 MC 的声音引擎（OpenAL 在加载界面前半段尚未就绪），
 * 而是用 {@code javax.sound.sampled} 直接播放 audio.wav。
 *
 * <p>播完策略：必须完整播完一遍才允许离开加载界面（由
 * {@code SplashOverlayMixin} 挡住 overlay 的关闭）；如果 mod 太多
 * 加载时间超过视频时长，视频和音频会循环播放直到加载结束。
 * 素材由 {@code scripts/prepare_startup_video.py} 从根目录的
 * video.mp4 / audio.mp3 生成。
 */
public final class StartupVideo {
    private static final Logger LOGGER = LoggerFactory.getLogger("VideoLoading/StartupVideo");

    private static final ResourceLocation VIDEO_ID = ResourceLocation.fromNamespaceAndPath("videoloading", "logo_video");
    private static final String RESOURCE_DIR = "/assets/videoloading/logo_video";
    private static final int FRAME_WIDTH = 960;
    private static final int FRAME_HEIGHT = 540;
    private static final int FRAME_BYTES = FRAME_WIDTH * FRAME_HEIGHT * 4;
    /** 解码帧缓冲池大小，2~3 帧即可形成对解码线程的背压 */
    private static final int POOL_SIZE = 3;
    private static final Path TEMP_DIR =
            Path.of(System.getProperty("java.io.tmpdir"), "videoloading_startup_video");

    private static boolean started;
    private static volatile boolean active;
    /** 视频是否已完整播过一遍，播完前由 mixin 挡住加载界面关闭 */
    private static volatile boolean firstPassDone;
    private static volatile boolean running;
    private static volatile boolean audioRunning;
    private static Process process;
    private static Clip clip;
    /** 音频相对游戏主音量的系数（来自设置界面滑条，默认 0.5） */
    private static volatile float volume = (float) StartupVideoConfig.getVolume();
    /** 播放音频时捕获的游戏主音量，乘以 volume 得到实际增益 */
    private static volatile float masterVolume = 1.0F;
    /** 加载完成时被推迟的主菜单，视频第一遍播完后再显示 */
    private static Screen pendingScreen;

    /** 跳过按钮：第一次点击屏幕后出现在右上角，点击按钮本身即跳过 */
    private static volatile boolean skipShown;

    private static final int SKIP_WIDTH = 60;
    private static final int SKIP_HEIGHT = 20;
    private static final int SKIP_MARGIN = 8;

    private static DynamicTexture texture;
    private static volatile boolean textureReady;

    private static final LinkedBlockingQueue<Frame> FREE = new LinkedBlockingQueue<>();
    private static final ArrayBlockingQueue<Frame> READY = new ArrayBlockingQueue<>(2);

    /** 一帧解码结果：NativeImage 由渲染线程上传，view 供解码线程直写像素 */
    private record Frame(NativeImage image, ByteBuffer view) {
    }

    private StartupVideo() {
    }

    /**
     * 首次渲染调用时初始化：解包素材到临时目录、注册纹理、
     * 启动解码线程和音频线程。只在每次 JVM 生命周期内执行一次。
     */
    public static void begin(Minecraft client) {
        if (started) {
            return;
        }
        started = true;
        try {
            Files.createDirectories(TEMP_DIR);
            Path exe = extractFfmpeg();
            Path video = extractResource("video.mp4", "video.mp4");
            Path audio = extractResource("audio.wav", "audio.wav");

            NativeImage blank = new NativeImage(NativeImage.Format.RGBA, FRAME_WIDTH, FRAME_HEIGHT, false);
            blank.fillRect(0, 0, FRAME_WIDTH, FRAME_HEIGHT, 0xFF000000);
            texture = new DynamicTexture(blank);
            client.getTextureManager().register(VIDEO_ID, texture);

            for (int i = 0; i < POOL_SIZE; i++) {
                NativeImage image = new NativeImage(NativeImage.Format.RGBA, FRAME_WIDTH, FRAME_HEIGHT, false);
                long pointer = ((NativeImageAccessor) (Object) image).videoloading$getPointer();
                FREE.offer(new Frame(image, MemoryUtil.memByteBuffer(pointer, FRAME_BYTES)));
            }

            Runtime.getRuntime().addShutdownHook(new Thread(StartupVideo::stop,
                    "VideoLoading-StartupVideoShutdown"));

            active = true;
            running = true;
            masterVolume = client.options.getSoundSourceOptionInstance(SoundSource.MASTER).get().floatValue();
            applyVolume();
            Thread decoder = new Thread(() -> decodeLoop(exe, video), "VideoLoading-StartupVideoDecoder");
            decoder.setDaemon(true);
            decoder.start();
            playAudio(audio);
            LOGGER.info("Startup video started (60s source, looping while the game loads)");
        } catch (Exception e) {
            LOGGER.warn("Startup video init failed, keeping the vanilla Mojang logo", e);
            active = false;
            running = false;
        }
    }

    public static boolean isActive() {
        return active;
    }

    /** 视频还没完整播完第一遍时，mixin 应挡住加载界面关闭。 */
    public static boolean holdsOverlay() {
        return active && !firstPassDone;
    }

    /** 加载完成时暂存被推迟的界面（主菜单），播完后释放。 */
    public static void setPendingScreen(Screen screen) {
        pendingScreen = screen;
    }

    /** 取走被推迟的界面；没有则返回 null。 */
    public static Screen takePendingScreen() {
        Screen screen = pendingScreen;
        pendingScreen = null;
        return screen;
    }

    /** 加载界面关闭时由 mixin 调用，终止解码与音频。 */
    public static void stop() {
        if (!started) {
            return;
        }
        running = false;
        active = false;
        audioRunning = false;
        Process p = process;
        if (p != null) {
            p.destroy();
        }
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }

    /**
     * 绘制当前解码帧（黑底居中、等比适配窗口）。
     * 返回 true 表示视频会话仍然活跃，{@code SplashOverlay} 不应绘制原版 logo。
     */
    public static boolean renderFrame(GuiGraphics graphics) {
        if (!active) {
            return false;
        }        // 取出最新解码帧（丢弃积压的旧帧），全部归还缓冲池
        Frame latest = null;
        Frame frame;
        while ((frame = READY.poll()) != null) {
            if (latest != null) {
                FREE.offer(latest);
            }
            latest = frame;
        }
        if (latest != null) {
            // 拷贝到纹理自有 image 后上传；不能用 setPixels，它会 close 池里的 image
            texture.getPixels().copyFrom(latest.image());
            texture.upload();
            textureReady = true;
            FREE.offer(latest);
        }
        int windowWidth = graphics.guiWidth();
        int windowHeight = graphics.guiHeight();
        graphics.fill(0, 0, windowWidth, windowHeight, 0xFF000000);
        if (!textureReady) {
            return true;
        }
        float scale = Math.min((float) windowWidth / FRAME_WIDTH, (float) windowHeight / FRAME_HEIGHT);
        int drawWidth = Math.round(FRAME_WIDTH * scale);
        int drawHeight = Math.round(FRAME_HEIGHT * scale);
        // 11 参重载：把整帧纹理区域缩放到 drawWidth×drawHeight；
        // 9 参重载是纹理像素 1:1 映射（不缩放），会只显示画面左上角
        graphics.blit(VIDEO_ID, (windowWidth - drawWidth) / 2, (windowHeight - drawHeight) / 2,
                drawWidth, drawHeight, 0.0F, 0.0F, FRAME_WIDTH, FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT);
        return true;
    }

    /**
     * 视频播放期间替代原版 {@code GenericMessageScreen} 的 HUD：
     * 屏幕底部一条半透明横幅，居中「正在加载 Minecraft」，
     * 以及（玩家点击屏幕后）右上角的跳过按钮。
     */
    // ── 早期翻译 ──
    // 横幅渲染在资源包加载完成之前，I18n 尚无语言表可查（生产环境会显示原始
    // key），这里直接从 jar 内的 lang JSON 读取，语言码回退 options.txt
    private static final Map<String, String> EARLY_LANG = loadEarlyLang();

    private static Map<String, String> loadEarlyLang() {
        Map<String, String> out = new HashMap<>();
        String code = null;
        try {
            code = Minecraft.getInstance().getLanguageManager().getSelected();
        } catch (Throwable ignored) {
            // 加载界面阶段语言管理器可能尚未就绪
        }
        if (code == null || code.isEmpty()) {
            try {
                Path options = FMLPaths.GAMEDIR.get().resolve("options.txt");
                if (Files.exists(options)) {
                    for (String line : Files.readAllLines(options)) {
                        if (line.startsWith("lang:")) {
                            code = line.substring(5).trim();
                            break;
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        for (String candidate : new String[]{code, "en_us"}) {
            if (candidate == null || candidate.isEmpty() || !out.isEmpty()) {
                continue;
            }
            try (InputStream in = StartupVideo.class.getResourceAsStream(
                    "/assets/videoloading/lang/" + candidate + ".json")) {
                if (in == null) {
                    continue;
                }
                JsonObject json = JsonParser.parseReader(
                        new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                json.entrySet().forEach(e -> out.put(e.getKey(), e.getValue().getAsString()));
            } catch (Throwable ignored) {
            }
        }
        out.putIfAbsent("gui.loadingMinecraft", "Loading Minecraft");
        out.putIfAbsent(StartupVideoConfig.SKIP_KEY, "Skip");
        return out;
    }

    /** 加载界面阶段的翻译：不走 I18n（此时语言表未装载）。 */
    private static String earlyTr(String key) {
        String s = EARLY_LANG.get(key);
        return s != null ? s : key;
    }

    public static void renderHud(GuiGraphics graphics) {
        Minecraft client = Minecraft.getInstance();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        graphics.drawCenteredString(client.font,
                Component.literal(earlyTr("gui.loadingMinecraft")), width / 2, height - 26, 0xFFFFFFFF);

        if (skipShown) {
            int skipX = width - SKIP_WIDTH - SKIP_MARGIN;
            graphics.fill(skipX, SKIP_MARGIN, skipX + SKIP_WIDTH, SKIP_MARGIN + SKIP_HEIGHT, 0x66000000);
            graphics.renderOutline(skipX, SKIP_MARGIN, SKIP_WIDTH, SKIP_HEIGHT, 0xFFFFFFFF);
            graphics.drawCenteredString(client.font,
                    Component.literal(earlyTr(StartupVideoConfig.SKIP_KEY)),
                    skipX + SKIP_WIDTH / 2, SKIP_MARGIN + (SKIP_HEIGHT - 8) / 2, 0xFFFFFFFF);
        }
    }

    /** 视频期间点击（由 {@code MouseMixin} 从 {@code Mouse#onMouseButton} 转发）。 */
    public static void onMouseClick(int x, int y) {
        if (!holdsOverlay()) {
            return;
        }
        if (skipShown) {
            if (isInsideSkipButton(x, y)) {
                skip();
            }
        } else {
            skipShown = true;
            LOGGER.info("Skip button shown (x: {}, y: {})", x, y);
        }
    }

    private static boolean isInsideSkipButton(int x, int y) {
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int skipX = width - SKIP_WIDTH - SKIP_MARGIN;
        return x >= skipX && x < skipX + SKIP_WIDTH
                && y >= SKIP_MARGIN && y < SKIP_MARGIN + SKIP_HEIGHT;
    }

    /** 跳过：解除「必须看完一遍」的限制，视频停止后由原版在加载完成时放行界面。 */
    public static void skip() {
        if (!firstPassDone) {
            LOGGER.info("Startup video skipped by player, releasing the screen once loading finishes");
        }
        firstPassDone = true;
    }

    /** 重新应用音频增益：音量滑条变化或音频打开时调用。 */
    public static void applyVolume() {
        Clip c = clip;
        if (c == null || !c.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        float effective = volume * masterVolume;
        float gain = effective <= 0.0F ? -80.0F : 20.0F * (float) Math.log10(effective);
        FloatControl control = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
        control.setValue(Math.max(control.getMinimum(), Math.min(control.getMaximum(), gain)));
    }

    /** 解码线程：启动 ffmpeg 子进程读 RGBA 原始像素流，EOF 后重开进程循环播放。 */
    private static void decodeLoop(Path exe, Path video) {
        byte[] chunk = new byte[FRAME_BYTES];
        try {
            while (running) {
                int frames = 0;
                long passStart = Util.getMillis();
                Process p = new ProcessBuilder(exe.toString(), "-v", "error",
                                "-i", video.toString(), "-f", "rawvideo", "-pix_fmt", "rgba", "pipe:1")
                        .redirectError(ProcessBuilder.Redirect.DISCARD)
                        .start();
                process = p;
                try (InputStream in = new BufferedInputStream(p.getInputStream(), 1 << 16)) {
                    while (running && readFully(in, chunk)) {
                        frames++;
                        Frame frame = FREE.poll();
                        if (frame == null) {
                            Thread.sleep(1);
                            continue;
                        }
                        frame.view().clear();
                        frame.view().put(chunk);
                        while (running && !READY.offer(frame)) {
                            // 队列满说明渲染线程跟不上，丢最旧的一帧保持实时
                            Frame stale = READY.poll();
                            if (stale != null) {
                                FREE.offer(stale);
                            }
                        }
                        if (!running) {
                            return;
                        }
                        // 实时节拍：对齐 30fps，否则解码全速跑完，视频会被加速播放
                        long target = passStart + frames * 1000L / 30;
                        long now = Util.getMillis();
                        if (target > now) {
                            Thread.sleep(target - now);
                        }
                    }
                } finally {
                    p.destroy();
                }
                if (!running) {
                    break;
                }
                if (frames == 0) {
                    LOGGER.warn("Startup video decoding failed (no frames produced), falling back to the vanilla logo");
                    active = false;
                    break;
                }
                firstPassDone = true;
                LOGGER.info("Startup video finished its first playthrough ({} frames), releasing the loading screen", frames);
                // 视频回到开头，音频同步回卷
                if (clip != null) {
                    clip.setMicrosecondPosition(0L);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Startup video decoder thread crashed, falling back to the vanilla logo", e);
            active = false;
        }
    }

    /** 从进程输出流精确读满一帧，流结束时返回 false。 */
    private static boolean readFully(InputStream in, byte[] chunk) throws IOException, InterruptedException {
        int offset = 0;
        while (offset < chunk.length) {
            int n = in.read(chunk, offset, chunk.length - offset);
            if (n < 0) {
                return false;
            }
            offset += n;
        }
        return true;
    }

    /** 音频线程：javax.sound.sampled 直接播放 WAV，播完自动循环，绕开未就绪的 OpenAL。 */
    private static void playAudio(Path wav) {
        Thread thread = new Thread(() -> {
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(wav.toFile())) {
                clip = AudioSystem.getClip();
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP && audioRunning) {
                        clip.setMicrosecondPosition(0L);
                        clip.start();
                    }
                });
                clip.open(ais);
                applyVolume();
                clip.start();
            } catch (Exception e) {
                LOGGER.warn("Failed to play startup video audio", e);
            }
        }, "VideoLoading-StartupAudio");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 按当前系统选取并解包内置的 ffmpeg：Windows → ffmpeg-windows.exe；
     * macOS 按 CPU 架构选 arm64/x64；Linux → x64 静态版。若 jar 里没有
     * 对应平台的二进制（例如 ARM Linux），回退到系统 PATH 中的 ffmpeg；
     * 仍找不到才放弃视频。
     */
    private static Path extractFfmpeg() throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase();
        String arch = System.getProperty("os.arch", "").toLowerCase();
        String resource;
        if (os.contains("win")) {
            resource = "ffmpeg-windows.exe";
        } else if (os.contains("mac") || os.contains("darwin")) {
            resource = arch.contains("aarch64") ? "ffmpeg-macos-arm64" : "ffmpeg-macos-x64";
        } else {
            resource = arch.contains("aarch64") ? "ffmpeg-linux-arm64" : "ffmpeg-linux-x64";
        }
        try (InputStream probe = StartupVideo.class.getResourceAsStream(RESOURCE_DIR + "/" + resource)) {
            if (probe == null) {
                LOGGER.warn("No bundled ffmpeg binary for {}, falling back to ffmpeg from the system PATH", resource);
                Path system = locateSystemFfmpeg();
                if (system == null) {
                    throw new IOException("找不到可用的 ffmpeg（内置缺失且系统 PATH 无 ffmpeg）");
                }
                return system;
            }
        }
        Path extracted = extractResource(resource, resource);
        makeExecutable(extracted);
        return extracted;
    }

    /** 在系统 PATH 中查找 ffmpeg（Windows 上含 .exe 后缀）。 */
    private static Path locateSystemFfmpeg() {
        String suffix = System.getProperty("os.name", "").toLowerCase().contains("win") ? ".exe" : "";
        for (String dir : System.getProperty("PATH", "").split(File.pathSeparator)) {
            Path candidate = Path.of(dir.trim(), "ffmpeg" + suffix);
            if (Files.isExecutable(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /** macOS / Linux 上给解包出来的 ffmpeg 补执行权限（Windows 会抛异常，忽略）。 */
    private static void makeExecutable(Path file) {
        try {
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rwxr-xr-x"));
        } catch (UnsupportedOperationException | IOException e) {
            // Windows 文件系统不支持 POSIX 权限，无需处理
        }
    }

    /** 解包资源到临时目录，内容以 .size 标记比对，避免每次启动重复写 100MB。 */
    private static Path extractResource(String resource, String fileName) throws IOException {
        byte[] data;
        try (InputStream in = StartupVideo.class.getResourceAsStream(RESOURCE_DIR + "/" + resource)) {
            if (in == null) {
                throw new IOException("缺少启动视频资源 " + resource);
            }
            data = in.readAllBytes();
        }
        Path target = TEMP_DIR.resolve(fileName);
        Path marker = TEMP_DIR.resolve(fileName + ".size");
        String expected = String.valueOf(data.length);
        if (Files.isRegularFile(target) && Files.isRegularFile(marker)
                && Files.readString(marker).equals(expected)) {
            return target;
        }
        Files.write(target, data);
        Files.writeString(marker, expected);
        return target;
    }
}
