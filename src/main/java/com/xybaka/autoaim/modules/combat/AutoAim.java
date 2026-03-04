package com.xybaka.autoaim.modules.combat;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module; // 确保导入的是你自己的 Module
import com.xybaka.autoaim.modules.settings.BooleanSetting;
import com.xybaka.autoaim.modules.settings.NumberSetting;
import com.xybaka.autoaim.util.TargetUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class AutoAim extends Module {
    public final NumberSetting range = new NumberSetting("Range", 4.5, 1.0, 10.0, 0.1);
    public final BooleanSetting players = new BooleanSetting("Players", true);
    public final BooleanSetting monsters = new BooleanSetting("Monsters", true);

    public AutoAim() {
        super("AutoAim", Category.COMBAT, GLFW.GLFW_KEY_G);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!this.isEnabled() || event.phase != TickEvent.Phase.START || mc.player == null) return;

        LivingEntity target = TargetUtil.getBestTarget(
                range.getValue(),
                players.isEnabled(),
                monsters.isEnabled(),
                false
        );

        if (target != null) faceEntity(target);
    }

    private void faceEntity(LivingEntity entity) {
        if (mc.player == null) return;

        double diffX = entity.getX() - mc.player.getX();
        double diffZ = entity.getZ() - mc.player.getZ();
        // 瞄准目标的眼睛位置
        double diffY = (entity.getY() + entity.getEyeHeight()) - (mc.player.getY() + mc.player.getEyeHeight());

        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) -(Math.atan2(diffY, dist) * 180.0D / Math.PI);

        mc.player.setYRot(yaw);
        mc.player.setXRot(pitch);
    }
}