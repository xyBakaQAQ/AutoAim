package com.xybaka.autoaim.modules.render;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.ModuleManager;
import com.xybaka.autoaim.modules.settings.BooleanSetting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HUD extends Module {

    public final BooleanSetting showNotifications = new BooleanSetting("Notifications", true);
    public final BooleanSetting showInfo = new BooleanSetting("Info", true);

    public HUD() {
        super("HUD", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
        this.enable();
    }

    private static class Notification {
        final String moduleName;
        final boolean enabled;
        final long    createTime  = System.currentTimeMillis();
        final long    duration    = 2500;   // 总显示时长 ms
        final float   fadeInTime  = 300f;   // 淡入时长  ms
        final float   fadeOutTime = 500f;   // 淡出时长  ms

        Notification(String moduleName, boolean enabled) {
            this.moduleName = moduleName;
            this.enabled = enabled;
        }

        float getAlpha() {
            long elapsed = System.currentTimeMillis() - createTime;
            if (elapsed < fadeInTime) {
                return elapsed / fadeInTime;
            }
            long fadeOutStart = duration - (long) fadeOutTime;
            if (elapsed > fadeOutStart) {
                return Math.max(0f, 1f - (elapsed - fadeOutStart) / fadeOutTime);
            }
            return 1f;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createTime > duration;
        }
    }

    private static final List<Notification> NOTIFICATIONS = new ArrayList<>();

    public static void push(String moduleName, boolean nowEnabled) {
        synchronized (NOTIFICATIONS) {
            NOTIFICATIONS.add(new Notification(moduleName, nowEnabled));
        }
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        if (!this.isEnabled() || mc.options.hideGui || mc.player == null) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        drawModuleList(graphics, screenW);

        if (showInfo.isEnabled()) {
            drawInfo(graphics, screenH);
        }

        if (showNotifications.isEnabled()) {
            drawNotifications(graphics, screenW, screenH);
        }
    }

    private void drawModuleList(GuiGraphics graphics, int screenW) {
        List<Module> active = ModuleManager.instance.getModules().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt(m -> -mc.font.width(m.getName())))
                .toList();

        int yOffset = 5;
        for (Module m : active) {
            String text = m.getName();
            int textWidth = mc.font.width(text);
            int x = screenW - textWidth - 5;
            int color = getRainbowColor(yOffset * 20);

            graphics.drawString(mc.font, text, x, yOffset, color, true);
            yOffset += mc.font.lineHeight + 2;
        }
    }

    private void drawNotifications(GuiGraphics graphics, int screenW, int screenH) {
        final int padding = 5;
        final int barWidth = 140;
        final int barHeight = mc.font.lineHeight + padding * 2;
        final int gap = 4;

        synchronized (NOTIFICATIONS) {
            NOTIFICATIONS.removeIf(Notification::isExpired);
            if (NOTIFICATIONS.isEmpty()) {
                return;
            }

            int baseY = screenH - barHeight - 10;

            for (int i = 0; i < NOTIFICATIONS.size(); i++) {
                Notification notification = NOTIFICATIONS.get(i);
                int alpha = (int) (notification.getAlpha() * 255);

                int x = screenW - barWidth - 10;
                int y = baseY - i * (barHeight + gap);

                int bgColor = withAlpha(notification.enabled ? 0x2A5C2A : 0x5C2A2A, alpha);
                int accentColor = withAlpha(notification.enabled ? 0x55FF55 : 0xFF5555, alpha);

                graphics.fill(x, y, x + barWidth, y + barHeight, bgColor);
                graphics.fill(x, y, x + 3, y + barHeight, accentColor);

                String label = (notification.enabled ? "On " : "Off ") + notification.moduleName;
                graphics.drawString(mc.font, label, x + 8, y + padding, withAlpha(0xFFFFFF, alpha), true);
            }
        }
    }

    private void drawInfo(GuiGraphics graphics, int screenH) {
        final int padding = 5;
        final int textColor = 0xFFFFFF;

        String xyz = String.format("XYZ: %.1f, %.1f, %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
        int yXyz = screenH - mc.font.lineHeight - padding;
        graphics.drawString(mc.font, xyz, padding, yXyz, textColor, true);

        double deltaX = mc.player.getX() - mc.player.xo;
        double deltaZ = mc.player.getZ() - mc.player.zo;
        double speed = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) * 20.0D;
        String speedText = String.format("Speed: %.2f m/s", speed);
        int ySpeed = yXyz - mc.font.lineHeight - padding;
        graphics.drawString(mc.font, speedText, padding, ySpeed, textColor, true);

        String fpsText = String.format("FPS: %d", mc.getFps());
        int yFps = ySpeed - mc.font.lineHeight - padding;
        graphics.drawString(mc.font, fpsText, padding, yFps, textColor, true);
    }

    private int getRainbowColor(int offset) {
        float speed = 3000f;
        float hue = (System.currentTimeMillis() + offset) % (int) speed / speed;
        return Color.HSBtoRGB(hue, 0.6f, 1f);
    }

    private static int withAlpha(int rgb, int alpha) {
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }
}
