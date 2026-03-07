package com.xybaka.autoaim;

import com.xybaka.autoaim.events.EventManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod("autoaim")
@EventBusSubscriber(modid = "autoaim", bus = Bus.MOD)
public class AutoAim {
    public AutoAim() {
        EventManager.register();
    }
}