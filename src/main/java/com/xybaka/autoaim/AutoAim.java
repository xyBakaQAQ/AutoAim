package com.xybaka.autoaim;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import java.util.List;

@Mod.EventBusSubscriber(modid = "autoaim", bus = Bus.FORGE, value = Dist.CLIENT)
public class AutoAim {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final double AIM_DISTANCE = 50.0D; // 自瞄范围
    private static boolean aimAtVillagers = true; // 是否瞄准村民
    private static boolean aimAtPlayers = false; // 是否瞄准玩家
    private static boolean usePosLookPacket = false; // 是否使用C04PacketPlayerPositionLook

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (mc.player == null || mc.level == null) {
                return;
            }

            while (KeyBindings.TOGGLE_VILLAGER_KEY.get().consumeClick()) {
                toggleAimAtVillagers();
            }

            while (KeyBindings.TOGGLE_PLAYER_KEY.get().consumeClick()) {
                toggleAimAtPlayers();
            }

            while (KeyBindings.TOGGLE_POSLOOK_KEY.get().consumeClick()) {
                toggleUsePosLookPacket();
            }

            Entity target = findClosestTarget();
            if (target != null) {
                aimAtTarget(target);
            }
        }
    }

    private static Entity findClosestTarget() {
        Entity closestEntity = null;
        double closestDistance = Double.MAX_VALUE;

        List<Entity> entities = mc.level.getEntities(mc.player, mc.player.getBoundingBox().inflate(AIM_DISTANCE), entity ->
                !entity.isRemoved() &&
                        entity instanceof LivingEntity livingEntity && !livingEntity.isDeadOrDying() &&
                        ((aimAtVillagers && entity instanceof Villager) ||
                                (aimAtPlayers && entity instanceof net.minecraft.client.player.AbstractClientPlayer &&
                                        entity != mc.player))
        );

        for (Entity entity : entities) {
            double distance = mc.player.distanceTo(entity);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestEntity = entity;
            }
        }

        return closestEntity;
    }

    private static void aimAtTarget(Entity target) {
        Vec3 targetPos = target.position().add(0, target.getEyeHeight(), 0);
        Vec3 playerPos = mc.player.position().add(0, mc.player.getEyeHeight(), 0);

        double deltaX = targetPos.x - playerPos.x;
        double deltaY = targetPos.y - playerPos.y;
        double deltaZ = targetPos.z - playerPos.z;

        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float yaw = (float) (Math.atan2(deltaZ, deltaX) * (180 / Math.PI)) - 90;
        float pitch = (float) -(Math.atan2(deltaY, distance) * (180 / Math.PI));

        if (usePosLookPacket) {
            mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, mc.player.onGround()));
        } else {
            mc.player.setYRot(yaw);
            mc.player.setXRot(pitch);
        }
    }

    private static void toggleAimAtVillagers() {
        aimAtVillagers = !aimAtVillagers;
        mc.player.displayClientMessage(Component.literal("Aim at villagers: " + aimAtVillagers), true);
    }

    private static void toggleAimAtPlayers() {
        aimAtPlayers = !aimAtPlayers;
        mc.player.displayClientMessage(Component.literal("Aim at players: " + aimAtPlayers), true);
    }

    private static void toggleUsePosLookPacket() {
        usePosLookPacket = !usePosLookPacket;
        mc.player.displayClientMessage(Component.literal("Use PosLook packet: " + usePosLookPacket), true);
    }
}