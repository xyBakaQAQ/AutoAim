package com.xybaka.autoaim.modules.client;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.settings.BooleanSetting;
import com.xybaka.autoaim.modules.settings.ModeSetting;
import org.lwjgl.glfw.GLFW;

public class Target extends Module {
    public final ModeSetting<String> mode = mode("Mode", "Distance", "Distance", "Health", "FOV");
    public final ModeSetting<String> aimPart = mode("Aim Part", "Head", "Head", "Body", "Feet");
    public final BooleanSetting players = new BooleanSetting("Players", true);
    public final BooleanSetting monsters = new BooleanSetting("Monsters", true);
    public final BooleanSetting animals = new BooleanSetting("Animals", false);
    public final BooleanSetting villagers = new BooleanSetting("Villagers", false);
    public final BooleanSetting golems = new BooleanSetting("Golems", false);
    public final BooleanSetting waterAnimals = new BooleanSetting("Water Animals", false);
    public final BooleanSetting waterCreatures = new BooleanSetting("Water Creatures", false);
    public final BooleanSetting ambient = new BooleanSetting("Ambient", false);

    public Target() {
        super("Target", Category.CLIENT, GLFW.GLFW_KEY_UNKNOWN);
        this.enable();
    }

    @Override
    public void toggle() {
        if (!isEnabled()) enable();
    }
}
