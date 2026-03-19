package com.xybaka.autoaim.modules;

import com.xybaka.autoaim.config.ConfigManager;
import com.xybaka.autoaim.modules.render.HUD;
import com.xybaka.autoaim.modules.settings.ModeSetting;
import com.xybaka.autoaim.modules.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public static final Minecraft mc = Minecraft.getInstance();
    private final List<Setting> settings = new ArrayList<>();
    private final String name;
    private final Category category;
    private int key;
    private boolean enabled;

    public Module(String name, Category category, int key) {
        this.name = name;
        this.category = category;
        this.key = key;
        this.enabled = false;
    }

    public final void init() {
        setupSettings();
    }

    protected final ModeSetting<String> mode(String name, String defaultValue, String... values) {
        return new ModeSetting<>(name, defaultValue, values);
    }


    private void setupSettings() {
        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                if (Setting.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    Object obj = field.get(this);
                    if (obj instanceof Setting s) {
                        s.setParent(this);
                        this.settings.add(s);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toggle() {
        if (enabled) disable();
        else enable();
        HUD.push(this.getName(), this.enabled);
    }

    public void enable() {
        this.enabled = true;
        MinecraftForge.EVENT_BUS.register(this);
        onEnable();
    }

    public void disable() {
        this.enabled = false;
        MinecraftForge.EVENT_BUS.unregister(this);
        onDisable();
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<Setting> getSettings() {
        return settings;
    }
}