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

    // ── 每个分类面板的状态 ──────────────────────────────────────
    private static class Panel {
        float x, y;           // 当前渲染坐标（平滑跟随）
        float targetX, targetY;
        boolean collapsed = false;
        Module expandedModule = null;  // 当前展开设置的模块
        String openedEnumName = null;  // 记住展开的 EnumSetting
        float settingsSlide = 0f;      // 设置面板滑入进度 0→1

        Panel(float x, float y) {
            this.x = this.targetX = x;
            this.y = this.targetY = y;
        }
    }

    // ── 模块开关动画 ─────────────────────────────────────────────
    private final Map<Module, Float> toggleAnim = new HashMap<>(); // 0→1

    // ── 面板数据 ─────────────────────────────────────────────────
    private final Map<Category, Panel> panels = new HashMap<>();

    // ── 拖动状态 ─────────────────────────────────────────────────
    private Panel draggingPanel = null;
    private double dragOffX, dragOffY;
    private NumberSetting draggingNumber = null;
    private Panel draggingNumberPanel = null;

    // ── 尺寸常量 ─────────────────────────────────────────────────
    private static final int PANEL_W      = 160;
    private static final int HEADER_H     = 22;
    private static final int MODULE_H     = 22;
    private static final int SETTING_H    = 28;
    private static final int ENUM_ROW_H   = 16;
    private static final int SETTINGS_W   = 160;
    private static final int CORNER       = 4;

    // ── 颜色 ─────────────────────────────────────────────────────
    private static final int C_BG         = 0xF0111118;
    private static final int C_HEADER1    = 0xFF3A3AFF; // 渐变起点（蓝）
    private static final int C_HEADER2    = 0xFF9B30FF; // 渐变终点（紫）
    private static final int C_MODULE_ON  = 0xFF2A2A4A;
    private static final int C_MODULE_OFF = 0xFF1A1A26;
    private static final int C_HOVER      = 0xFF252540;
    private static final int C_ACCENT     = 0xFF7B6FFF;
    private static final int C_TEXT       = 0xFFE0E0FF;
    private static final int C_TEXT_DIM   = 0xFF8888AA;
    private static final int C_TRUE       = 0xFF55FF88;
    private static final int C_FALSE      = 0xFFFF5566;
    private static final int C_BAR_BG     = 0xFF252535;
    private static final int C_BAR_FG     = 0xFF7B6FFF;
    private static final int C_ENUM_SEL   = 0xFF3A3A60;

    public ClickGuiScreen() {
        super(Component.literal("ClickGUI"));
        // 初始化每个分类面板的位置
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            panels.put(cats[i], new Panel(20 + i * (PANEL_W + 12), 30));
        }
    }

    // ── 平滑插值 ─────────────────────────────────────────────────
    private float lerp(float a, float b, float t) {
        float d = b - a;
        return Math.abs(d) < 0.3f ? b : a + d * t;
    }

    // ── 主渲染 ───────────────────────────────────────────────────
    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        // 半透明背景遮罩
        g.fill(0, 0, this.width, this.height, 0x88000000);

        for (Category cat : Category.values()) {
            Panel p = panels.get(cat);
            p.x = lerp(p.x, p.targetX, 0.25f);
            p.y = lerp(p.y, p.targetY, 0.25f);
            renderPanel(g, cat, p, mx, my);
        }

        if (draggingPanel != null) {
            draggingPanel.targetX = (float)(mx - dragOffX);
            draggingPanel.targetY = (float)(my - dragOffY);
        }
        if (draggingNumber != null) updateNumber(mx, draggingNumberPanel);

        super.render(g, mx, my, pt);
    }

    private void renderPanel(GuiGraphics g, Category cat, Panel p, int mx, int my) {
        List<Module> modules = ModuleManager.instance.getModulesByCategory(cat);
        float px = p.x, py = p.y;

        // 计算面板高度
        int modCount = p.collapsed ? 0 : modules.size();
        int panelH = HEADER_H + modCount * MODULE_H;

        // 面板背景
        fillRounded(g, px, py, PANEL_W, panelH, C_BG);

        // 标题栏渐变
        fillRoundedTop(g, px, py, PANEL_W, HEADER_H, C_HEADER1, C_HEADER2);

        // 标题文字
        String title = cat.name() + (p.collapsed ? " ▶" : " ▼");
        g.drawString(font, title, (int)px + 8, (int)py + 7, C_TEXT);

        if (p.collapsed) return;

        // 模块列表
        float my2 = py + HEADER_H;
        for (Module m : modules) {
            float animVal = toggleAnim.getOrDefault(m, m.isEnabled() ? 1f : 0f);
            float targetAnim = m.isEnabled() ? 1f : 0f;
            toggleAnim.put(m, lerp(animVal, targetAnim, 0.2f));

            boolean hovered = isIn(mx, my, px, my2, PANEL_W, MODULE_H);
            boolean expanded = m == p.expandedModule;

            // 模块背景（开关动画混色）
            int bgColor = lerpColor(C_MODULE_OFF, C_MODULE_ON, animVal);
            if (hovered) bgColor = lerpColor(bgColor, C_HOVER, 0.5f);
            g.fill((int)px, (int)my2, (int)(px + PANEL_W), (int)(my2 + MODULE_H), bgColor);

            // 开启状态左侧光条
            if (animVal > 0.01f) {
                int barAlpha = (int)(animVal * 255);
                g.fill((int)px, (int)my2, (int)px + 3, (int)(my2 + MODULE_H),
                        (barAlpha << 24) | (C_ACCENT & 0x00FFFFFF));
            }

            // 展开指示
            if (expanded) {
                g.fill((int)(px + PANEL_W - 3), (int)my2, (int)(px + PANEL_W), (int)(my2 + MODULE_H), C_ACCENT);
            }

            // 模块名
            int nameColor = m.isEnabled() ? C_TEXT : C_TEXT_DIM;
            g.drawString(font, m.getName(), (int)px + 10, (int)my2 + 7, nameColor);

            // 有设置时显示箭头
            if (!m.getSettings().isEmpty()) {
                String arrow = expanded ? "◀" : "▷";
                g.drawString(font, arrow, (int)(px + PANEL_W - 14), (int)my2 + 7, C_TEXT_DIM);
            }

            my2 += MODULE_H;
        }

        // 右侧设置面板
        if (p.expandedModule != null && !p.expandedModule.getSettings().isEmpty()) {
            p.settingsSlide = lerp(p.settingsSlide, 1f, 0.18f);
            float slideOffset = (1f - p.settingsSlide) * 20f;
            int alpha = (int)(p.settingsSlide * 255);
            renderSettings(g, p, px + PANEL_W + 4 + slideOffset, py, alpha, mx, my);
        } else {
            p.settingsSlide = lerp(p.settingsSlide, 0f, 0.2f);
        }
    }

    private void renderSettings(GuiGraphics g, Panel p, float sx, float sy, int alpha, int mx, int my) {
        Module m = p.expandedModule;
        List<Setting> settings = m.getSettings();

        // 计算设置面板高度
        int sh = 28;
        for (Setting s : settings) {
            sh += SETTING_H;
            if (s instanceof EnumSetting<?> e && s.getName().equals(p.openedEnumName)) {
                sh += e.getValues().length * ENUM_ROW_H + 6;
            }
        }

        // 背景
        int bgAlpha = (alpha * 0xF0 / 255);
        fillRounded(g, sx, sy, SETTINGS_W, sh, (bgAlpha << 24) | 0x111118);

        // 标题
        g.drawString(font, m.getName(), (int)sx + 8, (int)sy + 8,
                (alpha << 24) | (C_ACCENT & 0x00FFFFFF));

        float ry = sy + 26;

        for (Setting s : settings) {
            if (s instanceof BooleanSetting b) {
                boolean hov = isIn(mx, my, sx, ry, SETTINGS_W, SETTING_H);
                if (hov) g.fill((int)sx, (int)ry, (int)(sx + SETTINGS_W), (int)(ry + SETTING_H), 0x22FFFFFF);

                // 名称
                g.drawString(font, s.getName(), (int)sx + 8, (int)ry + 6,
                        (alpha << 24) | (C_TEXT & 0x00FFFFFF));

                // 开关胶囊
                drawToggle(g, sx + SETTINGS_W - 30, ry + 7, b.isEnabled(), alpha);
                ry += SETTING_H;
            }
            else if (s instanceof NumberSetting n) {
                g.drawString(font, n.getName() + ": " + String.format("%.1f", n.getValue()),
                        (int)sx + 8, (int)ry + 5, (alpha << 24) | (C_TEXT & 0x00FFFFFF));

                // 进度条
                float bx = sx + 8, bw = SETTINGS_W - 16;
                g.fill((int)bx, (int)ry + 18, (int)(bx + bw), (int)ry + 22,
                        (alpha / 2 << 24) | (C_BAR_BG & 0x00FFFFFF));
                float prog = (float)((n.getValue() - n.getMin()) / (n.getMax() - n.getMin()));
                g.fill((int)bx, (int)ry + 18, (int)(bx + bw * prog), (int)ry + 22,
                        (alpha << 24) | (C_BAR_FG & 0x00FFFFFF));
                // 拖动点
                g.fill((int)(bx + bw * prog) - 2, (int)ry + 16, (int)(bx + bw * prog) + 2, (int)ry + 24,
                        (alpha << 24) | (C_ACCENT & 0x00FFFFFF));
                ry += SETTING_H;
            }
            else if (s instanceof EnumSetting<?> e) {
                boolean isOpen = s.getName().equals(p.openedEnumName);
                boolean hov = isIn(mx, my, sx, ry, SETTINGS_W, 18);
                if (hov) g.fill((int)sx, (int)ry, (int)(sx + SETTINGS_W), (int)(ry + 18), 0x22FFFFFF);

                int hColor = isOpen ? (C_ACCENT & 0x00FFFFFF) : (C_TEXT_DIM & 0x00FFFFFF);
                g.drawString(font, s.getName() + ": §r" + e.getDisplayName() + (isOpen ? " ▲" : " ▼"),
                        (int)sx + 8, (int)ry + 5, (alpha << 24) | hColor);
                ry += 18;

                if (isOpen) {
                    for (Object val : e.getValues()) {
                        boolean sel = val == e.getValue();
                        boolean rowHov = isIn(mx, my, sx + 4, ry, SETTINGS_W - 8, ENUM_ROW_H);
                        int rowBg = sel ? C_ENUM_SEL : (rowHov ? 0xFF1E1E30 : 0xFF161622);
                        g.fill((int)sx + 4, (int)ry, (int)(sx + SETTINGS_W - 4), (int)(ry + ENUM_ROW_H), rowBg);
                        if (sel) g.fill((int)sx + 4, (int)ry, (int)sx + 7, (int)(ry + ENUM_ROW_H), C_ACCENT);
                        int valColor = sel ? (C_TEXT & 0x00FFFFFF) : (C_TEXT_DIM & 0x00FFFFFF);
                        g.drawString(font, val.toString(), (int)sx + 12, (int)ry + 4,
                                (alpha << 24) | valColor);
                        ry += ENUM_ROW_H;
                    }
                    ry += 6;
                }
            }
        }
    }

    // ── 胶囊开关绘制 ─────────────────────────────────────────────
    private void drawToggle(GuiGraphics g, float x, float y, boolean on, int alpha) {
        int bg = on ? ((alpha << 24) | (C_TRUE & 0x00FFFFFF)) : ((alpha << 24) | 0x444455);
        g.fill((int)x, (int)y, (int)x + 22, (int)y + 10, bg);
        float knobX = on ? x + 13 : x + 1;
        g.fill((int)knobX, (int)y + 1, (int)knobX + 8, (int)y + 9,
                (alpha << 24) | 0xEEEEFF);
    }

    // ── 鼠标点击 ─────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        // 先检查设置面板点击（覆盖在面板右侧）
        for (Category cat : Category.values()) {
            Panel p = panels.get(cat);
            if (p.expandedModule == null) continue;
            float sx = p.x + PANEL_W + 4;
            float sy = p.y;
            if (handleSettingsClick(p, sx, sy, mx, my)) return true;
        }

        for (Category cat : Category.values()) {
            Panel p = panels.get(cat);
            List<Module> modules = ModuleManager.instance.getModulesByCategory(cat);
            int panelH = HEADER_H + (p.collapsed ? 0 : modules.size() * MODULE_H);

            // 标题栏拖动 / 折叠
            if (isIn(mx, my, p.x, p.y, PANEL_W, HEADER_H)) {
                if (btn == 1) {
                    p.collapsed = !p.collapsed;
                    if (p.collapsed) { p.expandedModule = null; p.settingsSlide = 0; }
                } else {
                    draggingPanel = p;
                    dragOffX = mx - p.targetX;
                    dragOffY = my - p.targetY;
                }
                return true;
            }

            if (p.collapsed) continue;

            // 模块行
            float my2 = p.y + HEADER_H;
            for (Module m : modules) {
                if (isIn(mx, my, p.x, my2, PANEL_W, MODULE_H)) {
                    if (btn == 0) m.toggle();
                    if (btn == 1 || btn == 0) {
                        if (p.expandedModule == m) {
                            p.expandedModule = null;
                            p.settingsSlide = 0;
                        } else if (!m.getSettings().isEmpty()) {
                            p.expandedModule = m;
                            p.settingsSlide = 0;
                        }
                    }
                    return true;
                }
                my2 += MODULE_H;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    private boolean handleSettingsClick(Panel p, float sx, float sy, double mx, double my) {
        Module m = p.expandedModule;
        float ry = sy + 26;

        for (Setting s : m.getSettings()) {
            if (s instanceof BooleanSetting b) {
                if (isIn(mx, my, sx, ry, SETTINGS_W, SETTING_H)) {
                    b.toggle(); return true;
                }
                ry += SETTING_H;
            }
            else if (s instanceof NumberSetting n) {
                if (isIn(mx, my, sx + 8, ry + 14, SETTINGS_W - 16, 12)) {
                    draggingNumber = n;
                    draggingNumberPanel = p;
                    return true;
                }
                ry += SETTING_H;
            }
            else if (s instanceof EnumSetting<?> e) {
                if (isIn(mx, my, sx, ry, SETTINGS_W, 18)) {
                    p.openedEnumName = s.getName().equals(p.openedEnumName) ? null : s.getName();
                    return true;
                }
                ry += 18;
                if (s.getName().equals(p.openedEnumName)) {
                    for (int i = 0; i < e.getValues().length; i++) {
                        if (isIn(mx, my, sx + 4, ry, SETTINGS_W - 8, ENUM_ROW_H)) {
                            setEnumValue(e, i); return true;
                        }
                        ry += ENUM_ROW_H;
                    }
                    ry += 6;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        draggingPanel = null;
        draggingNumber = null;
        draggingNumberPanel = null;
        return super.mouseReleased(mx, my, btn);
    }

    private void updateNumber(double mx, Panel p) {
        if (draggingNumber == null || p == null) return;
        float bx = p.x + PANEL_W + 4 + 8;
        float bw = SETTINGS_W - 16;
        double prog = Math.min(1, Math.max(0, (mx - bx) / bw));
        double val = draggingNumber.getMin() + prog * (draggingNumber.getMax() - draggingNumber.getMin());
        draggingNumber.setValue(val);
    }

    // ── 工具方法 ─────────────────────────────────────────────────
    private boolean isIn(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @SuppressWarnings("unchecked")
    private <T extends Enum<T>> void setEnumValue(EnumSetting<T> s, int i) {
        s.setValue(s.getValues()[i]);
    }

    private int lerpColor(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int rr = (int)(ar + (br - ar) * t);
        int rg = (int)(ag + (bg - ag) * t);
        int rb = (int)(ab + (bb - ab) * t);
        int aa = ((a >> 24) & 0xFF);
        return (aa << 24) | (rr << 16) | (rg << 8) | rb;
    }

    // 圆角矩形（用多个 fill 近似）
    private void fillRounded(GuiGraphics g, float x, float y, float w, float h, int color) {
        g.fill((int)(x + CORNER), (int)y, (int)(x + w - CORNER), (int)(y + h), color);
        g.fill((int)x, (int)(y + CORNER), (int)(x + CORNER), (int)(y + h - CORNER), color);
        g.fill((int)(x + w - CORNER), (int)(y + CORNER), (int)(x + w), (int)(y + h - CORNER), color);
    }

    private void fillRoundedTop(GuiGraphics g, float x, float y, float w, float h, int c1, int c2) {
        // 水平渐变近似：分段绘制
        int steps = (int)w;
        for (int i = 0; i < steps; i++) {
            float t = (float)i / steps;
            int color = lerpColor(0xFF000000 | (c1 & 0xFFFFFF), 0xFF000000 | (c2 & 0xFFFFFF), t);
            g.fill((int)(x + i), (int)y, (int)(x + i + 1), (int)(y + h), color);
        }
    }

    @Override public boolean isPauseScreen() { return false; }
    @Override public boolean keyPressed(int key, int scan, int mods) {
        if (key == 256) { this.onClose(); return true; } // ESC 关闭
        return super.keyPressed(key, scan, mods);
    }
}