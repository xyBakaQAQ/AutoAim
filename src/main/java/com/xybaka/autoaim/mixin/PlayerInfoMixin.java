package com.xybaka.autoaim.mixin;

import com.xybaka.autoaim.modules.ModuleManager;
import com.xybaka.autoaim.modules.render.SkinOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public class PlayerInfoMixin {

    @Inject(method = "getSkinLocation", at = @At("RETURN"), cancellable = true)
    private void onGetSkinLocation(CallbackInfoReturnable<ResourceLocation> cir) {
        SkinOverlay mod = ModuleManager.instance.get(SkinOverlay.class);
        if (mod != null && mod.isEnabled() && mod.isSkinLoaded()) {
            cir.setReturnValue(mod.getCustomSkinLocation());
        }
    }
    @Inject(method = "getCapeLocation", at = @At("RETURN"), cancellable = true)
    private void onGetCapeLocation(CallbackInfoReturnable<ResourceLocation> cir) {
        SkinOverlay mod = ModuleManager.instance.get(SkinOverlay.class);
        if (mod != null && mod.isEnabled() && mod.isCloakLoaded()) {
            cir.setReturnValue(mod.getCustomCloakLocation());
        }
    }
}