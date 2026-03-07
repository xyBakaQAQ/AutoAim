package com.xybaka.autoaim.util;

import com.xybaka.autoaim.modules.ModuleManager;
import com.xybaka.autoaim.modules.client.Target;
import com.xybaka.autoaim.modules.client.Teams;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public class TargetUtil {
    private static final Minecraft mc = Minecraft.getInstance();

    private static Target getTargetModule() {
        return ModuleManager.instance.get(Target.class); // 修复：用 instance
    }

    public static LivingEntity getBestTarget(double range) {
        if (mc.level == null || mc.player == null) return null;
        Target t = getTargetModule();
        if (t == null) return null;

        return mc.level.getEntities(mc.player, mc.player.getBoundingBox().inflate(range))
                .stream()
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .filter(e -> isValid(e, t))  // 传入 t，避免重复获取
                .filter(e -> mc.player.distanceTo(e) <= range)
                .min(getComparator(t.mode.getValue()))
                .orElse(null);
    }

    private static Comparator<LivingEntity> getComparator(Target.Mode mode) {
        return switch (mode) {
            case CLOSEST -> Comparator.comparingDouble(e -> mc.player.distanceTo(e));
            case LOWEST_HEALTH -> Comparator.comparingDouble(LivingEntity::getHealth);
            case FOV -> Comparator.comparingDouble(TargetUtil::getFovAngle);
        };
    }

    private static double getFovAngle(LivingEntity entity) {
        Vec3 look = mc.player.getLookAngle();
        Vec3 toEntity = entity.getEyePosition()
                .subtract(mc.player.getEyePosition())
                .normalize();
        return -look.dot(toEntity);
    }

    public static boolean isValid(LivingEntity entity) {
        Target t = getTargetModule();
        if (t == null) return false;
        return isValid(entity, t);
    }

    private static boolean isValid(LivingEntity entity, Target t) {
        if (entity == mc.player || !entity.isAlive() || entity.isInvisible())
            return false;

        if (entity instanceof Player p) {
            if (!t.players.isEnabled()) return false;

            if (p.isCreative() || p.isSpectator()) return false;
            Teams teams = ModuleManager.instance.get(Teams.class);

            if (teams != null && teams.isEnabled()) {return !teams.isTeam(p);}return true;
        }
        if (entity instanceof Monster) return t.monsters.isEnabled();
        if (entity instanceof Animal) return t.animals.isEnabled();
        if (entity instanceof AbstractVillager) return t.villagers.isEnabled();
        if (entity instanceof IronGolem || entity instanceof SnowGolem) return t.golems.isEnabled();
        if (entity instanceof WaterAnimal) return t.waterAnimals.isEnabled();
        if (entity instanceof AbstractFish) return t.waterCreatures.isEnabled();
        if (entity instanceof AmbientCreature) return t.ambient.isEnabled();
        return false;
    }
}