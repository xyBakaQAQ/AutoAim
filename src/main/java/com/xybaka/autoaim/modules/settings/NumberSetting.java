package com.xybaka.autoaim.modules.settings;

import com.xybaka.autoaim.config.ConfigManager;

public class NumberSetting extends Setting {
    private double value, min, max, increment;

    public NumberSetting(String name, double defaultValue, double min, double max, double increment) {
        super(name);
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public double getValue() { return value; }

    public void setValue(double value) {
        double precision = 1.0 / increment;
        this.value = Math.round(Math.max(min, Math.min(max, value)) * precision) / precision;
        ConfigManager.instance.save();
    }

    public float getValueFloat() { return (float) value; }
    public double getMin() { return min; }
    public double getMax() { return max; }
}