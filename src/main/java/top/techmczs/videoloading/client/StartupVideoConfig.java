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
package top.techmczs.videoloading.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

/**
 * 启动视频的玩家配置：目前只有音量。
 *
 * <p>音量是相对主音量的系数，默认 0.5（即主音量的一半），
 * 滑条放在「音乐和声音」设置里。值持久化到独立的小配置文件，
 * 不挂进原版 {@code options.txt}（往 {@code Options} 序列化里
 * 塞自定义项需要更大范围的 mixin）。
 */
public final class StartupVideoConfig {
    private static final Path CONFIG_FILE =
            FMLPaths.CONFIGDIR.get().resolve("videoloading.properties");
    private static final String KEY_VOLUME = "startupVideoVolume";
    private static final double DEFAULT_VOLUME = 0.5D;

    /** 翻译 key，语言文件与运行时共用一份 */
    public static final String VOLUME_KEY = "videoloading.startup_video.volume";
    public static final String SKIP_KEY = "videoloading.startup_video.skip";

    /** 音量相对主音量的系数，0~1；修改即保存并实时作用于正在播放的音频 */
    private static final OptionInstance<Double> VOLUME = new OptionInstance<>(
            VOLUME_KEY,
            OptionInstance.noTooltip(),
            (label, value) -> label.copy().append(": " + Math.round(value * 100D) + "%"),
            OptionInstance.UnitDouble.INSTANCE,
            loadVolume(),
            StartupVideoConfig::onVolumeChanged);

    private StartupVideoConfig() {
    }

    public static OptionInstance<Double> volumeOption() {
        return VOLUME;
    }

    public static double getVolume() {
        return VOLUME.get();
    }

    private static void onVolumeChanged(double value) {
        saveVolume(value);
        StartupVideo.applyVolume();
    }

    private static double loadVolume() {
        if (Files.isRegularFile(CONFIG_FILE)) {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(CONFIG_FILE)) {
                props.load(in);
            } catch (IOException e) {
                return DEFAULT_VOLUME;
            }
            try {
                return Double.parseDouble(props.getProperty(KEY_VOLUME, String.valueOf(DEFAULT_VOLUME)));
            } catch (NumberFormatException e) {
                return DEFAULT_VOLUME;
            }
        }
        return DEFAULT_VOLUME;
    }

    private static void saveVolume(double value) {
        Properties props = new Properties();
        props.setProperty(KEY_VOLUME, String.valueOf(value));
        try (OutputStream out = Files.newOutputStream(CONFIG_FILE)) {
            props.store(out, "VideoLoading startup video settings");
        } catch (IOException e) {
            // 保存失败只影响下次启动的默认值，静默即可
        }
    }
}
