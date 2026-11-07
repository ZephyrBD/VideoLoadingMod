/*
 * Copyright (C) 2025 MCZS(ZephyrBD and FayYu)
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
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
    }
}
