package com.xybaka.autoaim.util.rotation;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public final class RotationManager {
    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean silentActive;
    private static float silentYaw;
    private static float silentPitch;

    private RotationManager() {
    }

    public static void setSilentRotation(float yaw, float pitch) {
        silentActive = true;
        silentYaw = yaw;
        silentPitch = pitch;
    }

    public static void clearSilentRotation() {
        silentActive = false;
    }

    public static boolean isSilentActive() {
        return silentActive;
    }

    public static float getSilentYaw() {
        return silentYaw;
    }

    public static float getSilentPitch() {
        return silentPitch;
    }

    public static float getEffectiveYaw() {
        return silentActive ? silentYaw : mc.player != null ? mc.player.getYRot() : 0.0F;
    }

    public static float getEffectivePitch() {
        return silentActive ? silentPitch : mc.player != null ? mc.player.getXRot() : 0.0F;
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
