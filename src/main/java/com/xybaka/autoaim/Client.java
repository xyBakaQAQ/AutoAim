package com.xybaka.autoaim;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("autoaim")
public class Client {
    public Client() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(AutoAim.class);
    }

    private void setup(final FMLClientSetupEvent event) {
        // Client setup code
    }
}
