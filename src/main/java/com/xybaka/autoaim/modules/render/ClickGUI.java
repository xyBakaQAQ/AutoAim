package com.xybaka.autoaim.modules.render;

import com.xybaka.autoaim.gui.clickgui.ClickGuiManager;
import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.settings.ModeSetting;
import org.lwjgl.glfw.GLFW;

public class ClickGUI extends Module {
    public final ModeSetting<String> mode = mode("Mode", "AutoAim", "AutoAim");

    public ClickGUI() {
        super("ClickGUI", Category.RENDER, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override
    public void onEnable() {
        ClickGuiManager manager = new ClickGuiManager(mode.getValue());
        mc.setScreen(manager.createScreen());
        this.disable();
    }
}
