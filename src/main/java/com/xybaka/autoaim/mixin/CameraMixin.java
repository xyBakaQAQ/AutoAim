package com.xybaka.autoaim.mixin;

import com.xybaka.autoaim.modules.ModuleManager;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class CameraMixin {

    @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
    private void onGetMaxZoom(double startingDistance, CallbackInfoReturnable<Double> cir) {
        com.xybaka.autoaim.modules.render.Camera mod =
                (com.xybaka.autoaim.modules.render.Camera) ModuleManager.instance.getModuleByName("Camera");
        if (mod != null && mod.isEnabled() && mod.noClip.isEnabled()) {
            cir.setReturnValue(startingDistance);
        }
    }
}