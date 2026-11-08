package top.techmczs.videoloading.mixin.client;

import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.SoundOptionsScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 暴露 {@code SoundOptionsScreen} 的选项列表，供 Mixin 在
 * {@code init} 末尾追加自定义选项。1.20.1 里该界面自建局部
 * {@code OptionsList}（字段名 {@code list}），不像 1.21 有父类
 * 共享的 {@code layout}。
 */
@Mixin(SoundOptionsScreen.class)
public interface SoundOptionsScreenAccessor {
    @Accessor("list")
    OptionsList videoloading$getList();
}
