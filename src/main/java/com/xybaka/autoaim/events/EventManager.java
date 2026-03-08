package com.xybaka.autoaim.events;

import com.xybaka.autoaim.config.ConfigManager;
import com.xybaka.autoaim.events.features.KeyHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventManager {
    public static void register(){
        MinecraftForge.EVENT_BUS.register(new KeyHandler());
        MinecraftForge.EVENT_BUS.register(new ConfigSaveHandler());
    }
    
    public static class ConfigSaveHandler {
        @SubscribeEvent
        public void onLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
            ConfigManager.instance.save();
        }
    }
}
