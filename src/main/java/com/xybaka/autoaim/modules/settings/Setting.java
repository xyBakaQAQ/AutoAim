package com.xybaka.autoaim.modules.settings;

import com.xybaka.autoaim.modules.Module;

public abstract class Setting {
    private final String name;
    private Module parent;

    public Setting(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public Module getParent() { return parent; }
    
    public void setParent(Module parent) {
        this.parent = parent;
    }
}