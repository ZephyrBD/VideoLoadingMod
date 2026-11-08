package top.techmczs.videoloading.datagen.languages;

import java.util.concurrent.CompletableFuture;

import net.minecraft.registry.RegistryWrapper;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

import top.techmczs.videoloading.client.StartupVideoConfig;

/**
 * 英文翻译生成器。
 */
public class ModENUSProvider extends FabricLanguageProvider {
    public ModENUSProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(StartupVideoConfig.SKIP_KEY, "Skip");
        translationBuilder.add(StartupVideoConfig.VOLUME_KEY, "Startup Video Volume");
    }
}
