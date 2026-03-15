package com.xybaka.autoaim.modules.render;

import com.xybaka.autoaim.gui.targethud.TargetHudManager;
import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.ModuleManager;
import com.xybaka.autoaim.modules.combat.AutoAim;
import com.xybaka.autoaim.modules.settings.ModeSetting;
import com.xybaka.autoaim.util.TargetUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class TargetHud extends Module {
    public enum StyleMode {
        AUTOAIM
    }

    public static final int HUD_WIDTH = 150;
    public static final int HUD_HEIGHT = 44;

    public final ModeSetting<StyleMode> styleMode = new ModeSetting<>("Style", StyleMode.AUTOAIM);

    private int hudX = 12;
    private int hudY = 12;

    public TargetHud() {
        super("TargetHud", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
        this.enable();
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        if (!isEnabled() || mc.player == null || mc.options.hideGui) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        LivingEntity target = resolveTarget();
        boolean editing = TargetHudManager.isChatEditing();
        if (target != null || editing) {
            renderHud(graphics, target, getHudX(), getHudY());
        }
    }

    public LivingEntity resolveTarget() {
        AutoAim autoAim = ModuleManager.instance.get(AutoAim.class);
        return TargetUtil.getBestTarget(autoAim.range.getValue());
    }

    public void renderHud(GuiGraphics graphics, LivingEntity target, int left, int top) {
        TargetHudManager.renderStyle(styleMode.getValue(), graphics, this, target, left, top);
    }

    public int getHudX() {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        return Mth.clamp(hudX, 0, Math.max(0, screenWidth - HUD_WIDTH));
    }

    public int getHudY() {
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        return Mth.clamp(hudY, 0, Math.max(0, screenHeight - HUD_HEIGHT));
    }

    public int getStoredX() {
        return hudX;
    }

    public int getStoredY() {
        return hudY;
    }

    public void setStoredPosition(int x, int y) {
        this.hudX = x;
        this.hudY = y;
    }

    public void setPosition(int x, int y, int screenWidth, int screenHeight) {
        this.hudX = Mth.clamp(x, 0, Math.max(0, screenWidth - HUD_WIDTH));
        this.hudY = Mth.clamp(y, 0, Math.max(0, screenHeight - HUD_HEIGHT));
    }
}