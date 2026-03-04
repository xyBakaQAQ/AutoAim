package com.xybaka.autoaim.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class RotationUtil {
    private static final Minecraft mc = Minecraft.getInstance();

    public static float[] getRotationsToEntity(Entity entity) {
        Vec3 eyesPos = mc.player.getEyePosition(1.0F);
        double diffX = entity.getX() - eyesPos.x;
        double diffZ = entity.getZ() - eyesPos.z;
        double diffY = (entity.getY() + entity.getEyeHeight()) - eyesPos.y;

        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, dist));

        return new float[]{
                mc.player.getYRot() + Mth.wrapDegrees(yaw - mc.player.getYRot()),
                mc.player.getXRot() + Mth.wrapDegrees(pitch - mc.player.getXRot())
        };
    }
}