package top.techmczs.videoloading.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

import top.techmczs.videoloading.datagen.languages.ModENUSProvider;
import top.techmczs.videoloading.datagen.languages.ModZHCNProvider;

/**
 * 数据生成入口：目前只生成语言文件（en_us / zh_cn）。
 */
public class VideoLoadingDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(ModENUSProvider::new);
        pack.addProvider(ModZHCNProvider::new);
    }
}
