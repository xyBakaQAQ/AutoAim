package com.xybaka.autoaim.events.features;

import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class KeyHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        if (event.getAction() == GLFW.GLFW_PRESS && Minecraft.getInstance().screen == null) {
            int pressedKey = event.getKey();
            if (pressedKey <= 0) return;
            for (Module m : ModuleManager.instance.getModules()) {
                if (m.getKey() == pressedKey) {
                    m.toggle();
                }
            }
        }
    }
}