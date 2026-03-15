package com.xybaka.autoaim.clickgui.mode;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.settings.BooleanSetting;
import com.xybaka.autoaim.modules.settings.EnumSetting;
import com.xybaka.autoaim.modules.settings.NumberSetting;
import com.xybaka.autoaim.modules.settings.Setting;
import com.xybaka.autoaim.clickgui.ClickGuiManager;
import com.xybaka.autoaim.modules.settings.StringSetting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AutoAimClickGuiScreen extends Screen {
    public static final int MODULE_ROW_HEIGHT = 22;
    public static final int SETTING_ROW_HEIGHT = 26;
    public static final int ENUM_ROW_HEIGHT = 15;
    public static final int STRING_SETTING_HEIGHT = 34;

    private static final int WIN_W = 480;
    private static final int WIN_H = 300;
    private static final int TAB_W = 90;
    private static final int STRIPE = 2;
    private static final int HDR_H = 32;
    private static final int TAB_H = 26;
    private static final int BAR_PAD = 10;
    private static final int BAR_Y = 17;
    private static final int BAR_H = 3;
    private static final int DOT_R = 4;

    private static final int C_WIN_BG = 0xF0101018;
    private static final int C_TAB_BG = 0xF00C0C14;
    private static final int C_MOD_BG = 0xF0141420;
    private static final int C_SET_BG = 0xF00F0F1A;
    private static final int C_HDR_BG = 0xF0080810;
    private static final int C_GRAD_L = 0xFF9B5DE5;
    private static final int C_GRAD_R = 0xFF00D4FF;
    private static final int C_TEXT = 0xFFE8E8FF;
    private static final int C_TEXT_DIM = 0xFF6666AA;
    private static final int C_TEXT_SEL = 0xFFFFFFFF;
    private static final int C_TAB_SEL = 0xFF1A1A2E;
    private static final int C_MOD_HOV = 0xFF1C1C2C;
    private static final int C_MOD_ON = 0xFF1E1E38;
    private static final int C_ACCENT = 0xFF9B5DE5;
    private static final int C_TOG_ON = 0xFF6C3BD9;
    private static final int C_TOG_OFF = 0xFF252535;
    private static final int C_TOG_KNOB = 0xFFEEEEFF;
    private static final int C_BAR_BG = 0xFF252535;
    private static final int C_BAR_FG = 0xFF7B52D9;
    private static final int C_ENUM_SEL = 0xFF282845;

    private final ClickGuiManager manager;

    private NumberSetting dragNumber;
    private float dragBarX;
    private float dragBarW;
    private Module bindingModule;
    private StringSetting editingString;

    private int winX;
    private int winY;
    private boolean dragging;
    private double dragOffX;
    private double dragOffY;

    public AutoAimClickGuiScreen(ClickGuiManager manager) {
        super(Component.literal("ClickGUI"));
        this.manager = manager;
    }

    @Override
    protected void init() {
        winX = (width - WIN_W) / 2;
        winY = (height - WIN_H) / 2;
    }

    private static float lerp(float a, float b, float t) {
        float d = b - a;
        return Math.abs(d) < 0.4f ? b : a + d * t;
    }

    private static boolean hit(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static int lerpRGB(int a, int b, float t) {
        int aa = (a >> 24) & 0xFF;
        int ar = (a >> 16) & 0xFF;
        int ag = (a >> 8) & 0xFF;
        int ab = a & 0xFF;
        int br = (b >> 16) & 0xFF;
        int bg = (b >> 8) & 0xFF;
        int bb = b & 0xFF;
        return (aa << 24)
                | ((int) (ar + (br - ar) * t) << 16)
                | ((int) (ag + (bg - ag) * t) << 8)
                | (int) (ab + (bb - ab) * t);
    }

    private static int withAlpha(int rgb, int a) {
        return (a << 24) | (rgb & 0x00FFFFFF);
    }

    private static int gradColor(float t) {
        return lerpRGB(0xFF000000 | (C_GRAD_L & 0xFFFFFF), 0xFF000000 | (C_GRAD_R & 0xFFFFFF), t);
    }
    private static int contentH() {
        return WIN_H - HDR_H - STRIPE;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0x88000000);

        if (dragging) {
            winX = (int) (mx - dragOffX);
            winY = (int) (my - dragOffY);
        }
        if (dragNumber != null) {
            applyDragNumber(mx);
        }

        manager.clampScroll(contentH());
        manager.animateScroll(0.22f);
        manager.clampScroll(contentH());

        renderWindow(g, mx, my);
        super.render(g, mx, my, pt);
    }

    private void renderWindow(GuiGraphics g, int mx, int my) {
        int wx = winX;
        int wy = winY;

        g.fill(wx, wy, wx + WIN_W, wy + WIN_H, C_WIN_BG);
        fillGradientH(g, wx, wy, WIN_W, STRIPE, C_GRAD_L, C_GRAD_R);
        renderHeader(g, wx, wy + STRIPE);
        renderTabs(g, wx, wy + STRIPE + HDR_H, mx, my);
        renderModuleList(g, wx + TAB_W, wy + STRIPE + HDR_H, mx, my);

        g.fill(wx, wy + STRIPE, wx + 1, wy + WIN_H, 0x22FFFFFF);
        g.fill(wx + WIN_W - 1, wy + STRIPE, wx + WIN_W, wy + WIN_H, 0x22FFFFFF);
        g.fill(wx, wy + WIN_H - 1, wx + WIN_W, wy + WIN_H, 0x22FFFFFF);
        g.fill(wx + TAB_W, wy + STRIPE, wx + TAB_W + 1, wy + WIN_H, 0x33FFFFFF);
        fillGradientH(g, wx, wy + STRIPE + HDR_H - 1, WIN_W, 1, C_GRAD_L, C_GRAD_R);
    }

    private void renderHeader(GuiGraphics g, int wx, int wy) {
        g.fill(wx, wy, wx + WIN_W, wy + HDR_H, C_HDR_BG);

        String title = manager.getMode().name();
        int titleX = wx + 12;
        int titleY = wy + (HDR_H - 8) / 2;
        for (int i = 0; i < title.length(); i++) {
            float t = title.length() == 1 ? 1f : (float) i / (title.length() - 1);
            String ch = String.valueOf(title.charAt(i));
            g.drawString(font, ch, titleX, titleY, gradColor(t), false);
            titleX += font.width(ch);
        }
    }

    private void renderTabs(GuiGraphics g, int wx, int wy, int mx, int my) {
        g.fill(wx, wy, wx + TAB_W, wy + contentH(), C_TAB_BG);

        int ty = wy + 6;
        for (Category cat : Category.values()) {
            boolean sel = cat == manager.getSelectedCategory();
            boolean hov = hit(mx, my, wx, ty, TAB_W, TAB_H);

            float hA = lerp(manager.getTabHoverAnimation(cat), (hov || sel) ? 1f : 0f, 0.18f);
            manager.setTabHoverAnimation(cat, hA);

            if (sel) {
                g.fill(wx, ty, wx + TAB_W, ty + TAB_H, C_TAB_SEL);
                fillGradientV(g, wx, ty, 3, TAB_H, C_GRAD_L, C_GRAD_R);
            } else if (hA > 0.01f) {
                g.fill(wx, ty, wx + TAB_W, ty + TAB_H, withAlpha(0x1A1A2E, (int) (hA * 0xBB)));
            }

            int col = sel ? C_TEXT_SEL : lerpRGB(C_TEXT_DIM, C_TEXT, hA);
            String label = capitalize(cat.name().toLowerCase());
            int textX = wx + (sel ? 10 : 8);
            g.drawString(font, label, textX, ty + (TAB_H - 8) / 2, col, false);

            String cntStr = String.valueOf(manager.getCategoryModuleCount(cat));
            int cntX = wx + TAB_W - font.width(cntStr) - 6;
            g.drawString(font, cntStr, cntX, ty + (TAB_H - 8) / 2, withAlpha(0xFFFFFF, sel ? 0x99 : 0x44), false);

            ty += TAB_H;
        }
    }

    private void renderModuleList(GuiGraphics g, int rx, int ry, int mx, int my) {
        g.fill(rx, ry, rx + WIN_W - TAB_W, ry + contentH(), C_MOD_BG);

        int listH = contentH();
        int contentTotalH = manager.calcModuleListHeight();
        g.enableScissor(rx, ry, rx + WIN_W - TAB_W, ry + listH);

        List<Module> mods = manager.getVisibleModules();
        float curY = ry - manager.getScrollOffset();

        for (Module m : mods) {
            if (curY + MODULE_ROW_HEIGHT > ry && curY < ry + listH) {
                curY = renderModuleRow(g, m, rx, curY, mx, my, ry, ry + listH);
            } else {
                curY += MODULE_ROW_HEIGHT;
                if (manager.isExpanded(m)) {
                    curY += Math.round(manager.calcSettingsHeight(m) * manager.getSlideAnimation(m));
                }
            }
        }

        g.disableScissor();

        if (contentTotalH > listH) {
            int sbW = 3;
            int sbH = listH;
            float thumbH = Math.max(20, (float) listH / contentTotalH * sbH);
            float thumbY = (manager.getScrollOffset() / (contentTotalH - listH)) * (sbH - thumbH);
            g.fill(rx + WIN_W - TAB_W - sbW, ry, rx + WIN_W - TAB_W, ry + sbH, 0x22FFFFFF);
            g.fill(rx + WIN_W - TAB_W - sbW, (int) (ry + thumbY), rx + WIN_W - TAB_W, (int) (ry + thumbY + thumbH), 0x88AAAAFF);
        }
    }

    private float renderModuleRow(GuiGraphics g, Module m, float rx, float curY, int mx, int my, int clipTop, int clipBot) {
        int rowW = WIN_W - TAB_W;

        float anim = lerp(manager.getToggleAnimation(m, m.isEnabled()), m.isEnabled() ? 1f : 0f, 0.18f);
        manager.setToggleAnimation(m, anim);

        boolean hov = hit(mx, my, rx, curY, rowW, MODULE_ROW_HEIGHT);
        boolean expanded = manager.isExpanded(m);

        int bg = C_MOD_BG;
        if (m.isEnabled()) {
            bg = lerpRGB(C_MOD_BG, C_MOD_ON, anim);
        }
        if (hov && !expanded) {
            bg = lerpRGB(bg, C_MOD_HOV, 0.6f);
        }
        if (expanded) {
            bg = lerpRGB(bg, C_MOD_ON, 0.7f);
        }
        g.fill((int) rx, (int) curY, (int) (rx + rowW), (int) (curY + MODULE_ROW_HEIGHT), bg);

        if (anim > 0.01f) {
            fillGradientV(g, (int) rx, (int) curY, 2, MODULE_ROW_HEIGHT, withAlpha(C_GRAD_L, (int) (anim * 220)), withAlpha(C_GRAD_R, (int) (anim * 180)));
        }

        int textCol = m.isEnabled() ? C_TEXT_SEL : lerpRGB(C_TEXT_DIM, C_TEXT, anim * 0.4f);
        g.drawString(font, m.getName(), (int) rx + 10, (int) curY + (MODULE_ROW_HEIGHT - 8) / 2, textCol, false);

        int rightX = (int) (rx + rowW - 8);
        if (!m.getSettings().isEmpty()) {
            String arrow = expanded ? "-" : "+";
            rightX -= font.width(arrow);
            g.drawString(font, arrow, rightX, (int) curY + (MODULE_ROW_HEIGHT - 8) / 2, expanded ? gradColor(0.5f) : C_TEXT_DIM, false);
            rightX -= 4;
        }
        if (m.getKey() > 0 || m == bindingModule) {
            String kn = m == bindingModule ? "..." : manager.getKeyName(m.getKey());
            String bracketed = "[" + kn + "]";
            rightX -= font.width(bracketed) + 4;
            g.drawString(font, bracketed, rightX, (int) curY + (MODULE_ROW_HEIGHT - 8) / 2, m == bindingModule ? gradColor(0.5f) : C_TEXT_DIM, false);
        }

        if (expanded) {
            g.fill((int) rx + 2, (int) (curY + MODULE_ROW_HEIGHT - 1), (int) (rx + rowW), (int) (curY + MODULE_ROW_HEIGHT), withAlpha(0x9B5DE5, 80));
        }

        curY += MODULE_ROW_HEIGHT;

        if (expanded && !m.getSettings().isEmpty()) {
            float sa = lerp(manager.getSlideAnimation(m), 1f, 0.16f);
            manager.setSlideAnimation(m, sa);
            int fullH = manager.calcSettingsHeight(m);
            int visH = Math.round(fullH * sa);
            if (visH > 0) {
                g.fill((int) rx, (int) curY, (int) (rx + rowW), (int) (curY + visH), C_SET_BG);
                g.fill((int) rx, (int) curY, (int) rx + 2, (int) (curY + visH), withAlpha(C_ACCENT, 100));
                g.enableScissor((int) rx, Math.max((int) curY, clipTop), (int) (rx + rowW), Math.min((int) (curY + visH), clipBot));
                renderSettings(g, m, rx, curY, mx, my);
                g.disableScissor();
                curY += visH;
            }
        }

        return curY;
    }

    private void renderSettings(GuiGraphics g, Module m, float rx, float sy, int mx, int my) {
        int rowW = WIN_W - TAB_W;
        float ry = sy + 3;

        for (Setting s : m.getSettings()) {
            if (s instanceof BooleanSetting b) {
                boolean hov = hit(mx, my, rx + 2, ry, rowW - 4, SETTING_ROW_HEIGHT);
                if (hov) {
                    g.fill((int) (rx + 2), (int) ry, (int) (rx + rowW - 2), (int) (ry + SETTING_ROW_HEIGHT), 0x18FFFFFF);
                }
                g.drawString(font, s.getName(), (int) rx + 12, (int) ry + (SETTING_ROW_HEIGHT - 8) / 2, C_TEXT, false);
                drawToggle(g, rx + rowW - 38, ry + (SETTING_ROW_HEIGHT - 10) / 2f, b.isEnabled());
                ry += SETTING_ROW_HEIGHT;
            } else if (s instanceof NumberSetting n) {
                String label = n.getName() + "  " + String.format("%.2f", n.getValue());
                g.drawString(font, label, (int) rx + 12, (int) ry + 4, C_TEXT, false);
                float bx = rx + BAR_PAD;
                float bw = rowW - BAR_PAD * 2;
                float prog = (float) ((n.getValue() - n.getMin()) / (n.getMax() - n.getMin()));
                int by = (int) (ry + BAR_Y);
                g.fill((int) bx, by, (int) (bx + bw), by + BAR_H, C_BAR_BG);
                fillGradientH(g, (int) bx, by, (int) (bw * prog), BAR_H, C_GRAD_L, C_GRAD_R);
                int dotX = (int) (bx + bw * prog);
                g.fill(dotX - DOT_R + 2, by - 1, dotX + DOT_R - 2, by + BAR_H + 1, C_BAR_FG);
                g.fill(dotX - DOT_R / 2, by - 2, dotX + DOT_R / 2, by + BAR_H + 2, gradColor(prog));
                ry += SETTING_ROW_HEIGHT;
            } else if (s instanceof EnumSetting<?> e) {
                boolean open = manager.isEnumOpen(m, s);
                boolean hov = hit(mx, my, rx + 2, ry, rowW - 4, 16);
                if (hov) {
                    g.fill((int) (rx + 2), (int) ry, (int) (rx + rowW - 2), (int) (ry + 16), 0x18FFFFFF);
                }
                String label = s.getName() + " : " + e.getDisplayName() + (open ? " -" : " +");
                g.drawString(font, label, (int) rx + 12, (int) ry + 4, open ? gradColor(0.3f) : C_TEXT, false);
                ry += 16;
                if (open) {
                    for (Object val : e.getValues()) {
                        boolean sel = val == e.getValue();
                        boolean rowHov = hit(mx, my, rx + 6, ry, rowW - 12, ENUM_ROW_HEIGHT);
                        int bg = sel ? C_ENUM_SEL : (rowHov ? 0xFF1A1A2E : 0xFF111120);
                        g.fill((int) (rx + 6), (int) ry, (int) (rx + rowW - 6), (int) (ry + ENUM_ROW_HEIGHT), bg);
                        if (sel) {
                            fillGradientH(g, (int) (rx + 6), (int) ry, 2, ENUM_ROW_HEIGHT, C_GRAD_L, C_GRAD_R);
                        }
                        g.drawString(font, val.toString(), (int) (rx + 14), (int) ry + 3, sel ? C_TEXT_SEL : C_TEXT_DIM, false);
                        ry += ENUM_ROW_HEIGHT;
                    }
                    ry += 4;
                }
            } else if (s instanceof StringSetting str) {
                boolean editing = str == editingString;
                g.drawString(font, s.getName(), (int) rx + 12, (int) ry + 3, C_TEXT, false);
                int boxY = (int) (ry + 14);
                g.fill((int) (rx + 8), boxY, (int) (rx + rowW - 8), boxY + 14, editing ? 0xFF1E1E32 : 0xFF141424);
                if (editing) {
                    fillGradientH(g, (int) (rx + 8), boxY, rowW - 16, 1, C_GRAD_L, C_GRAD_R);
                }
                String disp = str.getValue().isEmpty() && !editing ? "click to edit" : str.getValue() + (editing ? "|" : "");
                g.drawString(font, disp, (int) (rx + 12), boxY + 3, editing ? C_TEXT_SEL : C_TEXT_DIM, false);
                ry += STRING_SETTING_HEIGHT;
            }
        }
    }

    private void drawToggle(GuiGraphics g, float x, float y, boolean on) {
        int trackW = 24;
        int trackH = 10;
        g.fill((int) x, (int) y, (int) (x + trackW), (int) (y + trackH), on ? C_TOG_ON : C_TOG_OFF);
        if (on) {
            g.fill((int) x, (int) y, (int) (x + trackW), (int) y + 1, withAlpha(0xFFFFFF, 30));
        }
        float kx = on ? x + trackW - 9 : x + 1;
        g.fill((int) kx, (int) y + 1, (int) kx + 8, (int) y + trackH - 1, C_TOG_KNOB);
    }

    private void fillGradientH(GuiGraphics g, float x, float y, float w, float h, int c1, int c2) {
        int steps = Math.max(1, (int) w);
        for (int i = 0; i < steps; i++) {
            g.fill((int) (x + i), (int) y, (int) (x + i + 1), (int) (y + h), lerpRGB(0xFF000000 | (c1 & 0xFFFFFF), 0xFF000000 | (c2 & 0xFFFFFF), (float) i / steps));
        }
    }

    private void fillGradientH(GuiGraphics g, int x, int y, int w, int h, int c1, int c2) {
        fillGradientH(g, (float) x, (float) y, (float) w, (float) h, c1, c2);
    }

    private void fillGradientV(GuiGraphics g, int x, int y, int w, int h, int c1, int c2) {
        for (int i = 0; i < h; i++) {
            g.fill(x, y + i, x + w, y + i + 1, lerpRGB(0xFF000000 | (c1 & 0xFFFFFF), 0xFF000000 | (c2 & 0xFFFFFF), (float) i / h));
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static <T extends Enum<T>> void setEnum(EnumSetting<T> s, int i) {
        s.setValue(s.getValues()[i]);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0 && hit(mx, my, winX, winY + STRIPE, WIN_W, HDR_H)) {
            dragging = true;
            dragOffX = mx - winX;
            dragOffY = my - winY;
            return true;
        }

        int ty = winY + STRIPE + HDR_H + 6;
        for (Category cat : Category.values()) {
            if (hit(mx, my, winX, ty, TAB_W, TAB_H)) {
                manager.selectCategory(cat);
                return true;
            }
            ty += TAB_H;
        }

        int rx = winX + TAB_W;
        int ry = winY + STRIPE + HDR_H;
        if (hit(mx, my, rx, ry, WIN_W - TAB_W, contentH())) {
            handleModuleListClick(mx, my, rx, ry, btn);
            return true;
        }

        return super.mouseClicked(mx, my, btn);
    }

    private void handleModuleListClick(double mx, double my, int rx, int ry, int btn) {
        float curY = ry - manager.getScrollOffset();

        for (Module m : manager.getVisibleModules()) {
            if (hit(mx, my, rx, curY, WIN_W - TAB_W, MODULE_ROW_HEIGHT)) {
                if (btn == 0 && hasShiftDown()) {
                    bindingModule = m;
                } else if (btn == 0) {
                    m.toggle();
                } else if (btn == 1) {
                    manager.toggleExpanded(m);
                }
                return;
            }
            curY += MODULE_ROW_HEIGHT;

            if (manager.isExpanded(m) && !m.getSettings().isEmpty()) {
                int visH = Math.round(manager.calcSettingsHeight(m) * manager.getSlideAnimation(m));
                if (visH > 0 && hit(mx, my, rx, curY, WIN_W - TAB_W, visH)) {
                    handleSettingsClick(m, rx, curY, mx, my);
                    return;
                }
                curY += visH;
            }
        }
    }

    private void handleSettingsClick(Module m, float rx, float sy, double mx, double my) {
        int rowW = WIN_W - TAB_W;
        float ry = sy + 3;

        for (Setting s : m.getSettings()) {
            if (s instanceof BooleanSetting b) {
                if (hit(mx, my, rx + 2, ry, rowW - 4, SETTING_ROW_HEIGHT)) {
                    b.toggle();
                    return;
                }
                ry += SETTING_ROW_HEIGHT;
            } else if (s instanceof NumberSetting n) {
                float bx = rx + BAR_PAD;
                float bw = rowW - BAR_PAD * 2;
                if (hit(mx, my, bx, ry + BAR_Y - 6, bw, BAR_H + 12)) {
                    dragNumber = n;
                    dragBarX = bx;
                    dragBarW = bw;
                    applyDragNumber(mx);
                    return;
                }
                ry += SETTING_ROW_HEIGHT;
            } else if (s instanceof EnumSetting<?> e) {
                if (hit(mx, my, rx + 2, ry, rowW - 4, 16)) {
                    manager.toggleOpenEnum(m, s);
                    return;
                }
                ry += 16;
                if (manager.isEnumOpen(m, s)) {
                    for (int i = 0; i < e.getValues().length; i++) {
                        if (hit(mx, my, rx + 6, ry, rowW - 12, ENUM_ROW_HEIGHT)) {
                            setEnum(e, i);
                            return;
                        }
                        ry += ENUM_ROW_HEIGHT;
                    }
                    ry += 4;
                }
            } else if (s instanceof StringSetting str) {
                if (hit(mx, my, rx + 8, ry + 14, rowW - 16, 14)) {
                    editingString = editingString == str ? null : str;
                    return;
                }
                ry += STRING_SETTING_HEIGHT;
            }
        }
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        dragging = false;
        dragNumber = null;
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (dragNumber != null) {
            applyDragNumber(mx);
            return true;
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        int rx = winX + TAB_W;
        int ry = winY + STRIPE + HDR_H;
        if (hit(mx, my, rx, ry, WIN_W - TAB_W, contentH())) {
            manager.scrollBy(delta, contentH());
            return true;
        }
        return super.mouseScrolled(mx, my, delta);
    }

    private void applyDragNumber(double mx) {
        if (dragNumber == null) {
            return;
        }
        double prog = Math.min(1, Math.max(0, (mx - dragBarX) / dragBarW));
        dragNumber.setValue(dragNumber.getMin() + prog * (dragNumber.getMax() - dragNumber.getMin()));
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (editingString != null) {
            if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE || key == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER) {
                editingString = null;
            } else if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE) {
                String v = editingString.getValue();
                if (!v.isEmpty()) {
                    editingString.setValue(v.substring(0, v.length() - 1));
                }
            }
            return true;
        }
        if (bindingModule != null) {
            if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE || key == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE) {
                bindingModule.setKey(org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN);
            } else {
                bindingModule.setKey(key);
            }
            bindingModule = null;
            return true;
        }
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}





