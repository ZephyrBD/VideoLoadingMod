package top.techmczs.videoloading.mixin.client;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.client.sound.TickableSoundInstance;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import top.techmczs.videoloading.client.StartupVideo;

/**
 * 启动视频播放期间拦截所有游戏声音，避免与视频自带的音频
 * （走 javax.sound.sampled，不经过声音引擎）叠在一起。
 */
@Mixin(SoundSystem.class)
@Environment(EnvType.CLIENT)
public abstract class SoundSystemMixin {
    @Inject(method = {"play(Lnet/minecraft/client/sound/SoundInstance;)V",
            "play(Lnet/minecraft/client/sound/SoundInstance;I)V"}, at = @At("HEAD"), cancellable = true)
    private void vl$silenceDuringVideo(SoundInstance sound, CallbackInfo ci) {
        if (StartupVideo.isActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "playNextTick", at = @At("HEAD"), cancellable = true)
    private void vl$silenceTickableDuringVideo(TickableSoundInstance sound, CallbackInfo ci) {
        if (StartupVideo.isActive()) {
            ci.cancel();
        }
    }
}
