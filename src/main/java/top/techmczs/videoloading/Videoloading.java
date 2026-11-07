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
package top.techmczs.videoloading;

import com.mojang.logging.LogUtils;

import net.minecraftforge.fml.common.Mod;

import org.slf4j.Logger;

/**
 * mod 入口：本 mod 是纯客户端 mod，没有任何注册逻辑，
 * 启动视频的全部行为都由 mixin 在加载早期接管，
 * 这里只用于让 FML 识别 mod 存在。
 */
@Mod("videoloading")
public class Videoloading {
    public static final Logger LOGGER = LogUtils.getLogger();

    public Videoloading() {
        LOGGER.info("[VideoLoading] mod loaded（启动视频由 mixin 在加载界面接管）");
    }
}
