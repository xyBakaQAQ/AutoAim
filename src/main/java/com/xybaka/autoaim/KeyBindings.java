package com.xybaka.autoaim;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "autoaim", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBindings {

    public static final Lazy<KeyMapping> TOGGLE_VILLAGER_KEY = Lazy.of(() -> new KeyMapping(
            "key.autoaim.toggleVillager",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.autoaim"
    ));

    public static final Lazy<KeyMapping> TOGGLE_PLAYER_KEY = Lazy.of(() -> new KeyMapping(
            "key.autoaim.togglePlayer",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.autoaim"
    ));

    public static final Lazy<KeyMapping> TOGGLE_POSLOOK_KEY = Lazy.of(() -> new KeyMapping(
            "key.autoaim.togglePosLook",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_L,
            "key.categories.autoaim"
    ));

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_VILLAGER_KEY.get());
        event.register(TOGGLE_PLAYER_KEY.get());
        event.register(TOGGLE_POSLOOK_KEY.get());
    }
}