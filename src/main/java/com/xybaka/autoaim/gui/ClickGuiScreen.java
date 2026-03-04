package com.xybaka.autoaim.gui;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.ModuleManager;
import com.xybaka.autoaim.modules.settings.BooleanSetting;
import com.xybaka.autoaim.modules.settings.NumberSetting;
import com.xybaka.autoaim.modules.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClickGuiScreen extends Screen {
    private static final Minecraft mc = Minecraft.getInstance();

    // 目标坐标与渲染坐标（用于平滑跟随）
    public static float targetX = 100, targetY = 100;
    private float renderX = 100, renderY = 100;
    public static float width = 450, height = 280;

    private Category selectedCategory = Category.COMBAT;
    private Module selectedModule = null;

    // 动画相关变量
    private float selectionBarY = 0; // 中间模块选中框的平滑高度
    private float settingsAlpha = 0; // 右侧设置面板的淡入淡出

    private boolean dragging = false;
    private double dragX, dragY;
    private NumberSetting draggingNumber = null;

    public ClickGuiScreen() {
        super(Component.literal("Naven Smooth"));
        this.renderX = targetX;
        this.renderY = targetY;
    }

    // 平滑计算函数
    private float animate(float current, float target, float speed) {
        float diff = target - current;
        if (Math.abs(diff) < 0.1f) return target;
        return current + diff * speed;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // --- 1. 更新动画逻辑 ---
        renderX = animate(renderX, targetX, 0.2f);
        renderY = animate(renderY, targetY, 0.2f);

        // --- 2. 绘制背景 ---
        drawRect(graphics, renderX, renderY, width, height, 0xEE151515);

        // --- 3. 左侧分类 (Category) ---
        float catY = renderY + 30;
        for (Category c : Category.values()) {
            int color = (c == selectedCategory) ? 0xFF55FFFF : 0xFFAAAAAA;
            graphics.drawString(font, c.name(), (int)renderX + 10, (int)catY, color);
            catY += 25;
        }

        // --- 4. 中间模块 (Module) ---
        float modX = renderX + 110;
        float modY = renderY + 30;
        Module hoveredMod = null;

        for (Module m : ModuleManager.instance.getModulesByCategory(selectedCategory)) {
            // 基础背景
            int bgColor = m.isEnabled() ? 0xFF3662EC : 0xFF252525;
            drawRect(graphics, modX, modY, 130, 20, bgColor);

            // 如果是当前选中的模块，计算平滑选中框
            if (m == selectedModule) {
                // 这里可以加一个高亮边框或者在它旁边画个小条
                drawRect(graphics, modX - 2, modY, 2, 20, 0xFF55FFFF);
            }

            graphics.drawString(font, m.getName(), (int)modX + 5, (int)modY + 6, -1);
            modY += 25;
        }

        // --- 5. 右侧设置 (Settings) + 淡入动画 ---
        if (selectedModule != null) {
            settingsAlpha = animate(settingsAlpha, 1.0f, 0.15f);
            int alphaInt = (int)(settingsAlpha * 255);
            int textColor = (alphaInt << 24) | 0xFFFFFF;

            float setX = renderX + 250;
            float setY = renderY + 35;

            graphics.drawString(font, "Settings: " + selectedModule.getName(), (int)setX, (int)renderY + 10, (alphaInt << 24) | 0xAAAAAA);

            for (Setting s : selectedModule.getSettings()) {
                if (s instanceof BooleanSetting) {
                    BooleanSetting b = (BooleanSetting) s;
                    int boolColor = b.isEnabled() ? 0xFF55FF55 : 0xFFFF5555;
                    graphics.drawString(font, s.getName(), (int)setX, (int)setY, (alphaInt << 24) | (boolColor & 0x00FFFFFF));
                }
                else if (s instanceof NumberSetting) {
                    NumberSetting n = (NumberSetting) s;
                    graphics.drawString(font, n.getName() + ": " + n.getValue(), (int)setX, (int)setY, textColor);

                    // 进度条背景
                    drawRect(graphics, setX, setY + 12, 150, 2, (alphaInt / 2 << 24) | 0x303030);
                    double progress = (n.getValue() - n.getMin()) / (n.getMax() - n.getMin());
                    drawRect(graphics, setX, setY + 12, (float)(150 * progress), 2, (alphaInt << 24) | 0x55FFFF);
                }
                setY += 35;
            }
        } else {
            settingsAlpha = 0; // 重置透明度
        }

        // --- 6. 交互逻辑更新 ---
        if (dragging) {
            targetX = (float) (mouseX - dragX);
            targetY = (float) (mouseY - dragY);
        }
        if (draggingNumber != null) updateNumber(mouseX);

        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY, renderX, renderY, width, 25)) {
            dragging = true;
            dragX = mouseX - renderX;
            dragY = mouseY - renderY;
            return true;
        }

        // 点击分类
        float cY = renderY + 30;
        for (Category c : Category.values()) {
            if (isHovered(mouseX, mouseY, renderX, cY, 100, 20)) {
                selectedCategory = c;
                selectedModule = null;
                settingsAlpha = 0; // 切换分类重置动画
                return true;
            }
            cY += 25;
        }

        // 点击模块
        float mX = renderX + 110, mY = renderY + 30;
        for (Module m : ModuleManager.instance.getModulesByCategory(selectedCategory)) {
            if (isHovered(mouseX, mouseY, mX, mY, 130, 20)) {
                if (button == 0) m.toggle();
                if (m != selectedModule) {
                    selectedModule = m;
                    settingsAlpha = 0; // 切换模块重新播放淡入
                }
                return true;
            }
            mY += 25;
        }

        // 点击设置
        if (selectedModule != null) {
            float sX = renderX + 250, sY = renderY + 35;
            for (Setting s : selectedModule.getSettings()) {
                if (isHovered(mouseX, mouseY, sX, sY, 150, 25)) {
                    if (s instanceof BooleanSetting) ((BooleanSetting) s).toggle();
                    else if (s instanceof NumberSetting) draggingNumber = (NumberSetting) s;
                    return true;
                }
                sY += 35;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        draggingNumber = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateNumber(double mouseX) {
        float setX = renderX + 250;
        double diff = Math.min(150, Math.max(0, mouseX - setX));
        double val = draggingNumber.getMin() + (diff / 150) * (draggingNumber.getMax() - draggingNumber.getMin());
        draggingNumber.setValue(val);
    }

    private void drawRect(GuiGraphics g, float x, float y, float w, float h, int color) {
        g.fill((int)x, (int)y, (int)(x + w), (int)(y + h), color);
    }

    private boolean isHovered(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override public boolean isPauseScreen() { return false; }
}