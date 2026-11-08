package top.techmczs.videoloading.mixin.client;

import net.minecraft.client.gui.screens.options.SoundOptionsScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import top.techmczs.videoloading.client.StartupVideoConfig;

/**
 * 在「音乐和声音」设置末尾追加启动视频的音量滑条。
 */
@Mixin(SoundOptionsScreen.class)
public abstract class SoundOptionsScreenMixin {
    @Inject(method = "addOptions", at = @At("TAIL"))
    private void videoloading$addStartupVideoVolume(CallbackInfo ci) {
        // list 字段在父类 OptionsSubScreen 上，@Shadow 不可用，走 Accessor
        ((OptionsSubScreenAccessor) this).videoloading$getList()
                .addBig(StartupVideoConfig.volumeOption());
    }
}
