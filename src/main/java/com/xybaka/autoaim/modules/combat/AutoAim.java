package com.xybaka.autoaim.modules.combat;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.settings.BooleanSetting;
import com.xybaka.autoaim.modules.settings.NumberSetting;
import com.xybaka.autoaim.util.RotationUtil;
import com.xybaka.autoaim.util.TargetUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class AutoAim extends Module {
    public final NumberSetting range = new NumberSetting("Range", 4.5, 1.0, 10.0, 0.1);
//    public final BooleanSetting silent = new BooleanSetting("Silent", true);

    public AutoAim() {
        super("AutoAim", Category.COMBAT, GLFW.GLFW_KEY_G);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!isEnabled() || mc.player == null) return;
        if (event.phase != TickEvent.Phase.START) return;

        LivingEntity target = TargetUtil.getBestTarget(range.getValue());
        if (target == null) return;

        float[] rotations = RotationUtil.getRotationsToEntity(target);

        mc.player.setYRot(rotations[0]);
        mc.player.setXRot(rotations[1]);
    }
}
