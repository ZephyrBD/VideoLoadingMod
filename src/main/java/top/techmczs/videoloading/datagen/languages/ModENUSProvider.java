package top.techmczs.videoloading.datagen.languages;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

import top.techmczs.videoloading.client.StartupVideoConfig;

/**
 * 英文翻译生成器。
 */
public class ModENUSProvider extends FabricLanguageProvider {
    public ModENUSProvider(FabricDataOutput dataOutput) {
        super(dataOutput, "en_us");
    }

    @Override
    public void generateTranslations(TranslationBuilder translationBuilder) {
        translationBuilder.add(StartupVideoConfig.SKIP_KEY, "Skip");
        translationBuilder.add(StartupVideoConfig.VOLUME_KEY, "Startup Video Volume");
        // 1.20.1 原版语言文件没有该 key，横幅文案需 mod 自带
        translationBuilder.add("gui.loadingMinecraft", "Loading Minecraft");
    }
}
