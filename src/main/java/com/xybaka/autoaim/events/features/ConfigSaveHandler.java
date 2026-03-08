package com.xybaka.autoaim.events.features;

import com.xybaka.autoaim.config.ConfigManager;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ConfigSaveHandler {
    @SubscribeEvent
    public void onLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ConfigManager.instance.save();
    }
}
