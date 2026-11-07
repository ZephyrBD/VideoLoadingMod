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
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.server.packs.resources.ReloadInstance;

import net.neoforged.neoforge.client.loading.NeoForgeLoadingOverlay;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import top.techmczs.videoloading.client.StartupVideo;

import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

/**
 * 把 {@code NeoForgeLoadingOverlay}替换为 mod 的启动视频。
 *
 * <p>NeoForge 用 {@link ForgeLoadingOverlay}（继承并完全重写
 * {@link LoadingOverlay#render}，通过 earlydisplay 自绘进度条与 logo，
 * 由 {@code ImmediateWindowHandler.loadingOverlay} 装配）替换了原版
 * 加载界面，dev 与生产环境都是它，挂在原版类上的注入永远不会触发，
 * 所以必须接管 Forge 系的类。视频会话活跃时完全取消 NeoForge 的
 * 绘制，改画视频帧与「正在加载 Minecraft」横幅（纯文字无背景条）；
 * 资源加载完成且视频已完整播过一遍（或玩家已跳过）时放行进入主菜单。
 * 视频初始化失败时直接交还原逻辑，保留 NeoForge 进度条作回退。
 *
 * <p>注意本 mixin 目标是 Forge 系类（不参与混淆映射），相关注解须
 * {@code remap = false}。
 */
@Mixin(value = NeoForgeLoadingOverlay.class, remap = false)
public abstract class SplashOverlayMixin {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Shadow(remap = false)
    @Final
    private Minecraft minecraft;

    @Shadow(remap = false)
    @Final
    private ReloadInstance reload;

    @Shadow(remap = false)
    @Final
    private net.neoforged.fml.earlydisplay.DisplayWindow displayWindow;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private void videoloading$renderStartupVideo(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo ci) {
        // 首次渲染时初始化视频；失败则回退 NeoForge 原版加载界面
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
        // 显示加载期间被 MinecraftMixin 推迟的主菜单
        if (this.reload.isDone() && !StartupVideo.holdsOverlay()) {
            StartupVideo.stop();
            // 必须 close 掉 NeoForge 的 early display：直接 setOverlay(null) 会跳过
            // Forge 系的两阶段淡出交接，ImmediateWindowHandler 未关闭会导致
            // 渲染停在最后一帧（主菜单已就位、音乐已播但画面冻结）
            this.displayWindow.close();
            this.minecraft.setOverlay(null);
            Screen pending = StartupVideo.takePendingScreen();
            if (pending == null) {
                // 兜底：推迟机制意外丢失时也要显示主菜单，避免黑屏
                pending = new TitleScreen();
                LOGGER.info("放行时无缓存的待显示界面，兜底进入主菜单");
            }
            this.minecraft.setScreen(pending);
        }
    }
}
