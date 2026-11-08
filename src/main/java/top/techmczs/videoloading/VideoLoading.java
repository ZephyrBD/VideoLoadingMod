package top.techmczs.videoloading;

import net.minecraftforge.fml.common.Mod;

/**
 * VideoLoading mod 入口。功能全部由客户端 Mixin 驱动，这里
 * 只负责让 FML 扫描到 mod（clientSideOnly=true 时专用服务器
 * 不会加载本 mod）。
 */
@Mod("videoloading")
public class VideoLoading {
    public VideoLoading() {
    }
}
