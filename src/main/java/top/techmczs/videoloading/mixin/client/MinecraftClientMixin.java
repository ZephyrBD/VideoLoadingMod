package top.techmczs.videoloading.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import top.techmczs.videoloading.client.StartupVideo;

/**
 * 启动视频播放期间推迟主菜单的显示。
 *
 * <p>资源加载完成时 {@link Minecraft} 会立即把 {@code TitleScreen}
 * 设为当前界面，而此时启动视频还没播完、{@code SplashOverlay} 仍持有
 * 界面，导致主菜单文字透过淡出的加载背景叠在视频上，且输入仍被加载
 * 界面拦截、菜单点不动。这里在视频持住期间把界面暂存起来，待
 * {@code SplashOverlayMixin} 放行后再真正显示。
 */
@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void vl$deferScreenDuringStartupVideo(Screen screen, CallbackInfo ci) {
        if (screen != null && StartupVideo.holdsOverlay()) {
            StartupVideo.setPendingScreen(screen);
            ci.cancel();
        }
    }
}
