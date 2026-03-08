package com.xybaka.autoaim.mixin;

import com.xybaka.autoaim.modules.ModuleManager;
import com.xybaka.autoaim.modules.render.Camera;
import com.xybaka.autoaim.modules.render.NoFov;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

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
}
