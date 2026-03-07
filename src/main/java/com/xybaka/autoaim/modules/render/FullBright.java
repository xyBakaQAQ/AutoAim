package com.xybaka.autoaim.modules.render;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import org.lwjgl.glfw.GLFW;

public class FullBright extends Module {
    public FullBright() {
        super("FullBright", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
        this.enable();
    }

    @Override
    public void onEnable() {
        try {
            OptionInstance<Double> gammaOption = Minecraft.getInstance().options.gamma();
            var field = OptionInstance.class.getDeclaredField("value");
            field.setAccessible(true);
            field.set(gammaOption, 16.0D);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        try {
            OptionInstance<Double> gammaOption = Minecraft.getInstance().options.gamma();
            var field = OptionInstance.class.getDeclaredField("value");
            field.setAccessible(true);
            field.set(gammaOption, 0.5D); // 还原默认
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
