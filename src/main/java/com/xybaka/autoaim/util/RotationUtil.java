package com.xybaka.autoaim.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class RotationUtil {
    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean silentActive;
    private static float silentYaw;
    private static float silentPitch;

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

    public static void setSilentRotation(float yaw, float pitch) {
        silentActive = true;
        silentYaw = yaw;
        silentPitch = pitch;
    }

    public static void clearSilentRotation() {
        silentActive = false;
    }

    public static Packet<?> applySilentRotation(Packet<?> packet) {
        if (!(packet instanceof ServerboundMovePlayerPacket movePacket) || !silentActive) {
            return packet;
        }

        boolean onGround = movePacket.isOnGround();

        if (movePacket.hasPosition()) {
            return new ServerboundMovePlayerPacket.PosRot(
                    movePacket.getX(0.0),
                    movePacket.getY(0.0),
                    movePacket.getZ(0.0),
                    silentYaw,
                    silentPitch,
                    onGround
            );
        }

        return new ServerboundMovePlayerPacket.Rot(silentYaw, silentPitch, onGround);
    }
}
