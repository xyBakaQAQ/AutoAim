package com.xybaka.autoaim.gui;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.ModuleManager;
import com.xybaka.autoaim.modules.settings.BooleanSetting;
import com.xybaka.autoaim.modules.settings.EnumSetting;
import com.xybaka.autoaim.modules.settings.NumberSetting;
import com.xybaka.autoaim.modules.settings.Setting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGuiScreen extends Screen {

    // ────────────────────────── 面板状态 ──────────────────────────────────

    private static final class Panel {
        float x, y;
        float targetX, targetY;
        boolean collapsed = false;
        Module  openModule = null;   // 当前展开设置的模块
        String  openEnum   = null;   // 当前展开的 EnumSetting 名称
        float   slideAnim  = 0f;     // 0→1，设置区滑入进度

        Panel(float x, float y) { this.x = this.targetX = x; this.y = this.targetY = y; }
    }

    // ────────────────────────── 字段 ──────────────────────────────────────

    private final Map<Category, Panel> panels     = new HashMap<>();
    private final Map<Module, Float>   toggleAnim = new HashMap<>();

    private Panel  dragPanel;
    private double dragOffX, dragOffY;

    private NumberSetting dragNumber;
    private float         dragBarX;
    private float         dragBarW;

    // ────────────────────────── 尺寸常量 ──────────────────────────────────

    private static final int PW      = 160;
    private static final int HEADER  = 22;
    private static final int MOD_H   = 22;
    private static final int SET_H   = 28;
    private static final int ENUM_H  = 16;
    private static final int CORNER  = 4;
    private static final int BAR_PAD = 10;
    private static final int BAR_Y   = 18;
    private static final int BAR_H   = 4;
    private static final int DOT_R   = 3;

    // ────────────────────────── 颜色常量 ──────────────────────────────────

    private static final int C_BG       = 0xF0111118;
    private static final int C_SET_BG   = 0xF0181825;
    private static final int C_HDR_L    = 0xFF3A3AFF;
    private static final int C_HDR_R    = 0xFF9B30FF;
    private static final int C_MOD_ON   = 0xFF2A2A4A;
    private static final int C_MOD_OFF  = 0xFF1A1A26;
    private static final int C_HOVER    = 0xFF252540;
    private static final int C_ACCENT   = 0xFF7B6FFF;
    private static final int C_TEXT     = 0xFFE0E0FF;
    private static final int C_DIM      = 0xFF8888AA;
    private static final int C_TRUE     = 0xFF55FF88;
    private static final int C_BAR_BG   = 0xFF252535;
    private static final int C_BAR_FG   = 0xFF7B6FFF;
    private static final int C_ENUM_SEL = 0xFF3A3A60;

    // ────────────────────────── 构造 ──────────────────────────────────────

    public ClickGuiScreen() {
        super(Component.literal("ClickGUI"));
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            panels.put(cats[i], new Panel(20 + i * (PW + 12), 30));
        }
    }

    // ────────────────────────── 工具 ──────────────────────────────────────

    private static float lerp(float a, float b, float t) {
        float d = b - a;
        return Math.abs(d) < 0.3f ? b : a + d * t;
    }

    private static boolean hit(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static int lerpRGB(int a, int b, float t) {
        int aa = (a >> 24) & 0xFF;
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return (aa << 24)
                | ((int)(ar + (br - ar) * t) << 16)
                | ((int)(ag + (bg - ag) * t) << 8)
                |  (int)(ab + (bb - ab) * t);
    }

    private static int withAlpha(int rgb, int a) {
        return (a << 24) | (rgb & 0x00FFFFFF);
    }

    /** 计算展开模块的设置区总高度。 */
    private int calcSettingsH(Panel p, Module m) {
        if (m == null || m.getSettings().isEmpty()) return 0;
        int h = 8;
        for (Setting s : m.getSettings()) {
            if (s instanceof EnumSetting<?> e && s.getName().equals(p.openEnum)) {
                h += 18 + e.getValues().length * ENUM_H + 6;
            } else {
                h += SET_H;
            }
        }
        return h;
    }

    /** 计算面板总高度。 */
    private int calcPanelH(Panel p, List<Module> mods) {
        if (p.collapsed) return HEADER;
        int h = HEADER;
        for (Module m : mods) {
            h += MOD_H;
            if (m == p.openModule) h += Math.round(calcSettingsH(p, m) * p.slideAnim);
        }
        return h;
    }

    // ────────────────────────── 渲染 ──────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0x88000000);

        for (Category cat : Category.values()) {
            Panel p = panels.get(cat);
            p.x = lerp(p.x, p.targetX, 0.25f);
            p.y = lerp(p.y, p.targetY, 0.25f);
            renderPanel(g, cat, p, mx, my);
        }

        if (dragPanel != null) {
            dragPanel.targetX = (float)(mx - dragOffX);
            dragPanel.targetY = (float)(my - dragOffY);
        }
        if (dragNumber != null) applyDragNumber(mx);

        super.render(g, mx, my, pt);
    }

    private void renderPanel(GuiGraphics g, Category cat, Panel p, int mx, int my) {
        List<Module> mods = ModuleManager.instance.getModulesByCategory(cat);
        float px = p.x, py = p.y;

        fillRounded(g, px, py, PW, calcPanelH(p, mods), C_BG);
        fillGradientH(g, px, py, PW, HEADER, C_HDR_L, C_HDR_R);
        g.drawString(font, cat.name() + (p.collapsed ? " ▶" : " ▼"), (int)px + 8, (int)py + 7, C_TEXT);

        if (p.collapsed) return;

        if (p.openModule == null) p.slideAnim = lerp(p.slideAnim, 0f, 0.25f);

        float curY = py + HEADER;
        for (Module m : mods) {
            curY = renderModuleRow(g, p, m, px, curY, mx, my);
        }
    }

    /** 渲染模块行及其下方内联设置，返回下一行 Y。 */
    private float renderModuleRow(GuiGraphics g, Panel p, Module m, float px, float curY, int mx, int my) {
        // 开关动画
        float anim = lerp(toggleAnim.getOrDefault(m, m.isEnabled() ? 1f : 0f),
                m.isEnabled() ? 1f : 0f, 0.2f);
        toggleAnim.put(m, anim);

        boolean hov      = hit(mx, my, px, curY, PW, MOD_H);
        boolean expanded = m == p.openModule;

        // 背景
        int bg = lerpRGB(C_MOD_OFF, C_MOD_ON, anim);
        if (hov && !expanded) bg = lerpRGB(bg, C_HOVER, 0.5f);
        if (expanded)         bg = lerpRGB(bg, C_ACCENT, 0.10f);
        g.fill((int)px, (int)curY, (int)(px + PW), (int)(curY + MOD_H), bg);

        // 左侧启用光条
        if (anim > 0.01f)
            g.fill((int)px, (int)curY, (int)px + 3, (int)(curY + MOD_H),
                    withAlpha(C_ACCENT, (int)(anim * 255)));

        // 展开底部分隔线
        if (expanded)
            g.fill((int)px + 3, (int)(curY + MOD_H - 1), (int)(px + PW), (int)(curY + MOD_H),
                    withAlpha(C_ACCENT, 100));

        // 文字
        g.drawString(font, m.getName(), (int)px + 10, (int)curY + 7,
                m.isEnabled() ? C_TEXT : C_DIM);
        if (!m.getSettings().isEmpty())
            g.drawString(font, expanded ? "▲" : "▼", (int)(px + PW - 14), (int)curY + 7,
                    expanded ? C_ACCENT : C_DIM);

        curY += MOD_H;

        // 内联设置区
        if (expanded && !m.getSettings().isEmpty()) {
            p.slideAnim = lerp(p.slideAnim, 1f, 0.18f);
            int fullH = calcSettingsH(p, m);
            int visH  = Math.round(fullH * p.slideAnim);
            if (visH > 0) {
                g.fill((int)px, (int)curY, (int)(px + PW), (int)(curY + visH), C_SET_BG);
                g.fill((int)px, (int)curY, (int)px + 2, (int)(curY + visH), C_ACCENT);
                g.enableScissor((int)px, (int)curY, (int)(px + PW), (int)(curY + visH));
                renderSettings(g, p, m, px, curY, mx, my);
                g.disableScissor();
                curY += visH;
            }
        }

        return curY;
    }

    private void renderSettings(GuiGraphics g, Panel p, Module m, float px, float sy, int mx, int my) {
        float ry = sy + 4;
        for (Setting s : m.getSettings()) {
            if (s instanceof BooleanSetting b) {
                boolean hov = hit(mx, my, px + 2, ry, PW - 4, SET_H);
                if (hov) g.fill((int)(px + 2), (int)ry, (int)(px + PW - 2), (int)(ry + SET_H), 0x22FFFFFF);
                g.drawString(font, s.getName(), (int)px + 10, (int)ry + 6, C_TEXT);
                drawToggle(g, px + PW - 34, ry + (SET_H - 10) / 2f, b.isEnabled());
                ry += SET_H;

            } else if (s instanceof NumberSetting n) {
                g.drawString(font, n.getName() + ": " + String.format("%.2f", n.getValue()),
                        (int)px + BAR_PAD, (int)ry + 5, C_TEXT);
                float bx = px + BAR_PAD, bw = PW - BAR_PAD * 2;
                float prog = (float)((n.getValue() - n.getMin()) / (n.getMax() - n.getMin()));
                int by = (int)(ry + BAR_Y);
                g.fill((int)bx, by, (int)(bx + bw), by + BAR_H, C_BAR_BG);
                g.fill((int)bx, by, (int)(bx + bw * prog), by + BAR_H, C_BAR_FG);
                int dotX = (int)(bx + bw * prog);
                g.fill(dotX - DOT_R, by - 2, dotX + DOT_R, by + BAR_H + 2, C_ACCENT);
                ry += SET_H;

            } else if (s instanceof EnumSetting<?> e) {
                boolean open   = s.getName().equals(p.openEnum);
                boolean hov    = hit(mx, my, px + 2, ry, PW - 4, 18);
                if (hov) g.fill((int)(px + 2), (int)ry, (int)(px + PW - 2), (int)(ry + 18), 0x22FFFFFF);
                g.drawString(font, s.getName() + ": " + e.getDisplayName() + (open ? " ▲" : " ▼"),
                        (int)px + 10, (int)ry + 5, open ? C_ACCENT : C_DIM);
                ry += 18;
                if (open) {
                    for (Object val : e.getValues()) {
                        boolean sel    = val == e.getValue();
                        boolean rowHov = hit(mx, my, px + 4, ry, PW - 8, ENUM_H);
                        g.fill((int)(px + 4), (int)ry, (int)(px + PW - 4), (int)(ry + ENUM_H),
                                sel ? C_ENUM_SEL : (rowHov ? 0xFF1E1E30 : 0xFF161622));
                        if (sel) g.fill((int)(px + 4), (int)ry, (int)(px + 6), (int)(ry + ENUM_H), C_ACCENT);
                        g.drawString(font, val.toString(), (int)(px + 12), (int)ry + 4, sel ? C_TEXT : C_DIM);
                        ry += ENUM_H;
                    }
                    ry += 6;
                }
            }
        }
    }

    private void drawToggle(GuiGraphics g, float x, float y, boolean on) {
        g.fill((int)x, (int)y, (int)x + 22, (int)y + 10, on ? C_TRUE : 0xFF444455);
        float kx = on ? x + 13 : x + 1;
        g.fill((int)kx, (int)y + 1, (int)kx + 8, (int)y + 9, 0xFFEEEEFF);
    }

    // ────────────────────────── 鼠标事件 ──────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        for (Category cat : Category.values()) {
            Panel p = panels.get(cat);
            List<Module> mods = ModuleManager.instance.getModulesByCategory(cat);

            // 标题栏
            if (hit(mx, my, p.x, p.y, PW, HEADER)) {
                if (btn == 1) {
                    p.collapsed = !p.collapsed;
                    if (p.collapsed) { p.openModule = null; p.slideAnim = 0; }
                } else if (btn == 0) {
                    dragPanel = p;
                    dragOffX  = mx - p.targetX;
                    dragOffY  = my - p.targetY;
                }
                return true;
            }

            if (p.collapsed) continue;

            float curY = p.y + HEADER;
            for (Module m : mods) {
                if (hit(mx, my, p.x, curY, PW, MOD_H)) {
                    if (btn == 0) m.toggle();         // 左键：切换开关
                    if (btn == 1) toggleSettings(p, m); // 右键：展开设置
                    return true;
                }
                curY += MOD_H;

                if (m == p.openModule && !m.getSettings().isEmpty()) {
                    int visH = Math.round(calcSettingsH(p, m) * p.slideAnim);
                    if (visH > 0 && hit(mx, my, p.x, curY, PW, visH)) {
                        handleSettingsClick(p, m, p.x, curY, mx, my);
                        return true;
                    }
                    curY += visH;
                }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    private void toggleSettings(Panel p, Module m) {
        if (m.getSettings().isEmpty()) return;
        if (p.openModule == m) {
            p.openModule = null;
            p.slideAnim  = 0;
            p.openEnum   = null;
        } else {
            p.openModule = m;
            p.slideAnim  = 0;
            p.openEnum   = null;
        }
    }

    private void handleSettingsClick(Panel p, Module m, float px, float sy, double mx, double my) {
        float ry = sy + 4;
        for (Setting s : m.getSettings()) {
            if (s instanceof BooleanSetting b) {
                if (hit(mx, my, px + 2, ry, PW - 4, SET_H)) { b.toggle(); return; }
                ry += SET_H;
            } else if (s instanceof NumberSetting n) {
                float bx = px + BAR_PAD, bw = PW - BAR_PAD * 2;
                if (hit(mx, my, bx, ry + BAR_Y - 6, bw, BAR_H + 12)) {
                    dragNumber = n;
                    dragBarX   = bx;
                    dragBarW   = bw;
                    applyDragNumber(mx);
                    return;
                }
                ry += SET_H;
            } else if (s instanceof EnumSetting<?> e) {
                if (hit(mx, my, px + 2, ry, PW - 4, 18)) {
                    p.openEnum = s.getName().equals(p.openEnum) ? null : s.getName();
                    return;
                }
                ry += 18;
                if (s.getName().equals(p.openEnum)) {
                    for (int i = 0; i < e.getValues().length; i++) {
                        if (hit(mx, my, px + 4, ry, PW - 8, ENUM_H)) { setEnum(e, i); return; }
                        ry += ENUM_H;
                    }
                    ry += 6;
                }
            }
        }
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        dragPanel  = null;
        dragNumber = null;
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (dragNumber != null) { applyDragNumber(mx); return true; }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    private void applyDragNumber(double mx) {
        if (dragNumber == null) return;
        double prog = Math.min(1, Math.max(0, (mx - dragBarX) / dragBarW));
        dragNumber.setValue(dragNumber.getMin() + prog * (dragNumber.getMax() - dragNumber.getMin()));
    }

    // ────────────────────────── 绘图工具 ──────────────────────────────────

    private void fillRounded(GuiGraphics g, float x, float y, float w, float h, int c) {
        g.fill((int)(x + CORNER), (int)y,             (int)(x + w - CORNER), (int)(y + h),            c);
        g.fill((int)x,            (int)(y + CORNER),   (int)(x + CORNER),     (int)(y + h - CORNER),   c);
        g.fill((int)(x + w - CORNER), (int)(y + CORNER), (int)(x + w),        (int)(y + h - CORNER),   c);
    }

    private void fillGradientH(GuiGraphics g, float x, float y, float w, float h, int c1, int c2) {
        int steps = (int)w;
        for (int i = 0; i < steps; i++) {
            g.fill((int)(x + i), (int)y, (int)(x + i + 1), (int)(y + h),
                    lerpRGB(0xFF000000 | (c1 & 0xFFFFFF), 0xFF000000 | (c2 & 0xFFFFFF), (float)i / steps));
        }
    }

    private static <T extends Enum<T>> void setEnum(EnumSetting<T> s, int i) {
        s.setValue(s.getValues()[i]);
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == 256) { onClose(); return true; }
        return super.keyPressed(key, scan, mods);
    }
}