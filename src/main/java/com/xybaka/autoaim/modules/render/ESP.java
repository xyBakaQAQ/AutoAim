package com.xybaka.autoaim.modules.render;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.util.ColorsUtil;
import com.xybaka.autoaim.util.TargetUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.glfw.GLFW;

public class ESP extends Module {

    public ESP() {
        super("ESP", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    public boolean shouldGlow(Entity entity) {
        if (!isEnabled()) return false;
        if (!(entity instanceof LivingEntity living)) return false;
        return TargetUtil.isValid(living);
    }

    public int getGlowColor(Entity entity) {
        return ColorsUtil.getEspColor(entity);
    }
}