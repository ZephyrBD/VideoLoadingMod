package top.techmczs.videoloading.mixin.client;

import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.SoundOptionsScreen;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import top.techmczs.videoloading.client.StartupVideoConfig;

/**
 * 在「音乐和声音」设置末尾追加启动视频的音量滑条。
 */
@Mixin(SoundOptionsScreen.class)
public abstract class SoundOptionsScreenMixin {
    @Shadow
    private OptionsList list;

    @Inject(method = "init", at = @At("TAIL"))
    private void vl$addStartupVideoVolume(CallbackInfo ci) {
        list.addBig(StartupVideoConfig.volumeOption());
    }
}
