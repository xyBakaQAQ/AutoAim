package com.xybaka.autoaim.modules.misc;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.settings.BooleanSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import org.lwjgl.glfw.GLFW;

public class Teams extends Module {

    private final Minecraft mc = Minecraft.getInstance();

    public final BooleanSetting scoreboard = new BooleanSetting("Scoreboard", true);
    public final BooleanSetting colorCheck = new BooleanSetting("ColorCheck", false);

    public Teams() {
        super("Teams", Category.MISC, GLFW.GLFW_KEY_UNKNOWN);
    }

    public boolean isTeam(Player target) {
        if (mc.player == null || target == null) return false;

        // 计分板检测
        if (scoreboard.isEnabled()) {
            PlayerTeam myTeam = (PlayerTeam) mc.player.getTeam();
            PlayerTeam targetTeam = (PlayerTeam) target.getTeam();

            if (myTeam != null && targetTeam != null) {
                if (myTeam == targetTeam) {
                    return true;
                }
            }
        }

        // 名字颜色检测
        if (colorCheck.isEnabled()) {
            if (mc.player.getDisplayName().getStyle().getColor() != null &&
                    target.getDisplayName().getStyle().getColor() != null) {

                return mc.player.getDisplayName().getStyle().getColor()
                        .equals(target.getDisplayName().getStyle().getColor());
            }
        }

        return false;
    }
}