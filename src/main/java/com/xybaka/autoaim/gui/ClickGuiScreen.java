package com.xybaka.autoaim.gui;

import com.xybaka.autoaim.modules.Category;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    private final List<Panel> panels = new ArrayList<>();

    public ClickGuiScreen() {
        // 这里的标题仅用于系统标识
        super(Component.literal("ClickGUI"));

        int x = 20;
        for (Category category : Category.values()) {
            panels.add(new Panel(category, x, 20, 100, 20));
            x += 110;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        for (Panel panel : panels) {
            panel.render(graphics, mouseX, mouseY);
        }
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Panel panel : panels) {
            if (panel.mouseClicked(mouseX, mouseY, button)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}