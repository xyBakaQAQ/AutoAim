package com.xybaka.autoaim.mixin;

import com.xybaka.autoaim.modules.ModuleManager;
import com.xybaka.autoaim.modules.render.Camera;
import com.xybaka.autoaim.modules.render.NoFov;
import com.xybaka.autoaim.util.RotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Unique
    private boolean autoAim$restoringSilentPick;
    @Unique
    private float autoAim$pickYaw;
    @Unique
    private float autoAim$pickPitch;
    @Unique
    private float autoAim$pickYawO;
    @Unique
    private float autoAim$pickPitchO;

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void onBobHurt(CallbackInfo ci) {
        Camera mod = (Camera) ModuleManager.instance.getModuleByName("Camera");
        if (mod != null && mod.isEnabled() && mod.noHurtCam.isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void onGetFov(CallbackInfoReturnable<Double> cir) {
        NoFov mod = (NoFov) ModuleManager.instance.getModuleByName("NoFov");
        if (mod != null && mod.isEnabled()) {
            cir.setReturnValue(mod.fov.getValue());
        }
    }

    @Inject(method = "pick", at = @At("HEAD"))
    private void onPickHead(float partialTick, CallbackInfo ci) {
        if (!RotationUtil.isSilentActive()) {
            autoAim$restoringSilentPick = false;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity == null) {
            autoAim$restoringSilentPick = false;
            return;
        }

        autoAim$restoringSilentPick = true;
        autoAim$pickYaw = cameraEntity.getYRot();
        autoAim$pickPitch = cameraEntity.getXRot();
        autoAim$pickYawO = cameraEntity.yRotO;
        autoAim$pickPitchO = cameraEntity.xRotO;

        float yaw = RotationUtil.getEffectiveYaw();
        float pitch = RotationUtil.getEffectivePitch();
        cameraEntity.setYRot(yaw);
        cameraEntity.setXRot(pitch);
        cameraEntity.yRotO = yaw;
        cameraEntity.xRotO = pitch;
    }

    @Inject(method = "pick", at = @At("RETURN"))
    private void onPickReturn(float partialTick, CallbackInfo ci) {
        if (!autoAim$restoringSilentPick) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity != null) {
            cameraEntity.setYRot(autoAim$pickYaw);
            cameraEntity.setXRot(autoAim$pickPitch);
            cameraEntity.yRotO = autoAim$pickYawO;
            cameraEntity.xRotO = autoAim$pickPitchO;
        }

        autoAim$restoringSilentPick = false;
    }
}
