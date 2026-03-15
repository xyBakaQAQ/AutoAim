package com.xybaka.autoaim.gui.targethud.style;

import com.xybaka.autoaim.modules.render.TargetHud;
import com.xybaka.autoaim.util.ColorsUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.Locale;

public final class AutoAimStyle {
    public static final AutoAimStyle INSTANCE = new AutoAimStyle();

    private static final int BACKGROUND_COLOR = 0x90000000;
    private static final int BORDER_COLOR = 0xA0FFFFFF;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int SUB_TEXT_COLOR = 0xFFBFBFBF;
    private static final int HEALTH_BACKGROUND_COLOR = 0x60303030;

    private AutoAimStyle() {
    }

    public void render(GuiGraphics graphics, TargetHud targetHud, LivingEntity target, int left, int top) {
        int right = left + TargetHud.HUD_WIDTH;
        int bottom = top + TargetHud.HUD_HEIGHT;

        int accentColor = 0xFF000000 | (target != null ? ColorsUtil.getEspColor(target) : 0xFFFFFF);
        float maxHealth = target != null ? Math.max(target.getMaxHealth(), 1.0f) : 20.0f;
        float health = target != null ? target.getHealth() : 20.0f;
        float healthPercent = Mth.clamp(health / maxHealth, 0.0f, 1.0f);
        int barLeft = left + 8;
        int barTop = top + 31;
        int barRight = right - 8;
        int barBottom = barTop + 6;
        int healthWidth = Math.max(0, Math.round((barRight - barLeft) * healthPercent));

        String name = target != null ? target.getName().getString() : "No target";
        float distance = target != null && TargetHud.mc.player != null ? TargetHud.mc.player.distanceTo(target) : 0.0f;

        graphics.fill(left, top, right, bottom, BACKGROUND_COLOR);
        graphics.fill(left, top, left + 2, bottom, accentColor);
        graphics.fill(left, top, right, top + 1, BORDER_COLOR);
        graphics.fill(left, bottom - 1, right, bottom, BORDER_COLOR);
        graphics.fill(left, top, left + 1, bottom, BORDER_COLOR);
        graphics.fill(right - 1, top, right, bottom, BORDER_COLOR);

        graphics.drawString(TargetHud.mc.font, name, left + 8, top + 7, TEXT_COLOR, true);
        graphics.drawString(TargetHud.mc.font, String.format(Locale.ROOT, "HP: %.1f / %.1f", health, maxHealth), left + 8, top + 17, TEXT_COLOR, false);
        graphics.drawString(TargetHud.mc.font, String.format(Locale.ROOT, "Dist: %.1fm", distance), left + 86, top + 17, SUB_TEXT_COLOR, false);

        graphics.fill(barLeft, barTop, barRight, barBottom, HEALTH_BACKGROUND_COLOR);
        graphics.fill(barLeft, barTop, barLeft + healthWidth, barBottom, accentColor);
    }
}