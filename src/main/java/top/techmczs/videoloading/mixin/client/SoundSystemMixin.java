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

import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import top.techmczs.videoloading.client.StartupVideo;

/**
 * 启动视频播放期间拦截所有游戏声音，避免与视频自带的音频
 * （走 javax.sound.sampled，不经过声音引擎）叠在一起。
 */
@Mixin(SoundEngine.class)
public abstract class SoundSystemMixin {
    @Inject(method = {"play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V",
            "playDelayed(Lnet/minecraft/client/resources/sounds/SoundInstance;I)V"}, at = @At("HEAD"), cancellable = true)
    private void vl$silenceDuringVideo(SoundInstance sound, CallbackInfo ci) {
        if (StartupVideo.isActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "queueTickingSound", at = @At("HEAD"), cancellable = true)
    private void vl$silenceTickableDuringVideo(TickableSoundInstance sound, CallbackInfo ci) {
        if (StartupVideo.isActive()) {
            ci.cancel();
        }
    }
}
