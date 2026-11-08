package top.techmczs.videoloading.mixin.client;

import net.minecraft.client.gui.screens.SoundOptionsScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import top.techmczs.videoloading.client.StartupVideoConfig;

/**
 * 在「音乐和声音」设置末尾追加启动视频的音量滑条。
 * 1.20.1 没有抽象的 addOptions，选项构建在 {@code init} 里完成。
 */
@Mixin(SoundOptionsScreen.class)
public abstract class SoundOptionsScreenMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void vl$addStartupVideoVolume(CallbackInfo ci) {
        ((SoundOptionsScreenAccessor) this).videoloading$getList()
                .addBig(StartupVideoConfig.volumeOption());
    }
}
