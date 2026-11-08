package top.techmczs.videoloading;

import com.mojang.logging.LogUtils;

import net.neoforged.fml.common.Mod;

import org.slf4j.Logger;

/**
 * mod 入口：本 mod 是纯客户端 mod，没有任何注册逻辑，
 * 启动视频的全部行为都由 mixin 在加载早期接管，
 * 这里只用于让 FML 识别 mod 存在。
 */
@Mod("videoloading")
public class VideoLoading {
    public static final Logger LOGGER = LogUtils.getLogger();

    public VideoLoading() {
        LOGGER.info("[VideoLoading] mod loaded (startup video takes over the loading screen via mixin)");
    }
}
