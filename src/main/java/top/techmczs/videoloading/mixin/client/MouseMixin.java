package top.techmczs.videoloading.mixin.client;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import top.techmczs.videoloading.client.StartupVideo;

/**
 * 启动视频期间捕获鼠标点击。
 *
 * <p>{@code SplashOverlay} 只是 {@code Overlay}，不实现
 * {@code GuiEventListener}，且 {@link MouseHandler#onPress} 在 overlay
 * 存在时会直接跳过点击转发，所以原版加载界面收不到任何鼠标输入。
 * 这里在方法入口处把点击转发给 {@link StartupVideo}，用于
 * 呼出并点击右上角的跳过按钮。点击坐标按原版公式从窗口像素
 * 坐标缩放成 GUI 坐标，与界面绘制一致。
 */
@Mixin(MouseHandler.class)
public abstract class MouseMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onPress", at = @At("HEAD"))
    private void vl$forwardClickToStartupVideo(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS && button == 0 && minecraft.getOverlay() != null) {
            // xpos/ypos 是原始窗口像素坐标，需按原版同款公式缩放成
            // GUI 坐标，与界面绘制一致
            Window win = minecraft.getWindow();
            int guiX = (int) (((MouseHandler) (Object) this).xpos() * win.getGuiScaledWidth() / win.getWidth());
            int guiY = (int) (((MouseHandler) (Object) this).ypos() * win.getGuiScaledHeight() / win.getHeight());
            StartupVideo.onMouseClick(guiX, guiY);
        }
    }
}
