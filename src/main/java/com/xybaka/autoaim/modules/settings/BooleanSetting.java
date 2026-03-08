package com.xybaka.autoaim.modules.settings;

import com.xybaka.autoaim.config.ConfigManager;

public class BooleanSetting extends Setting {
    private boolean enabled;

    public BooleanSetting(String name, boolean defaultValue) {
        super(name);
        this.enabled = defaultValue;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { 
        this.enabled = enabled; 
        ConfigManager.instance.save();
    }
    public void toggle() { 
        this.enabled = !this.enabled; 
        ConfigManager.instance.save();
    }
}