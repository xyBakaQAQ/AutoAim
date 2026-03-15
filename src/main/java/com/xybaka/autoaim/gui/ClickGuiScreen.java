package com.xybaka.autoaim.gui;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.ModuleManager;
import com.xybaka.autoaim.modules.settings.BooleanSetting;
import com.xybaka.autoaim.modules.settings.EnumSetting;
import com.xybaka.autoaim.modules.settings.NumberSetting;
import com.xybaka.autoaim.modules.settings.Setting;
import com.xybaka.autoaim.modules.settings.StringSetting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Fatality-style ClickGUI
 *
 * 布局：
 *   ┌──────────────────────────────────────────────┐
 *   │  [顶部渐变色条 2px]                            │
 *   ├──────────┬───────────────────────────────────┤
 *   │ 分类标签  │  模块列表                          │
 *   │  (左栏)  │  + 内联设置                        │
 *   └──────────┴───────────────────────────────────┘
 *
 * 颜色方案：深色半透明面板 + 紫/青渐变强调色
 */
public class ClickGuiScreen extends Screen {

    // ─────────────────── 尺寸常量 ───────────────────

    /** 整个窗口的固定宽高 */
    private static final int WIN_W   = 480;
    private static final int WIN_H   = 300;

    /** 左侧分类栏宽 */
    private static final int TAB_W   = 90;
    /** 顶部彩色条高 */
    private static final int STRIPE  = 2;
    /** 标题栏高（标志 + 标题文字区） */
    private static final int HDR_H   = 32;
    /** 每个分类 tab 行高 */
    private static final int TAB_H   = 26;
    /** 右侧模块区每行高 */
    private static final int MOD_H   = 22;
    /** 每个设置项高 */
    private static final int SET_H   = 26;
    /** Enum 下拉每行高 */
    private static final int ENUM_H  = 15;
    /** String 设置总高 */
    private static final int STR_H   = 34;
    /** 滑块参数 */
    private static final int BAR_PAD = 10;
    private static final int BAR_Y   = 17;
    private static final int BAR_H   = 3;
    private static final int DOT_R   = 4;

    // ─────────────────── 颜色常量 ───────────────────

    // 面板背景
    private static final int C_WIN_BG    = 0xF0101018;  // 主背景
    private static final int C_TAB_BG    = 0xF00C0C14;  // 左栏背景
    private static final int C_MOD_BG    = 0xF0141420;  // 右栏背景
    private static final int C_SET_BG    = 0xF00F0F1A;  // 设置区背景
    private static final int C_HDR_BG    = 0xF0080810;  // 顶部标题背景

    // 强调渐变：从左(紫) 到右(青)
    private static final int C_GRAD_L    = 0xFF9B5DE5;  // 紫
    private static final int C_GRAD_R    = 0xFF00D4FF;  // 青

    // 文字
    private static final int C_TEXT      = 0xFFE8E8FF;
    private static final int C_TEXT_DIM  = 0xFF6666AA;
    private static final int C_TEXT_SEL  = 0xFFFFFFFF;

    // 选中 / hover
    private static final int C_TAB_SEL   = 0xFF1A1A2E;  // 选中 tab 背景
    private static final int C_MOD_HOV   = 0xFF1C1C2C;
    private static final int C_MOD_ON    = 0xFF1E1E38;
    private static final int C_ACCENT    = 0xFF9B5DE5;  // 左侧亮条 / dot 颜色
    private static final int C_ACCENT2   = 0xFF00D4FF;

    // 开关
    private static final int C_TOG_ON    = 0xFF6C3BD9;
    private static final int C_TOG_OFF   = 0xFF252535;
    private static final int C_TOG_KNOB  = 0xFFEEEEFF;

    // 滑块
    private static final int C_BAR_BG    = 0xFF252535;
    private static final int C_BAR_FG    = 0xFF7B52D9;

    // Enum 选中行
    private static final int C_ENUM_SEL  = 0xFF282845;

    // ─────────────────── 运行时状态 ─────────────────

    /** 当前选中分类 */
    private Category selectedCat = Category.values()[0];

    /** 每个模块的设置展开动画 0→1（存在 key 即为展开中或已展开） */
    private final java.util.Map<Module, Float> slideAnims = new java.util.HashMap<>();
    /** 每个模块当前展开的 EnumSetting 名称 */
    private final java.util.Map<Module, String> openEnums = new java.util.HashMap<>();

    /** 模块开关动画表，值代表"开启进度" 0→1 */
    private final java.util.Map<Module, Float> toggleAnim = new java.util.HashMap<>();

    /** 分类 tab hover 动画 */
    private final java.util.Map<Category, Float> tabHoverAnim = new java.util.HashMap<>();

    /** 拖拽数值设置 */
    private NumberSetting dragNumber;
    private float         dragBarX, dragBarW;

    /** 按键绑定中的模块 */
    private Module        bindingModule  = null;
    /** 正在编辑的字符串设置 */
    private StringSetting editingString  = null;

    /** 窗口在屏幕上的锚点（左上角） */
    private int winX, winY;
    /** 拖拽偏移 */
    private boolean dragging;
    private double  dragOffX, dragOffY;

    /** 模块列表滚动偏移（像素） */
    private float scrollOffset = 0f;
    private float scrollTarget = 0f;

    // ─────────────────── 构造 ───────────────────────

    public ClickGuiScreen() {
        super(Component.literal("ClickGUI"));
    }

    @Override
    protected void init() {
        winX = (width  - WIN_W) / 2;
        winY = (height - WIN_H) / 2;
    }

    // ─────────────────── 工具 ───────────────────────

    private static float lerp(float a, float b, float t) {
        float d = b - a;
        return Math.abs(d) < 0.4f ? b : a + d * t;
    }

    private static boolean hit(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static int lerpRGB(int a, int b, float t) {
        int aa = (a >> 24) & 0xFF;
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab2 = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb  = b & 0xFF;
        return (aa << 24)
                | ((int)(ar + (br - ar) * t) << 16)
                | ((int)(ag + (bg - ag) * t) << 8)
                |  (int)(ab2 + (bb - ab2) * t);
    }

    private static int withAlpha(int rgb, int a) {
        return (a << 24) | (rgb & 0x00FFFFFF);
    }

    /** 渐变色：按水平位置在紫→青之间插值 */
    private static int gradColor(float t) {
        return lerpRGB(0xFF000000 | (C_GRAD_L & 0xFFFFFF),
                0xFF000000 | (C_GRAD_R & 0xFFFFFF), t);
    }

    private static String getKeyName(int key) {
        if (key <= 0) return "NONE";
        try {
            int sc = org.lwjgl.glfw.GLFW.glfwGetKeyScancode(key);
            String n = org.lwjgl.glfw.GLFW.glfwGetKeyName(key, sc);
            if (n != null) return n.toUpperCase();
            return switch (key) {
                case org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE        -> "SPACE";
                case org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT   -> "LSHIFT";
                case org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT  -> "RSHIFT";
                case org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
                case org.lwjgl.glfw.GLFW.GLFW_KEY_TAB          -> "TAB";
                case org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER        -> "ENTER";
                case org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE    -> "BACK";
                case org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE       -> "ESC";
                default -> "K" + key;
            };
        } catch (Exception e) { return "K" + key; }
    }

    // ─────────────────── 高度计算 ───────────────────

    private int calcSettingsH(Module m) {
        if (m == null || m.getSettings().isEmpty()) return 0;
        String openEnum = openEnums.get(m);
        int h = 6;
        for (Setting s : m.getSettings()) {
            if (s instanceof EnumSetting<?> e && s.getName().equals(openEnum)) {
                h += 16 + e.getValues().length * ENUM_H + 4;
            } else if (s instanceof StringSetting) {
                h += STR_H;
            } else {
                h += SET_H;
            }
        }
        return h;
    }

    private boolean isExpanded(Module m) {
        return slideAnims.containsKey(m);
    }

    /** 模块列表总高度（用于滚动计算） */
    private int calcModListH() {
        List<Module> mods = ModuleManager.instance.getModulesByCategory(selectedCat);
        int h = 0;
        for (Module m : mods) {
            h += MOD_H;
            if (isExpanded(m)) h += Math.round(calcSettingsH(m) * slideAnims.getOrDefault(m, 0f));
        }
        return h;
    }

    /** 右侧内容区高度 */
    private static int contentH() { return WIN_H - HDR_H - STRIPE; }

    // ─────────────────── 渲染主入口 ─────────────────

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        // 半透明遮罩
        g.fill(0, 0, width, height, 0x88000000);

        // 拖拽更新
        if (dragging) {
            winX = (int)(mx - dragOffX);
            winY = (int)(my - dragOffY);
        }
        if (dragNumber != null) applyDragNumber(mx);

        // 平滑滚动
        scrollOffset = lerp(scrollOffset, scrollTarget, 0.22f);

        renderWindow(g, mx, my);
        super.render(g, mx, my, pt);
    }

    // ─────────────────── 窗口整体 ───────────────────

    private void renderWindow(GuiGraphics g, int mx, int my) {
        int wx = winX, wy = winY;

        // ── 主背景 ──
        g.fill(wx, wy, wx + WIN_W, wy + WIN_H, C_WIN_BG);

        // ── 顶部渐变色条 ──
        fillGradientH(g, wx, wy, WIN_W, STRIPE, C_GRAD_L, C_GRAD_R);

        // ── 标题栏 ──
        renderHeader(g, wx, wy + STRIPE, mx, my);

        // ── 左侧分类栏 ──
        renderTabs(g, wx, wy + STRIPE + HDR_H, mx, my);

        // ── 右侧模块列表 ──
        renderModuleList(g, wx + TAB_W, wy + STRIPE + HDR_H, mx, my);

        // ── 外边框（渐变） ──
        // 上边框（已被 stripe 覆盖），左/右/下 细线
        g.fill(wx, wy + STRIPE, wx + 1, wy + WIN_H, 0x22FFFFFF);          // 左
        g.fill(wx + WIN_W - 1, wy + STRIPE, wx + WIN_W, wy + WIN_H, 0x22FFFFFF); // 右
        g.fill(wx, wy + WIN_H - 1, wx + WIN_W, wy + WIN_H, 0x22FFFFFF);   // 下

        // tab / 模块区分隔线
        g.fill(wx + TAB_W, wy + STRIPE, wx + TAB_W + 1, wy + WIN_H, 0x33FFFFFF);
        // 标题 / 内容分隔线（渐变）
        fillGradientH(g, wx, wy + STRIPE + HDR_H - 1, WIN_W, 1, C_GRAD_L, C_GRAD_R);
    }

    // ─────────────────── 标题栏 ─────────────────────

    private void renderHeader(GuiGraphics g, int wx, int wy, int mx, int my) {
        g.fill(wx, wy, wx + WIN_W, wy + HDR_H, C_HDR_BG);

        // "AutoAim" 渐变文字：用逐字符渐变模拟
        String title = "AutoAim";
        int titleX = wx + 12;
        int titleY = wy + (HDR_H - 8) / 2;
        for (int i = 0; i < title.length(); i++) {
            float t = (float) i / (title.length() - 1);
            g.drawString(font, String.valueOf(title.charAt(i)), titleX, titleY, gradColor(t), false);
            titleX += font.width(String.valueOf(title.charAt(i)));
        }



    }

    // ─────────────────── 分类 Tab 栏 ────────────────

    private void renderTabs(GuiGraphics g, int wx, int wy, int mx, int my) {
        g.fill(wx, wy, wx + TAB_W, wy + contentH(), C_TAB_BG);

        Category[] cats = Category.values();
        int ty = wy + 6;
        for (Category cat : cats) {
            boolean sel = cat == selectedCat;
            boolean hov = hit(mx, my, wx, ty, TAB_W, TAB_H);

            float hA = lerp(tabHoverAnim.getOrDefault(cat, 0f), (hov || sel) ? 1f : 0f, 0.18f);
            tabHoverAnim.put(cat, hA);

            // 选中 / hover 背景
            if (sel) {
                g.fill(wx, ty, wx + TAB_W, ty + TAB_H, C_TAB_SEL);
                // 左侧渐变亮条（3px）
                fillGradientV(g, wx, ty, 3, TAB_H, C_GRAD_L, C_GRAD_R);
            } else if (hA > 0.01f) {
                g.fill(wx, ty, wx + TAB_W, ty + TAB_H, withAlpha(0x1A1A2E, (int)(hA * 0xBB)));
            }

            // 文字颜色
            int col = sel ? C_TEXT_SEL : lerpRGB(C_TEXT_DIM, C_TEXT, hA);
            String label = capitalize(cat.name().toLowerCase());
            int textX = wx + (sel ? 10 : 8);
            g.drawString(font, label, textX, ty + (TAB_H - 8) / 2, col, false);

            // 模块数小标签
            int cnt = ModuleManager.instance.getModulesByCategory(cat).size();
            String cntStr = String.valueOf(cnt);
            int cntX = wx + TAB_W - font.width(cntStr) - 6;
            g.drawString(font, cntStr, cntX, ty + (TAB_H - 8) / 2, withAlpha(0xFFFFFF, sel ? 0x99 : 0x44), false);

            ty += TAB_H;
        }
    }

    // ─────────────────── 模块列表区 ─────────────────

    private void renderModuleList(GuiGraphics g, int rx, int ry, int mx, int my) {
        g.fill(rx, ry, rx + WIN_W - TAB_W, ry + contentH(), C_MOD_BG);

        int listH = contentH();
        int contentTotalH = calcModListH();
        // 裁剪
        g.enableScissor(rx, ry, rx + WIN_W - TAB_W, ry + listH);

        List<Module> mods = ModuleManager.instance.getModulesByCategory(selectedCat);
        float curY = ry - scrollOffset;

        for (Module m : mods) {
            if (curY + MOD_H > ry && curY < ry + listH) {
                curY = renderModuleRow(g, m, rx, curY, mx, my, ry, ry + listH);
            } else {
                curY += MOD_H;
                if (isExpanded(m)) curY += Math.round(calcSettingsH(m) * slideAnims.getOrDefault(m, 0f));
            }
        }

        g.disableScissor();

        // 滚动条（如果内容超出）
        if (contentTotalH > listH) {
            int sbW = 3, sbH = listH;
            float thumbH = Math.max(20, (float) listH / contentTotalH * sbH);
            float thumbY = (scrollOffset / (contentTotalH - listH)) * (sbH - thumbH);
            g.fill(rx + WIN_W - TAB_W - sbW, ry, rx + WIN_W - TAB_W, ry + sbH, 0x22FFFFFF);
            g.fill(rx + WIN_W - TAB_W - sbW, (int)(ry + thumbY),
                    rx + WIN_W - TAB_W, (int)(ry + thumbY + thumbH), 0x88AAAAFF);
        }
    }

    private float renderModuleRow(GuiGraphics g, Module m, float rx, float curY,
                                  int mx, int my, int clipTop, int clipBot) {
        int rowW = WIN_W - TAB_W;

        // 开关动画
        float anim = lerp(toggleAnim.getOrDefault(m, m.isEnabled() ? 1f : 0f),
                m.isEnabled() ? 1f : 0f, 0.18f);
        toggleAnim.put(m, anim);

        boolean hov      = hit(mx, my, rx, curY, rowW, MOD_H);
        boolean expanded = isExpanded(m);

        // 行背景
        int bg = C_MOD_BG;
        if (m.isEnabled()) bg = lerpRGB(C_MOD_BG, C_MOD_ON, anim);
        if (hov && !expanded) bg = lerpRGB(bg, C_MOD_HOV, 0.6f);
        if (expanded) bg = lerpRGB(bg, C_MOD_ON, 0.7f);
        g.fill((int)rx, (int)curY, (int)(rx + rowW), (int)(curY + MOD_H), bg);

        // 左侧启用亮条（渐变 2px）
        if (anim > 0.01f) {
            fillGradientV(g, (int)rx, (int)curY, 2, MOD_H,
                    withAlpha(C_GRAD_L, (int)(anim * 220)),
                    withAlpha(C_GRAD_R, (int)(anim * 180)));
        }

        // 模块名
        int textCol = m.isEnabled() ? C_TEXT_SEL : lerpRGB(C_TEXT_DIM, C_TEXT, anim * 0.4f);
        g.drawString(font, m.getName(), (int)rx + 10, (int)curY + (MOD_H - 8) / 2, textCol, false);

        // 右侧：keybind + 展开箭头
        int rightX = (int)(rx + rowW - 8);
        if (!m.getSettings().isEmpty()) {
            String arrow = expanded ? "▲" : "▼";
            rightX -= font.width(arrow);
            g.drawString(font, arrow, rightX, (int)curY + (MOD_H - 8) / 2,
                    expanded ? gradColor(0.5f) : C_TEXT_DIM, false);
            rightX -= 4;
        }
        if (m.getKey() > 0 || m == bindingModule) {
            String kn = m == bindingModule ? "..." : getKeyName(m.getKey());
            String bracketed = "[" + kn + "]";
            rightX -= font.width(bracketed) + 4;
            g.drawString(font, bracketed, rightX, (int)curY + (MOD_H - 8) / 2,
                    m == bindingModule ? gradColor(0.5f) : C_TEXT_DIM, false);
        }

        // 展开底部分隔线
        if (expanded)
            g.fill((int)rx + 2, (int)(curY + MOD_H - 1), (int)(rx + rowW),
                    (int)(curY + MOD_H), withAlpha(0x9B5DE5, 80));

        curY += MOD_H;

        // 设置区
        if (expanded && !m.getSettings().isEmpty()) {
            float sa = lerp(slideAnims.getOrDefault(m, 0f), 1f, 0.16f);
            slideAnims.put(m, sa);
            int fullH = calcSettingsH(m);
            int visH  = Math.round(fullH * sa);
            if (visH > 0) {
                g.fill((int)rx, (int)curY, (int)(rx + rowW), (int)(curY + visH), C_SET_BG);
                // 左侧细竖线
                g.fill((int)rx, (int)curY, (int)rx + 2, (int)(curY + visH), withAlpha(C_ACCENT, 100));
                g.enableScissor((int)rx, Math.max((int)curY, clipTop),
                        (int)(rx + rowW), Math.min((int)(curY + visH), clipBot));
                renderSettings(g, m, rx, curY, mx, my, openEnums.get(m));
                g.disableScissor();
                curY += visH;
            }
        }

        return curY;
    }

    // ─────────────────── 设置区渲染 ─────────────────

    private void renderSettings(GuiGraphics g, Module m, float rx, float sy, int mx, int my, String openEnum) {
        int rowW = WIN_W - TAB_W;
        float ry = sy + 3;

        for (Setting s : m.getSettings()) {
            if (s instanceof BooleanSetting b) {
                boolean hov = hit(mx, my, rx + 2, ry, rowW - 4, SET_H);
                if (hov) g.fill((int)(rx + 2), (int)ry, (int)(rx + rowW - 2), (int)(ry + SET_H), 0x18FFFFFF);
                g.drawString(font, s.getName(), (int)rx + 12, (int)ry + (SET_H - 8) / 2, C_TEXT, false);
                drawToggle(g, rx + rowW - 38, ry + (SET_H - 10) / 2f, b.isEnabled());
                ry += SET_H;

            } else if (s instanceof NumberSetting n) {
                String label = n.getName() + "  " + String.format("%.2f", n.getValue());
                g.drawString(font, label, (int)rx + 12, (int)ry + 4, C_TEXT, false);
                float bx = rx + BAR_PAD, bw = rowW - BAR_PAD * 2;
                float prog = (float)((n.getValue() - n.getMin()) / (n.getMax() - n.getMin()));
                int by = (int)(ry + BAR_Y);
                // 轨道
                g.fill((int)bx, by, (int)(bx + bw), by + BAR_H, C_BAR_BG);
                // 进度（渐变）
                fillGradientH(g, (int)bx, by, (int)(bw * prog), BAR_H, C_GRAD_L, C_GRAD_R);
                // 拖拽点（菱形感：上下各多 2px）
                int dotX = (int)(bx + bw * prog);
                g.fill(dotX - DOT_R + 2, by - 1, dotX + DOT_R - 2, by + BAR_H + 1, C_BAR_FG);
                g.fill(dotX - DOT_R / 2, by - 2, dotX + DOT_R / 2, by + BAR_H + 2,
                        gradColor(prog));
                ry += SET_H;

            } else if (s instanceof EnumSetting<?> e) {
                boolean open = s.getName().equals(openEnum);
                boolean hov  = hit(mx, my, rx + 2, ry, rowW - 4, 16);
                if (hov) g.fill((int)(rx + 2), (int)ry, (int)(rx + rowW - 2), (int)(ry + 16), 0x18FFFFFF);
                String label = s.getName() + " : " + e.getDisplayName() + (open ? " ▲" : " ▼");
                g.drawString(font, label, (int)rx + 12, (int)ry + 4,
                        open ? gradColor(0.3f) : C_TEXT, false);
                ry += 16;
                if (open) {
                    for (Object val : e.getValues()) {
                        boolean sel    = val == e.getValue();
                        boolean rowHov = hit(mx, my, rx + 6, ry, rowW - 12, ENUM_H);
                        int bg = sel ? C_ENUM_SEL : (rowHov ? 0xFF1A1A2E : 0xFF111120);
                        g.fill((int)(rx + 6), (int)ry, (int)(rx + rowW - 6), (int)(ry + ENUM_H), bg);
                        if (sel) fillGradientH(g, (int)(rx + 6), (int)ry, 2, ENUM_H, C_GRAD_L, C_GRAD_R);
                        g.drawString(font, val.toString(), (int)(rx + 14), (int)ry + 3,
                                sel ? C_TEXT_SEL : C_TEXT_DIM, false);
                        ry += ENUM_H;
                    }
                    ry += 4;
                }

            } else if (s instanceof StringSetting str) {
                boolean editing = str == editingString;
                g.drawString(font, s.getName(), (int)rx + 12, (int)ry + 3, C_TEXT, false);
                int boxY = (int)(ry + 14);
                g.fill((int)(rx + 8), boxY, (int)(rx + rowW - 8), boxY + 14,
                        editing ? 0xFF1E1E32 : 0xFF141424);
                if (editing)
                    fillGradientH(g, (int)(rx + 8), boxY, rowW - 16, 1, C_GRAD_L, C_GRAD_R);
                String disp = str.getValue().isEmpty() && !editing ? "click to edit" : str.getValue() + (editing ? "|" : "");
                g.drawString(font, disp, (int)(rx + 12), boxY + 3,
                        editing ? C_TEXT_SEL : C_TEXT_DIM, false);
                ry += STR_H;
            }
        }
    }

    // ─────────────────── 小组件 ─────────────────────

    private void drawToggle(GuiGraphics g, float x, float y, boolean on) {
        int trackW = 24, trackH = 10;
        g.fill((int)x, (int)y, (int)(x + trackW), (int)(y + trackH),
                on ? C_TOG_ON : C_TOG_OFF);
        // 渐变顶部高光
        if (on) g.fill((int)x, (int)y, (int)(x + trackW), (int)y + 1, withAlpha(0xFFFFFF, 30));
        float kx = on ? x + trackW - 9 : x + 1;
        g.fill((int)kx, (int)y + 1, (int)kx + 8, (int)y + trackH - 1, C_TOG_KNOB);
    }

    // ─────────────────── 绘图原语 ───────────────────

    private void fillGradientH(GuiGraphics g, float x, float y, float w, float h, int c1, int c2) {
        int steps = Math.max(1, (int)w);
        for (int i = 0; i < steps; i++) {
            g.fill((int)(x + i), (int)y, (int)(x + i + 1), (int)(y + h),
                    lerpRGB(0xFF000000 | (c1 & 0xFFFFFF), 0xFF000000 | (c2 & 0xFFFFFF), (float)i / steps));
        }
    }

    private void fillGradientH(GuiGraphics g, int x, int y, int w, int h, int c1, int c2) {
        fillGradientH(g, (float)x, (float)y, (float)w, (float)h, c1, c2);
    }

    private void fillGradientV(GuiGraphics g, int x, int y, int w, int h, int c1, int c2) {
        for (int i = 0; i < h; i++) {
            g.fill(x, y + i, x + w, y + i + 1,
                    lerpRGB(0xFF000000 | (c1 & 0xFFFFFF), 0xFF000000 | (c2 & 0xFFFFFF), (float)i / h));
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static <T extends Enum<T>> void setEnum(EnumSetting<T> s, int i) {
        s.setValue(s.getValues()[i]);
    }

    // ─────────────────── 鼠标事件 ───────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        // 标题栏拖拽
        if (btn == 0 && hit(mx, my, winX, winY + STRIPE, WIN_W, HDR_H)) {
            dragging = true;
            dragOffX = mx - winX;
            dragOffY = my - winY;
            return true;
        }

        // 分类 tab 点击
        Category[] cats = Category.values();
        int ty = winY + STRIPE + HDR_H + 6;
        for (Category cat : cats) {
            if (hit(mx, my, winX, ty, TAB_W, TAB_H)) {
                if (cat != selectedCat) {
                    selectedCat  = cat;
                    slideAnims.clear();
                    openEnums.clear();
                    scrollOffset = 0f;
                    scrollTarget = 0f;
                }
                return true;
            }
            ty += TAB_H;
        }

        // 模块列表点击
        int rx = winX + TAB_W;
        int ry = winY + STRIPE + HDR_H;
        if (hit(mx, my, rx, ry, WIN_W - TAB_W, contentH())) {
            handleModuleListClick(mx, my, rx, ry, btn);
            return true;
        }

        return super.mouseClicked(mx, my, btn);
    }

    private void handleModuleListClick(double mx, double my, int rx, int ry, int btn) {
        List<Module> mods = ModuleManager.instance.getModulesByCategory(selectedCat);
        float curY = ry - scrollOffset;

        for (Module m : mods) {
            if (hit(mx, my, rx, curY, WIN_W - TAB_W, MOD_H)) {
                if (btn == 0 && hasShiftDown()) {
                    bindingModule = m;
                } else if (btn == 0) {
                    m.toggle();
                } else if (btn == 1) {
                    toggleSettings(m);
                }
                return;
            }
            curY += MOD_H;

            if (isExpanded(m) && !m.getSettings().isEmpty()) {
                int visH = Math.round(calcSettingsH(m) * slideAnims.getOrDefault(m, 0f));
                if (visH > 0 && hit(mx, my, rx, curY, WIN_W - TAB_W, visH)) {
                    handleSettingsClick(m, rx, curY, mx, my);
                    return;
                }
                curY += visH;
            }
        }
    }

    private void toggleSettings(Module m) {
        if (m.getSettings().isEmpty()) return;
        if (isExpanded(m)) {
            slideAnims.remove(m);
            openEnums.remove(m);
        } else {
            slideAnims.put(m, 0f);
        }
    }

    private void handleSettingsClick(Module m, float rx, float sy, double mx, double my) {
        int rowW = WIN_W - TAB_W;
        float ry = sy + 3;

        for (Setting s : m.getSettings()) {
            if (s instanceof BooleanSetting b) {
                if (hit(mx, my, rx + 2, ry, rowW - 4, SET_H)) { b.toggle(); return; }
                ry += SET_H;
            } else if (s instanceof NumberSetting n) {
                float bx = rx + BAR_PAD, bw = rowW - BAR_PAD * 2;
                if (hit(mx, my, bx, ry + BAR_Y - 6, bw, BAR_H + 12)) {
                    dragNumber = n;
                    dragBarX   = bx;
                    dragBarW   = bw;
                    applyDragNumber(mx);
                    return;
                }
                ry += SET_H;
            } else if (s instanceof EnumSetting<?> e) {
                if (hit(mx, my, rx + 2, ry, rowW - 4, 16)) {
                    String cur = openEnums.get(m);
                    openEnums.put(m, s.getName().equals(cur) ? null : s.getName());
                    return;
                }
                ry += 16;
                if (s.getName().equals(openEnums.get(m))) {
                    for (int i = 0; i < e.getValues().length; i++) {
                        if (hit(mx, my, rx + 6, ry, rowW - 12, ENUM_H)) { setEnum(e, i); return; }
                        ry += ENUM_H;
                    }
                    ry += 4;
                }
            } else if (s instanceof StringSetting str) {
                if (hit(mx, my, rx + 8, ry + 14, rowW - 16, 14)) {
                    editingString = (editingString == str) ? null : str;
                    return;
                }
                ry += STR_H;
            }
        }
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        dragging   = false;
        dragNumber = null;
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (dragNumber != null) { applyDragNumber(mx); return true; }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        int rx = winX + TAB_W, ry = winY + STRIPE + HDR_H;
        if (hit(mx, my, rx, ry, WIN_W - TAB_W, contentH())) {
            int maxScroll = Math.max(0, calcModListH() - contentH());
            scrollTarget = Math.min(maxScroll, Math.max(0, scrollTarget - (float)(delta * 12)));
            return true;
        }
        return super.mouseScrolled(mx, my, delta);
    }

    private void applyDragNumber(double mx) {
        if (dragNumber == null) return;
        double prog = Math.min(1, Math.max(0, (mx - dragBarX) / dragBarW));
        dragNumber.setValue(dragNumber.getMin() + prog * (dragNumber.getMax() - dragNumber.getMin()));
    }

    // ─────────────────── 键盘事件 ───────────────────

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (editingString != null) {
            if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE ||
                    key == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER) {
                editingString = null;
            } else if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE) {
                String v = editingString.getValue();
                if (!v.isEmpty()) editingString.setValue(v.substring(0, v.length() - 1));
            }
            return true;
        }
        if (bindingModule != null) {
            if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE ||
                    key == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE) {
                bindingModule.setKey(org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN);
            } else {
                bindingModule.setKey(key);
            }
            bindingModule = null;
            return true;
        }
        if (key == 256) { onClose(); return true; }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean charTyped(char ch, int mods) {
        if (editingString != null) {
            if (editingString.getValue().length() < editingString.getMaxLength()) {
                editingString.setValue(editingString.getValue() + ch);
            }
            return true;
        }
        return super.charTyped(ch, mods);
    }

    @Override public boolean isPauseScreen() { return false; }
}