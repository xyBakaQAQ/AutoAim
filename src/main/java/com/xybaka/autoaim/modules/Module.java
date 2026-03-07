package com.xybaka.autoaim.modules;

import com.xybaka.autoaim.modules.render.HUD;
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
    private final int key;
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


    private void setupSettings() {
        try {
            // 获取当前具体子类中声明的所有字段
            for (Field field : this.getClass().getDeclaredFields()) {
                // 判断字段类型是否是 Setting 或其子类
                if (Setting.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    Object obj = field.get(this);
                    if (obj instanceof Setting s) {
                        s.setParent(this); // 自动绑定父模块
                        this.settings.add(s); // 添加到列表
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

    public boolean isEnabled() {
        return enabled;
    }

    public List<Setting> getSettings() {
        return settings;
    }
}