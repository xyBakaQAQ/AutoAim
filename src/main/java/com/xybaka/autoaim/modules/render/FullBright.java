package com.xybaka.autoaim.modules.render;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import org.lwjgl.glfw.GLFW;

public class FullBright extends Module {
    public FullBright() {
        super("FullBright", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }
}
