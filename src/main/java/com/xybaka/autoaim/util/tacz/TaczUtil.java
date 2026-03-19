package com.xybaka.autoaim.util.tacz;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

public final class TaczUtil {
    private TaczUtil() {
    }

    // 检查当前运行环境是否存在 TaCZ。
    public static boolean hasTacz() {
        try {
            Class.forName("com.tacz.guns.api.item.IGun");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    // 客户端主动触发一次 TaCZ 开火。
    public static boolean shoot(LocalPlayer player) {
        try {
            Class<?> gunOperatorClass = Class.forName("com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator");
            if (!gunOperatorClass.isInstance(player)) {
                return false;
            }

            Method shootMethod = gunOperatorClass.getMethod("shoot");
            shootMethod.invoke(player);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    // 检查当前主手枪是否还能开火。
    public static boolean hasShootableAmmo(LocalPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            return false;
        }

        try {
            Class<?> gunClass = Class.forName("com.tacz.guns.api.item.IGun");
            Object item = stack.getItem();
            if (!gunClass.isInstance(item)) {
                return true;
            }

            Method getCurrentAmmoCount = gunClass.getMethod("getCurrentAmmoCount", ItemStack.class);
            Method hasBulletInBarrel = gunClass.getMethod("hasBulletInBarrel", ItemStack.class);
            int ammo = (int) getCurrentAmmoCount.invoke(item, stack);
            boolean bulletInBarrel = (boolean) hasBulletInBarrel.invoke(item, stack);
            return ammo > 0 || bulletInBarrel;
        } catch (Throwable ignored) {
            return true;
        }
    }

    // 读取同步的开镜状态。
    public static boolean isAiming(LocalPlayer player) {
        try {
            Class<?> gunOperatorClass = Class.forName("com.tacz.guns.api.entity.IGunOperator");
            if (!gunOperatorClass.isInstance(player)) {
                return false;
            }

            Method getSynIsAiming = gunOperatorClass.getMethod("getSynIsAiming");
            return (boolean) getSynIsAiming.invoke(player);
        } catch (Throwable ignored) {
            return false;
        }
    }

    // 读取同步的开镜进度，范围 0.0 - 1.0。
    public static float getAimProgress(LocalPlayer player) {
        try {
            Class<?> gunOperatorClass = Class.forName("com.tacz.guns.api.entity.IGunOperator");
            if (!gunOperatorClass.isInstance(player)) {
                return 0.0F;
            }

            Method getSynAimingProgress = gunOperatorClass.getMethod("getSynAimingProgress");
            return ((Number) getSynAimingProgress.invoke(player)).floatValue();
        } catch (Throwable ignored) {
            return 0.0F;
        }
    }

    // 读取同步的拉栓状态。
    public static boolean isBolting(LocalPlayer player) {
        try {
            Class<?> gunOperatorClass = Class.forName("com.tacz.guns.api.entity.IGunOperator");
            if (!gunOperatorClass.isInstance(player)) {
                return false;
            }

            Method getSynIsBolting = gunOperatorClass.getMethod("getSynIsBolting");
            return (boolean) getSynIsBolting.invoke(player);
        } catch (Throwable ignored) {
            return false;
        }
    }

    // 任意换弹阶段中返回 true。
    public static boolean isReloading(LocalPlayer player) {
        return getReloadStateFlag(player, "isReloading");
    }

    // 换弹收尾阶段返回 true。
    public static boolean isReloadFinishing(LocalPlayer player) {
        return getReloadStateFlag(player, "isReloadFinishing");
    }

    // 空仓换弹时返回 true。
    public static boolean isReloadingEmpty(LocalPlayer player) {
        return getReloadStateFlag(player, "isReloadingEmpty");
    }

    // 战术换弹时返回 true。
    public static boolean isReloadingTactical(LocalPlayer player) {
        return getReloadStateFlag(player, "isReloadingTactical");
    }

    // 统一读取 TaCZ 的换弹状态标记。
    private static boolean getReloadStateFlag(LocalPlayer player, String methodName) {
        try {
            Class<?> gunOperatorClass = Class.forName("com.tacz.guns.api.entity.IGunOperator");
            if (!gunOperatorClass.isInstance(player)) {
                return false;
            }

            Object reloadState = gunOperatorClass.getMethod("getSynReloadState").invoke(player);
            if (reloadState == null) {
                return false;
            }

            Object stateType = reloadState.getClass().getMethod("getStateType").invoke(reloadState);
            if (stateType == null) {
                return false;
            }

            Method stateMethod = stateType.getClass().getMethod(methodName);
            return (boolean) stateMethod.invoke(stateType);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
