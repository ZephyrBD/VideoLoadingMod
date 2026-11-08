package top.techmczs.videoloading.mixin.client;

import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 暴露 {@link OptionsSubScreen} 的选项列表，供
 * {@code SoundOptionsScreenMixin} 在子类里追加自定义选项。
 * {@code @Shadow} 不能引用父类字段，所以走 Accessor。
 */
@Mixin(OptionsSubScreen.class)
public interface GameOptionsScreenAccessor {
    @Accessor("list")
    OptionsList videoloading$getBody();
}
