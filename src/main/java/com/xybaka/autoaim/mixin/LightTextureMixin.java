package com.xybaka.autoaim.mixin;

import com.xybaka.autoaim.modules.ModuleManager;
import com.xybaka.autoaim.modules.render.FullBright;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightTexture.class)
public class LightTextureMixin {

    @Redirect(
            method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;",
                    ordinal = 1
            )
    )
    private Object onGetGammaValue(OptionInstance<Double> gammaOption) {
        FullBright fullBright = ModuleManager.instance.get(FullBright.class);
        if (fullBright != null && fullBright.isEnabled()) {
            return 16.0D;
        }
        return gammaOption.get();
    }
}
