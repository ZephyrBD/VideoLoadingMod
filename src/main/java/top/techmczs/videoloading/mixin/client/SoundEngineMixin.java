package top.techmczs.videoloading.mixin.client;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundEngine;

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
public abstract class SoundEngineMixin {
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void videoloading$silenceDuringVideo(SoundInstance sound, CallbackInfo ci) {
        if (StartupVideo.isActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "playDelayed", at = @At("HEAD"), cancellable = true)
    private void videoloading$silenceDelayedDuringVideo(SoundInstance sound, int delay, CallbackInfo ci) {
        if (StartupVideo.isActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "queueTickingSound", at = @At("HEAD"), cancellable = true)
    private void videoloading$silenceTickingDuringVideo(TickableSoundInstance sound, CallbackInfo ci) {
        if (StartupVideo.isActive()) {
            ci.cancel();
        }
    }
}
