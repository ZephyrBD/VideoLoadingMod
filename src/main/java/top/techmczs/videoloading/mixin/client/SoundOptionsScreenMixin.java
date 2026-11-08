package top.techmczs.videoloading.mixin.client;

import net.minecraft.client.gui.screen.option.SoundOptionsScreen;
import net.minecraft.client.gui.widget.OptionListWidget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import top.techmczs.videoloading.client.StartupVideoConfig;

/**
 * 在「音乐和声音」设置末尾追加启动视频的音量滑条。
 *
 * <p>1.20.1 的 {@code SoundOptionsScreen} 没有 1.21 的
 * {@code addOptions()} 钩子，选项列表在 {@code init()} 里
 * 自建（字段 {@code optionButtons}），所以在 {@code init()}
 * 尾部追加。
 */
@Mixin(SoundOptionsScreen.class)
@Environment(EnvType.CLIENT)
public abstract class SoundOptionsScreenMixin {
    @Shadow
    private OptionListWidget optionButtons;

    @Inject(method = "init", at = @At("TAIL"))
    private void vl$addStartupVideoVolume(CallbackInfo ci) {
        optionButtons.addSingleOptionEntry(StartupVideoConfig.volumeOption());
    }
}
