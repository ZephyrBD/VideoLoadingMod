package top.techmczs.videoloading.datagen.languages;

import java.util.concurrent.CompletableFuture;

import net.minecraft.registry.RegistryWrapper;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

import top.techmczs.videoloading.client.StartupVideoConfig;

/**
 * 简体中文翻译生成器。
 */
public class ModZHCNProvider extends FabricLanguageProvider {
    public ModZHCNProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        // FabricLanguageProvider 默认注册为 en_us，zh_cn 需显式指定语言代码
        super(dataOutput, "zh_cn", registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(StartupVideoConfig.SKIP_KEY, "跳过");
        translationBuilder.add(StartupVideoConfig.VOLUME_KEY, "启动视频音量");
    }
}
