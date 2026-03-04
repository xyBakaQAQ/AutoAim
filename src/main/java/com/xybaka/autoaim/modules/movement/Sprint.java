package com.xybaka.autoaim.modules.movement;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class Sprint extends Module {

    public Sprint() {
        super("Sprint", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.player != null) {
            if (mc.player.input.up) {
                mc.player.setSprinting(true);
            }
        }
    }
}