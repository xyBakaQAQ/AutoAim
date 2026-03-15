package com.xybaka.autoaim.modules.render;

import com.xybaka.autoaim.clickgui.ClickGuiManager;
import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.settings.EnumSetting;
import org.lwjgl.glfw.GLFW;

public class ClickGUI extends Module {
    public final EnumSetting<ClickGuiManager.Mode> mode = new EnumSetting<>("Mode", ClickGuiManager.Mode.AutoAim);

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

