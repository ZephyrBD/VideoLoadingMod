package top.techmczs.videoloading.mixin.client;

import net.minecraft.client.texture.NativeImage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 暴露 NativeImage 底层像素内存指针，启动视频解码线程据此
 * 用 ByteBuffer 直写 RGBA 像素，避免逐像素 setColor 的开销。
 */
@Mixin(NativeImage.class)
public interface NativeImageAccessor {
    @Accessor("pointer")
    long videoloading$getPointer();
}
