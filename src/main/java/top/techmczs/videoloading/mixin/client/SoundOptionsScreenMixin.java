package top.techmczs.videoloading.mixin.client;

import net.minecraft.client.gui.screen.option.SoundOptionsScreen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import top.techmczs.videoloading.client.StartupVideoConfig;

/**
 * 在「音乐和声音」设置末尾追加启动视频的音量滑条。
 */
@Mixin(SoundOptionsScreen.class)
@Environment(EnvType.CLIENT)
public abstract class SoundOptionsScreenMixin {
    @Inject(method = "addOptions", at = @At("TAIL"))
    private void mvm$addStartupVideoVolume(CallbackInfo ci) {
        // body 字段在父类 GameOptionsScreen 上，@Shadow 不可用，走 Accessor
        ((GameOptionsScreenAccessor) this).videoloading$getBody()
                .addSingleOptionEntry(StartupVideoConfig.volumeOption());
    }
}
