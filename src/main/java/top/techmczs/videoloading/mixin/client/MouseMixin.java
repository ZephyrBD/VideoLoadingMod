/*
 * Copyright (C) 2025 MCZS(ZephyrBD and FayYu)
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package top.techmczs.videoloading.mixin.client;

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
 * <p>{@code LoadingOverlay} 只是 {@code Overlay}，不实现
 * {@code ContainerEventHandler}，且 {@code MouseHandler#onPress} 在
 * overlay 存在时会直接跳过点击转发，所以原版加载界面收不到任何
 * 鼠标输入。这里在方法入口处把点击转发给 {@code StartupVideo}，
 * 用于呼出并点击右上角的跳过按钮。点击坐标按原版公式从窗口
 * 像素坐标缩放成 GUI 坐标，与界面绘制一致。
 */
@Mixin(MouseHandler.class)
public abstract class MouseMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onPress", at = @At("HEAD"))
    private void videoloading$forwardClickToStartupVideo(long window, int button, int action, int mods,
            CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS && button == 0 && minecraft.getOverlay() != null) {
            // xpos/ypos 字段与 xpo()/ypo() 返回的都是原始窗口像素坐标，
            // 需按原版同款公式缩放成 GUI 坐标，与界面绘制一致
            var win = minecraft.getWindow();
            int guiX = (int) (((MouseHandler) (Object) this).xpos() * win.getGuiScaledWidth() / win.getWidth());
            int guiY = (int) (((MouseHandler) (Object) this).ypos() * win.getGuiScaledHeight() / win.getHeight());
            StartupVideo.onMouseClick(guiX, guiY);
        }
    }
}
