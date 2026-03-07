package com.xybaka.autoaim.events;

import com.xybaka.autoaim.events.features.KeyHandler;
import net.minecraftforge.common.MinecraftForge;

public class EventManager {
    public static void register(){
        MinecraftForge.EVENT_BUS.register(new KeyHandler());
    }
}
