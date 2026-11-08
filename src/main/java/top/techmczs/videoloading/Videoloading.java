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
