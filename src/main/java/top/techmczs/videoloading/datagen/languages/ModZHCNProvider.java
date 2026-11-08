package top.techmczs.videoloading.datagen.languages;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

import top.techmczs.videoloading.client.StartupVideoConfig;

/**
 * 简体中文翻译生成器。
 */
public class ModZHCNProvider extends FabricLanguageProvider {
    public ModZHCNProvider(FabricDataOutput dataOutput) {
        // FabricLanguageProvider 默认注册为 en_us，zh_cn 需显式指定语言代码
        super(dataOutput, "zh_cn");
    }

    @Override
    public void generateTranslations(TranslationBuilder translationBuilder) {
        translationBuilder.add(StartupVideoConfig.SKIP_KEY, "跳过");
        translationBuilder.add(StartupVideoConfig.VOLUME_KEY, "启动视频音量");
        // 1.20.1 原版语言文件没有该 key，横幅文案需 mod 自带
        translationBuilder.add("gui.loadingMinecraft", "正在加载 Minecraft");
    }
}
