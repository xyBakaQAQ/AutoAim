package com.xybaka.autoaim.events.features;

import com.xybaka.autoaim.gui.targethud.TargetHudManager;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TargetHudScreenHandler {
    @SubscribeEvent
    public void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof ChatScreen)) {
            return;
        }
        if (TargetHudManager.handleMouseClicked(event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onMouseDragged(ScreenEvent.MouseDragged.Pre event) {
        if (!(event.getScreen() instanceof ChatScreen)) {
            return;
        }
        if (TargetHudManager.handleMouseDragged(event.getMouseX(), event.getMouseY(), event.getMouseButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (!(event.getScreen() instanceof ChatScreen)) {
            return;
        }
        if (TargetHudManager.handleMouseReleased(event.getButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onScreenClosing(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof ChatScreen) {
            TargetHudManager.onChatClosed();
        }
    }
}