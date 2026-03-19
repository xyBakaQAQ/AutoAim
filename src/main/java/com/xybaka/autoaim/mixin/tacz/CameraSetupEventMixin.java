package com.xybaka.autoaim.mixin.tacz;

import com.xybaka.autoaim.modules.ModuleManager;
import com.xybaka.autoaim.modules.combat.NoRecoil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.tacz.guns.client.event.CameraSetupEvent", remap = false)
public class CameraSetupEventMixin {

    @Inject(method = "applyCameraRecoil", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onApplyCameraRecoil(CallbackInfo ci) {
        NoRecoil noRecoil = (NoRecoil) ModuleManager.instance.getModuleByName("NoRecoil");
        if (noRecoil != null && noRecoil.isEnabled()) {
            ci.cancel();
        }
    }
}
