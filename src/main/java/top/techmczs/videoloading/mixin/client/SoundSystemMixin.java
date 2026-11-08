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
