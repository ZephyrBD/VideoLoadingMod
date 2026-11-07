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

import net.minecraftforge.fml.common.Mod;

/**
 * mod 入口：纯客户端 mod，不注册任何内容，实际逻辑都在
 * {@link top.techmczs.videoloading.client.StartupVideo} 与 mixin 里。
 * 构造函数保持空实现，专用服务器上加载也无副作用。
 */
@Mod(Videoloading.MODID)
public class Videoloading {
    public static final String MODID = "videoloading";

    public Videoloading() {
        // 无需初始化：启动视频在 LoadingOverlay 首次渲染时才开始
    }
}
