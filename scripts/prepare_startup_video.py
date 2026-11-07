# -*- coding: utf-8 -*-
"""把用户提供的视频/音频转码为启动视频素材，并复制 Windows ffmpeg 解码器。

输入（仓库根目录，手动放入）:
- video.mp4 : 任意编码的启动视频
- audio.mp3 : 对应音频

输出 (src/main/resources/assets/videoloading/logo_video/):
- video.mp4    : H.264 960x540 30fps（运行时解码开销低；注意 960x540 是固定
                 目标尺寸，源视频长宽比不同会有极轻微拉伸）
- audio.wav    : 48kHz 立体声 PCM（javax.sound.sampled 原生支持，无需解码库）
- ffmpeg-windows.exe : imageio_ffmpeg 自带的 ffmpeg 二进制，运行时用它把
                 video.mp4 解码为原始 RGBA 像素流（视频无法以帧序列形式
                 放进内存/资源包，必须运行时解码）

跨平台说明:
- Windows 解码器由本脚本自动打包（ffmpeg-windows.exe）。
- macOS（ffmpeg-macos-arm64 / ffmpeg-macos-x64）与 Linux
  （ffmpeg-linux-x64 / ffmpeg-linux-arm64）需要手动下载静态二进制放进
  OUT_DIR，推荐来源:
  https://github.com/eugeneware/ffmpeg-static/releases （tag 形如 b6.0）
  文件名对应: ffmpeg-darwin-arm64 / ffmpeg-darwin-x64 / ffmpeg-linux-x64
  运行时若 jar 内缺对应平台的二进制，会回退到系统 PATH 中的 ffmpeg，
  仍找不到则保留原版 Mojang logo。

换视频时：把新的 video.mp4 / audio.mp3 放到仓库根目录后重跑本脚本。
"""
import os
import shutil
import subprocess

import imageio_ffmpeg

ROOT = os.path.dirname(os.path.abspath(__file__)) + "/.."
OUT_DIR = os.path.join(
    ROOT, "src/main/resources/assets/videoloading/logo_video")
FF = imageio_ffmpeg.get_ffmpeg_exe()


def run(args):
    subprocess.run(args, check=True)


def transcode_video():
    run([
        FF, "-y", "-v", "error",
        "-i", os.path.join(ROOT, "video.mp4"),
        "-vf", "fps=30,scale=960:540",
        "-c:v", "libx264", "-preset", "medium", "-crf", "22",
        "-pix_fmt", "yuv420p", "-movflags", "+faststart",
        os.path.join(OUT_DIR, "video.mp4"),
    ])
    print("video.mp4 转码完成")


def convert_audio():
    run([
        FF, "-y", "-v", "error",
        "-i", os.path.join(ROOT, "audio.mp3"),
        "-ar", "48000", "-ac", "2", "-c:a", "pcm_s16le",
        os.path.join(OUT_DIR, "audio.wav"),
    ])
    print("audio.wav 转换完成")


def bundle_ffmpeg():
    dest = os.path.join(OUT_DIR, "ffmpeg-windows.exe")
    shutil.copyfile(FF, dest)
    print("ffmpeg-windows.exe 已复制 (%.1f MB)" % (os.path.getsize(dest) / 1e6))


if __name__ == "__main__":
    os.makedirs(OUT_DIR, exist_ok=True)
    transcode_video()
    convert_audio()
    bundle_ffmpeg()
