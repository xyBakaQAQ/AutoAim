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

    public HUD() {
        super("HUD", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
        this.enable();
    }

    private static class Notification {
        final String  moduleName;
        final boolean enabled;
        final long    createTime  = System.currentTimeMillis();
        final long    duration    = 2500;   // 总显示时长 ms
        final float   fadeInTime  = 300f;   // 淡入时长  ms
        final float   fadeOutTime = 500f;   // 淡出时长  ms

        Notification(String moduleName, boolean enabled) {
            this.moduleName = moduleName;
            this.enabled    = enabled;
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
        if (!this.isEnabled() || mc.options.hideGui || mc.player == null) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int screenW          = mc.getWindow().getGuiScaledWidth();
        int screenH          = mc.getWindow().getGuiScaledHeight();

        drawModuleList(graphics, screenW);

        if (showNotifications.isEnabled()) {
            drawNotifications(graphics, screenW, screenH);
        }
    }

    // ── 右侧模块列表（彩虹色） ────────────────────────────────────────────────
    private void drawModuleList(GuiGraphics graphics, int screenW) {
        List<Module> active = ModuleManager.instance.getModules().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt(m -> -mc.font.width(m.getName())))
                .toList();

        int yOffset = 5;
        for (Module m : active) {
            String text      = m.getName();
            int    textWidth = mc.font.width(text);
            int    x         = screenW - textWidth - 5;
            int    color     = getRainbowColor(yOffset * 20);

            graphics.drawString(mc.font, text, x, yOffset, color, true);
            yOffset += mc.font.lineHeight + 2;
        }
    }

    // ── 右下角通知栏 ──────────────────────────────────────────────────────────
    private void drawNotifications(GuiGraphics graphics, int screenW, int screenH) {
        final int PADDING    = 5;
        final int BAR_WIDTH  = 140;
        final int BAR_HEIGHT = mc.font.lineHeight + PADDING * 2;
        final int GAP        = 4;

        synchronized (NOTIFICATIONS) {
            NOTIFICATIONS.removeIf(Notification::isExpired);
            if (NOTIFICATIONS.isEmpty()) return;

            // 从底部向上堆叠，最新的通知在最下方
            int baseY = screenH - BAR_HEIGHT - 10;

            for (int i = 0; i < NOTIFICATIONS.size(); i++) {
                Notification n = NOTIFICATIONS.get(i);
                int a = (int)(n.getAlpha() * 255);

                int x = screenW - BAR_WIDTH - 10;
                int y = baseY - i * (BAR_HEIGHT + GAP);

                // 背景色：开启 → 深绿，关闭 → 深红
                int bgColor     = withAlpha(n.enabled ? 0x2A5C2A : 0x5C2A2A, a);
                // 左侧强调竖条：开启 → 亮绿，关闭 → 亮红
                int accentColor = withAlpha(n.enabled ? 0x55FF55 : 0xFF5555, a);

                // 背景矩形
                graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, bgColor);
                // 左侧竖条（3px 宽）
                graphics.fill(x, y, x + 3, y + BAR_HEIGHT, accentColor);

                // 文字：[+]/[-] + 模块名
                String label = (n.enabled ? "§a[+] " : "§c[-] ") + "§f" + n.moduleName;
                graphics.drawString(mc.font, label, x + 8, y + PADDING, withAlpha(0xFFFFFF, a), true);
            }
        }
    }

    // ── 工具方法 ──────────────────────────────────────────────────────────────
    private int getRainbowColor(int offset) {
        float speed = 3000f;
        float hue   = (System.currentTimeMillis() + offset) % (int) speed / speed;
        return Color.HSBtoRGB(hue, 0.6f, 1f);
    }

    private static int withAlpha(int rgb, int a) {
        return (a << 24) | (rgb & 0x00FFFFFF);
    }
}