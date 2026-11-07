# VideoLoading

把 Minecraft 的加载界面（Mojang logo 一屏）替换为一段启动视频的 Forge mod，
目标版本 Minecraft **1.21.1**（Forge 52.x，官方映射，Java 21）。

## 功能

- 启动时全屏播放内置视频与音频，替代 Mojang logo；
- **必须完整播放一遍视频才进入游戏**；mod 数量多、加载时间超过视频时长时，
  视频与音频自动循环，直到加载完成；
- 视频播放期间屏幕底部显示「正在加载 Minecraft」横幅（随游戏语言变化）；
- 点击屏幕后右上角出现「跳过」按钮，点击可解除「必须看完一遍」的限制
  （资源未加载完时视频会继续播放到加载结束）；
- 音量默认为游戏主音量的 50%，可在 **选项 → 音乐和声音** 用
  「启动视频音量」滑条调整，配置持久化在
  `config/videoloading.properties`。

## 技术说明

- 视频**运行时解码**：用 mod 内置的 ffmpeg 把打包的视频解码为原始 RGBA
  像素流（后台线程实时节拍解码），渲染线程逐帧上传纹理绘制；
- 音频不经过 Minecraft 声音引擎（加载前半段 OpenAL 未就绪），而是用
  `javax.sound.sampled` 直接播放 WAV 并循环，音量通过 `MASTER_GAIN` 控制；
- 跨平台解码器：jar 内同时携带 `ffmpeg-windows.exe`、
  `ffmpeg-macos-arm64`、`ffmpeg-macos-x64`、`ffmpeg-linux-x64`；
  找不到对应平台的二进制时回退系统 PATH 中的 `ffmpeg`，仍失败则
  保留原版 Mojang logo。

## 换视频

1. 把新的 `video.mp4` / `audio.mp3` 放到仓库根目录；
2. 运行 `python scripts/prepare_startup_video.py`（依赖
   `pip install imageio-ffmpeg`），自动转码到
   `src/main/resources/assets/videoloading/logo_video/`；
3. macOS / Linux 解码器需按脚本内说明手动下载静态二进制放入同一目录。

## 构建

```bash
./gradlew.bat build        # Windows
./gradlew build            # macOS / Linux
```

产物在 `build/libs/`。仓库根目录的 `video.mp4` / `audio.mp3` 为原始素材，
已加入 `.gitignore`。

## 许可

GPL-2.0，见 `LICENSE`。内置的 ffmpeg 二进制遵循其上游许可
（Windows 版来自 imageio-ffmpeg，macOS/Linux 版来自
[eugeneware/ffmpeg-static](https://github.com/eugeneware/ffmpeg-static)）。
