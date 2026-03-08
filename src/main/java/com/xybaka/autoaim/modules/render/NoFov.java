package com.xybaka.autoaim.modules.render;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.settings.NumberSetting;
import org.lwjgl.glfw.GLFW;

public class NoFov extends Module {
    public final NumberSetting fov = new NumberSetting("FOV", 70, 30, 180, 1);

    public NoFov() {
        super("NoFov", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }
}
