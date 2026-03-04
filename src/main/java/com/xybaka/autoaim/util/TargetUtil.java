package com.xybaka.autoaim.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class TargetUtil {
    private static final Minecraft mc = Minecraft.getInstance();

    public static LivingEntity getBestTarget(double range, boolean targetPlayers, boolean targetMonsters, boolean targetAnimals) {
        if (mc.level == null || mc.player == null) return null;

        List<Entity> entities = mc.level.getEntities(mc.player,
                mc.player.getBoundingBox().inflate(range));

        return entities.stream()
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .filter(entity -> isValid(entity, targetPlayers, targetMonsters, targetAnimals))
                .min((e1, e2) -> Float.compare(mc.player.distanceTo(e1), mc.player.distanceTo(e2)))
                .orElse(null);
    }

    private static boolean isValid(LivingEntity entity, boolean targetPlayers, boolean targetMonsters, boolean targetAnimals) {
        // 基础过滤：不能是自己，必须存活，且不能是隐身/无敌状态
        if (entity == mc.player || !entity.isAlive() || entity.isInvisible()) return false;

        // 1. 玩家过滤
        if (entity instanceof Player) {
            if (!targetPlayers) return false;
            // 排除创造模式玩家
            if (((Player) entity).isCreative()) return false;
        }

        // 2. 敌对生物过滤 (僵尸、骷髅、爬行者等)
        if (entity instanceof Monster) {
            return targetMonsters;
        }

        // 3. 友好/被动生物过滤 (猪、羊、村民等)
        if (entity instanceof Animal || entity instanceof AbstractVillager) {
            return targetAnimals;
        }

        // 其他情况（例如盔甲架等，默认返回 false）
        return false;
    }
}