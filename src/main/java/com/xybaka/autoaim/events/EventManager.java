package com.xybaka.autoaim.events;

import com.xybaka.autoaim.events.features.ConfigSaveHandler;
import com.xybaka.autoaim.events.features.KeyHandler;
import com.xybaka.autoaim.events.features.TargetHudScreenHandler;
import net.minecraftforge.common.MinecraftForge;

public class EventManager {
    public static void register(){
        MinecraftForge.EVENT_BUS.register(new KeyHandler());
        MinecraftForge.EVENT_BUS.register(new TargetHudScreenHandler());
        MinecraftForge.EVENT_BUS.register(new ConfigSaveHandler());
    }
}