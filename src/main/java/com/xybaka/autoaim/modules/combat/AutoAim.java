package com.xybaka.autoaim.modules.combat;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.settings.BooleanSetting;
import com.xybaka.autoaim.modules.settings.NumberSetting;
import com.xybaka.autoaim.util.RotationUtil;
import com.xybaka.autoaim.util.TargetUtil;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class AutoAim extends Module {
    public final NumberSetting range = new NumberSetting("Range", 4.5, 1.0, 10, 0.1);
    public final BooleanSetting silent = new BooleanSetting("Silent", false);

    public AutoAim() {
        super("AutoAim", Category.COMBAT, GLFW.GLFW_KEY_G);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!isEnabled() || mc.player == null || event.phase != TickEvent.Phase.START) return;

        LivingEntity target = TargetUtil.getBestTarget(range.getValue());
        if (target == null) return;

        float[] rotations = RotationUtil.getRotationsToEntity(target);
        float yaw = rotations[0];
        float pitch = rotations[1];

        if (silent.isEnabled()) {
            mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, mc.player.onGround()));
        } else {
            mc.player.setYRot(yaw);
            mc.player.setXRot(pitch);
        }
    }
}