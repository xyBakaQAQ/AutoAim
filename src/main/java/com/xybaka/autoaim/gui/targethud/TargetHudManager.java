package com.xybaka.autoaim.gui.targethud;

import com.xybaka.autoaim.gui.targethud.style.AutoAimStyle;
import com.xybaka.autoaim.modules.ModuleManager;
import com.xybaka.autoaim.modules.render.TargetHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.entity.LivingEntity;

public final class TargetHudManager {
    private static boolean dragging;
    private static boolean dirty;
    private static int dragOffsetX;
    private static int dragOffsetY;

    private TargetHudManager() {
    }

    public static boolean isChatEditing() {
        return Minecraft.getInstance().screen instanceof ChatScreen;
    }

    public static void renderStyle(TargetHud.StyleMode styleMode, net.minecraft.client.gui.GuiGraphics graphics, TargetHud targetHud, LivingEntity target, int left, int top) {
        switch (styleMode) {
            case AUTOAIM -> AutoAimStyle.INSTANCE.render(graphics, targetHud, target, left, top);
        }
    }

    public static boolean handleMouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isChatEditing()) {
            return false;
        }

        TargetHud targetHud = ModuleManager.instance.get(TargetHud.class);
        if (targetHud == null) {
            return false;
        }

        int left = targetHud.getHudX();
        int top = targetHud.getHudY();
        boolean hit = mouseX >= left && mouseX <= left + TargetHud.HUD_WIDTH
                && mouseY >= top && mouseY <= top + TargetHud.HUD_HEIGHT;
        if (!hit) {
            return false;
        }

        dragging = true;
        dragOffsetX = (int) mouseX - left;
        dragOffsetY = (int) mouseY - top;
        return true;
    }

    public static boolean handleMouseDragged(double mouseX, double mouseY, int button) {
        if (!dragging || button != 0) {
            return false;
        }

        TargetHud targetHud = ModuleManager.instance.get(TargetHud.class);
        Minecraft mc = Minecraft.getInstance();
        if (targetHud == null || mc.screen == null) {
            return false;
        }

        targetHud.setPosition((int) mouseX - dragOffsetX, (int) mouseY - dragOffsetY, mc.screen.width, mc.screen.height);
        dirty = true;
        return true;
    }

    public static boolean handleMouseReleased(int button) {
        if (button != 0 || !dragging) {
            return false;
        }

        dragging = false;
        saveIfDirty();
        return true;
    }

    public static void onChatClosed() {
        dragging = false;
        saveIfDirty();
    }

    private static void saveIfDirty() {
        if (!dirty) {
            return;
        }
        dirty = false;
        ModuleManager.instance.saveConfig();
    }
}