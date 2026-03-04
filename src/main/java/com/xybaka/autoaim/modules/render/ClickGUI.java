package com.xybaka.autoaim.modules.render;

import com.xybaka.autoaim.gui.ClickGuiScreen;
import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import org.lwjgl.glfw.GLFW;

public class ClickGUI extends Module {
    public ClickGUI() {
        super("ClickGUI", Category.RENDER, GLFW.GLFW_KEY_RIGHT_SHIFT); // 推荐使用右 Shift
    }

    @Override
    public void onEnable() {
        mc.setScreen(new ClickGuiScreen());
        this.disable();
    }
}