package com.xybaka.autoaim.events.features;

import com.xybaka.autoaim.modules.ModuleManager;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ConfigSaveHandler {
    @SubscribeEvent
    public void onLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ModuleManager.instance.saveConfig();
    }
}