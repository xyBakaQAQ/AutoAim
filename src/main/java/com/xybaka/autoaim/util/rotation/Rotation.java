package com.xybaka.autoaim.util.rotation;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class Rotation {
    private static final Minecraft mc = Minecraft.getInstance();

    private Rotation() {
    }

    public static float[] getRotationsToEntity(Entity entity) {
        return getRotationsToPosition(entity.position().add(0.0D, entity.getEyeHeight(), 0.0D));
    }

    public static float[] getRotationsToPosition(Vec3 targetPos) {
        Vec3 eyesPos = mc.player.getEyePosition(1.0F);
        double diffX = targetPos.x - eyesPos.x;
        double diffZ = targetPos.z - eyesPos.z;
        double diffY = targetPos.y - eyesPos.y;

        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, dist));

        return new float[]{
                mc.player.getYRot() + Mth.wrapDegrees(yaw - mc.player.getYRot()),
                mc.player.getXRot() + Mth.wrapDegrees(pitch - mc.player.getXRot())
        };
    }
}
