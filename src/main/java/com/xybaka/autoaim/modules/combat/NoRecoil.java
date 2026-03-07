package com.xybaka.autoaim.modules.combat;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import org.lwjgl.glfw.GLFW;

public class NoRecoil extends Module {
    public NoRecoil() {
        super("NoRecoil", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }
}
