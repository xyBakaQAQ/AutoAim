package com.xybaka.autoaim.modules.render;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.settings.BooleanSetting;
import org.lwjgl.glfw.GLFW;

public class Camera extends Module {

    public final BooleanSetting noClip = new BooleanSetting("NoClip", true);
    public final BooleanSetting noHurtCam = new BooleanSetting("NoHurt", false);

    public Camera() {
        super("Camera", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }
}