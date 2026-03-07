package com.xybaka.autoaim.modules.render;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import org.lwjgl.glfw.GLFW;

public class NoHurtCam extends Module {
    public NoHurtCam() {
        super("NoHurtCam", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }
}
