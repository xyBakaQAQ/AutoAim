package com.xybaka.autoaim.modules.settings;

import com.xybaka.autoaim.config.ConfigManager;

public class EnumSetting<T extends Enum<T>> extends Setting {
    private T value;
    private final T[] values;

    public EnumSetting(String name, T defaultValue) {
        super(name);
        this.value = defaultValue;
        this.values = defaultValue.getDeclaringClass().getEnumConstants();
    }

    public T getValue() { return value; }
    public void setValue(T value) {
        this.value = value;
    }
    public T[] getValues() { return values; }

    public void cycle() {
        int next = (value.ordinal() + 1) % values.length;
        this.value = values[next];
    }

    public String getDisplayName() {
        return value.name();
    }
}