package com.xybaka.autoaim.mixin;

import com.xybaka.autoaim.util.RotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Redirect(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;rotLerp(FFF)F", ordinal = 0)
    )
    private float useSilentBodyYaw(float partialTicks, float start, float end, LivingEntity entity, float entityYaw, float partialTick,
                                   com.mojang.blaze3d.vertex.PoseStack poseStack,
                                   MultiBufferSource buffer,
                                   int packedLight) {
        if (entity == Minecraft.getInstance().player && RotationUtil.isSilentActive()) {
            float yaw = RotationUtil.getSilentYaw();
            return Mth.rotLerp(partialTicks, yaw, yaw);
        }

        return Mth.rotLerp(partialTicks, start, end);
    }

    @Redirect(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;rotLerp(FFF)F", ordinal = 1)
    )
    private float useSilentHeadYaw(float partialTicks, float start, float end, LivingEntity entity, float entityYaw, float partialTick,
                                   com.mojang.blaze3d.vertex.PoseStack poseStack,
                                   MultiBufferSource buffer,
                                   int packedLight) {
        if (entity == Minecraft.getInstance().player && RotationUtil.isSilentActive()) {
            float yaw = RotationUtil.getSilentYaw();
            return Mth.rotLerp(partialTicks, yaw, yaw);
        }

        return Mth.rotLerp(partialTicks, start, end);
    }

    @Redirect(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F", ordinal = 0)
    )
    private float useSilentHeadPitch(float partialTicks, float start, float end, LivingEntity entity, float entityYaw, float partialTick,
                                     com.mojang.blaze3d.vertex.PoseStack poseStack,
                                     MultiBufferSource buffer,
                                     int packedLight) {
        if (entity == Minecraft.getInstance().player && RotationUtil.isSilentActive()) {
            float pitch = RotationUtil.getSilentPitch();
            return Mth.lerp(partialTicks, pitch, pitch);
        }

        return Mth.lerp(partialTicks, start, end);
    }
}
