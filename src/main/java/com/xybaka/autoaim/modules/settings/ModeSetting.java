package com.xybaka.autoaim.modules.settings;

import java.util.Objects;

public class ModeSetting<T> extends Setting {
    private T value;
    private final T[] values;

    @SafeVarargs
    public ModeSetting(String name, T defaultValue, T... values) {
        super(name);
        this.value = defaultValue;
        this.values = values;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T[] getValues() {
        return values;
    }

    public void cycle() {
        for (int i = 0; i < values.length; i++) {
            if (Objects.equals(values[i], value)) {
                this.value = values[(i + 1) % values.length];
                return;
            }
        }
        if (values.length > 0) {
            this.value = values[0];
        }
    }

    public void setValueByName(String name) {
        for (T option : values) {
            if (Objects.equals(String.valueOf(option), name)) {
                this.value = option;
                return;
            }
        }
    }

    public void setValueByIndex(int index) {
        if (index >= 0 && index < values.length) {
            this.value = values[index];
        }
    }

    public String getDisplayName() {
        return String.valueOf(value);
    }
}
