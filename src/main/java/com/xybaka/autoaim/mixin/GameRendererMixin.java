package com.xybaka.autoaim.mixin;

import com.xybaka.autoaim.modules.ModuleManager;
import com.xybaka.autoaim.modules.render.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void onBobHurt(CallbackInfo ci) {
        Camera mod = (Camera) ModuleManager.instance.getModuleByName("Camera");
        if (mod != null && mod.isEnabled() && mod.noHurtCam.isEnabled()) {
            ci.cancel();
        }
    }
}