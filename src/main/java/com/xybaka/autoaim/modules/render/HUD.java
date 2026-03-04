package com.xybaka.autoaim.modules.render;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.ModuleManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class HUD extends Module {

    public HUD() {
        // 名称 HUD，分类 RENDER，默认按键 O，默认开启
        super("HUD", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
        this.enable(); // 初始化时默认开启
    }

    @SubscribeEvent
    public void onRenderSafe(RenderGuiEvent.Post event) {
        if (!this.isEnabled() || mc.options.hideGui || mc.player == null) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();

        // 1. 获取所有已开启的模块并排序
        List<Module> activeModules = ModuleManager.instance.getModules().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt(m -> -mc.font.width(m.getName())))
                .toList();

        int yOffset = 5;

        // 2. 循环绘制每个模块名称
        for (Module m : activeModules) {
            String text = m.getName();
            int textWidth = mc.font.width(text);
            int x = screenWidth - textWidth - 5;

            int color = getRainbowColor(yOffset * 20);

            // 绘制文字阴影
            graphics.drawString(mc.font, text, x, yOffset, color, true);

            // 增加 Y 偏移 (行高 + 间距)
            yOffset += mc.font.lineHeight + 2;
        }
    }

    private int getRainbowColor(int offset) {
        float speed = 3000f; // 速度，数值越大越慢
        float hue = (System.currentTimeMillis() + offset) % (int)speed / speed;
        return Color.HSBtoRGB(hue, 0.6f, 1f);
    }
}