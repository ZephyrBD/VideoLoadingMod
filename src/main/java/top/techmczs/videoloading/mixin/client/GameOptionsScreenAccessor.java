package top.techmczs.videoloading.mixin.client;

import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.OptionListWidget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 暴露 {@code GameOptionsScreen} 的选项列表，供
 * {@code SoundOptionsScreenMixin} 在子类里追加自定义选项。
 * {@code @Shadow} 不能引用父类字段，所以走 Accessor。
 */
@Mixin(GameOptionsScreen.class)
@Environment(EnvType.CLIENT)
public interface GameOptionsScreenAccessor {
    @Accessor("body")
    OptionListWidget videoloading$getBody();
}
