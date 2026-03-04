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
        // 打开 GUI
        mc.setScreen(new ClickGuiScreen());
        // 开启后立即关闭模块状态，防止下次按键冲突，且 GUI 关闭由 Screen 自己处理
        this.disable();
    }
}