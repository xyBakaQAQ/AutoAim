package com.xybaka.autoaim.util;

import com.xybaka.autoaim.modules.ModuleManager;
import com.xybaka.autoaim.modules.client.Teams;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;

public final class ColorsUtil {
    private static final int WHITE = 0xFFFFFF;

    private ColorsUtil() {
    }

    public static int getEspColor(Entity entity) {
        if (entity instanceof Player player) {
            Integer preferredTeamColor = getPreferredTeamColor(player);
            if (preferredTeamColor != null) {
                return preferredTeamColor;
            }
        }

        return getVanillaTeamColor(entity);
    }

    public static Integer getPreferredTeamColor(Player player) {
        Teams teams = ModuleManager.instance.get(Teams.class);
        if (teams == null || !teams.isEnabled()) {
            return null;
        }

        if (teams.scoreboard.isEnabled()) {
            Integer scoreboardColor = getScoreboardTeamColor(player);
            if (scoreboardColor != null) {
                return scoreboardColor;
            }
        }

        if (teams.colorCheck.isEnabled()) {
            Integer displayColor = getDisplayNameColor(player);
            if (displayColor != null) {
                return displayColor;
            }
        }

        return null;
    }

    public static int getVanillaTeamColor(Entity entity) {
        Team team = entity.getTeam();
        Integer color = team != null ? team.getColor().getColor() : null;
        return color != null ? color : WHITE;
    }

    private static Integer getScoreboardTeamColor(Player player) {
        Team team = player.getTeam();
        return team != null ? team.getColor().getColor() : null;
    }

    private static Integer getDisplayNameColor(Player player) {
        TextColor textColor = player.getDisplayName().getStyle().getColor();
        return textColor != null ? textColor.getValue() : null;
    }
}
