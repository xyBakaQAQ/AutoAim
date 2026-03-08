package com.xybaka.autoaim.mixin;

import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.ModuleManager;
import com.xybaka.autoaim.modules.movement.invMove;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin implements IForgeKeyMapping {
    @Override
    public boolean isConflictContextAndModifierActive() {
        invMove mod = ModuleManager.instance.get(invMove.class);
        if (mod != null && mod.isEnabled() && Module.mc.screen != null) {
            return true;
        }
        return IForgeKeyMapping.super.isConflictContextAndModifierActive();
    }
}