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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraftforge.client.loading.ForgeLoadingOverlay;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import top.techmczs.videoloading.client.StartupVideo;

/**
 * 把 Forge 的加载界面替换为 mod 的启动视频。
 *
 * <p>Forge 用 {@link ForgeLoadingOverlay}（继承并完全重写
 * {@link LoadingOverlay#render}，通过 earlydisplay 自绘进度条与 logo）
 * 替换了原版加载界面，且 dev 与生产环境都用它，所以必须挂在它上面。
 * 这里在 render 入口接管：视频会话活跃时完全取消 Forge 的绘制，
 * 改画视频帧与「正在加载 Minecraft」横幅；资源加载完成且视频已
 * 完整播过一遍（或玩家已跳过）时放行进入主菜单。视频初始化失败
 * 时直接交还原版逻辑，保留 Forge 进度条作回退。
 *
 * <p>注意本 mixin 目标是 Forge 类（不参与混淆映射），相关注解须
 * {@code remap = false}，否则注解处理器会因找不到 SRG 映射而报错。
 */
@Mixin(value = ForgeLoadingOverlay.class, remap = false)
public abstract class SplashOverlayMixin {
    @Shadow(remap = false)
    @Final
    private Minecraft minecraft;

    @Shadow(remap = false)
    @Final
    private ReloadInstance reload;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private void vl$renderStartupVideo(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo ci) {
        // 首次渲染时初始化视频；失败则回退 Forge 原版加载界面
        StartupVideo.begin(this.minecraft);
        if (!StartupVideo.isActive()) {
            return;
        }
        ci.cancel();

        // 视频帧 + 底部横幅；帧绘制内部若发现解码线程死亡会停用视频会话
        if (StartupVideo.renderFrame(graphics)) {
            StartupVideo.renderHud(graphics);
        } else {
            return;
        }

        // 加载完成且视频已完整播过一遍（或玩家已跳过）时放行，
        // 显示加载期间被 MinecraftClientMixin 推迟的主菜单
        if (this.reload.isDone() && !StartupVideo.holdsOverlay()) {
            StartupVideo.stop();
            this.minecraft.setOverlay(null);
            Screen pending = StartupVideo.takePendingScreen();
            if (pending != null) {
                this.minecraft.setScreen(pending);
            }
        }
    }
}
