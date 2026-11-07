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
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import top.techmczs.videoloading.client.StartupVideo;

/**
 * 把启动界面的 Mojang logo 替换为 mod 的启动视频。
 *
 * <p>原版 {@code LoadingOverlay.render} 里 logo 通过两次
 * {@code blit}（两种配色/半区）绘制；视频播放期间第一处改为
 * 绘制视频帧，第二处直接跳过。同时挡住视频播完第一遍前的
 * {@code setOverlay(null)}，保证必须看完一遍视频才进入游戏；
 * 加载耗时超过视频时长时视频会一直循环。
 */
@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/Screen;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
    private void vl$drawLoadingHud(Screen screen, GuiGraphics context, int mouseX, int mouseY, float delta,
            Operation<Void> original) {
        // 视频播放期间不渲染原版 GenericMessageScreen（居中的全景图 + 文字），
        // 改为屏幕底部的横幅，把画面留给视频
        if (StartupVideo.isActive()) {
            StartupVideo.renderHud(context);
            return;
        }
        original.call(screen, context, mouseX, mouseY, delta);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V",
            ordinal = 0))
    private void vl$drawStartupVideo(GuiGraphics context, ResourceLocation texture,
            int x, int y, int width, int height,
            float u, float v, int regionWidth, int regionHeight,
            int textureWidth, int textureHeight, Operation<Void> original) {
        StartupVideo.begin(this.minecraft);
        if (!StartupVideo.renderFrame(context)) {
            original.call(context, texture, x, y, width, height,
                    u, v, regionWidth, regionHeight, textureWidth, textureHeight);
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V",
            ordinal = 1))
    private void vl$skipSecondLogoHalf(GuiGraphics context, ResourceLocation texture,
            int x, int y, int width, int height,
            float u, float v, int regionWidth, int regionHeight,
            int textureWidth, int textureHeight, Operation<Void> original) {
        if (!StartupVideo.isActive()) {
            original.call(context, texture, x, y, width, height,
                    u, v, regionWidth, regionHeight, textureWidth, textureHeight);
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V"))
    private void vl$holdUntilVideoFinished(Minecraft client, Overlay overlay, Operation<Void> original) {
        if (StartupVideo.holdsOverlay()) {
            return;
        }
        StartupVideo.stop();
        original.call(client, overlay);
        // 加载期间被推迟的主菜单此时才真正显示
        Screen pending = StartupVideo.takePendingScreen();
        if (pending != null) {
            client.setScreen(pending);
        }
    }
}
