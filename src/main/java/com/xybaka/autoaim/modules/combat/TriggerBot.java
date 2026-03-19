package com.xybaka.autoaim.modules.combat;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.settings.BooleanSetting;
import com.xybaka.autoaim.modules.settings.NumberSetting;
import com.xybaka.autoaim.util.TargetUtil;
import com.xybaka.autoaim.util.tacz.TaczUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class TriggerBot extends Module {
    public final BooleanSetting checkAiming = new BooleanSetting("Check Aiming", true);
    public final NumberSetting range = new NumberSetting("Range", 4.5D, 1.0D, 10.0D, 0.1D);
    public final NumberSetting delay = new NumberSetting("Delay", 100.0D, 0.0D, 500.0D, 5.0D);

    private long lastShotTime;

    public TriggerBot() {
        super("TriggerBot", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!isEnabled() || event.phase != TickEvent.Phase.START || mc.player == null || mc.hitResult == null) {
            return;
        }

        if (!(mc.hitResult instanceof EntityHitResult entityHitResult) || mc.hitResult.getType() != HitResult.Type.ENTITY) {
            return;
        }

        if (!(entityHitResult.getEntity() instanceof LivingEntity target)) {
            return;
        }

        if (!TargetUtil.isValid(target) || mc.player.distanceTo(target) > range.getValue()) {
            return;
        }

        if (checkAiming.isEnabled() && TaczUtil.getAimProgress(mc.player) < 1.0F) {
            return;
        }

        if (!TaczUtil.hasShootableAmmo(mc.player)) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastShotTime < (long) delay.getValue()) {
            return;
        }

        if (TaczUtil.shoot(mc.player)) {
            lastShotTime = now;
        }
    }
}
