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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import top.techmczs.videoloading.client.StartupVideo;

/**
 * 启动视频播放期间推迟主菜单的显示。
 *
 * <p>资源加载完成时 {@code MinecraftClient} 会立即把 {@code TitleScreen}
 * 设为当前界面，而此时启动视频还没播完、{@code SplashOverlay} 仍持有着
 * 界面，导致主菜单文字透过淡出的加载背景叠在视频上，且输入仍被加载
 * 界面拦截、菜单点不动。这里在视频持住期间把界面暂存起来，待
 * {@code SplashOverlayMixin} 放行后再真正显示。
 */
@Mixin(MinecraftClient.class)
@Environment(EnvType.CLIENT)
public abstract class MinecraftClientMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void mvm$deferScreenDuringStartupVideo(Screen screen, CallbackInfo ci) {
        if (screen != null && StartupVideo.holdsOverlay()) {
            StartupVideo.setPendingScreen(screen);
            ci.cancel();
        }
    }
}
