package com.xybaka.autoaim.modules.settings;

public class StringSetting extends Setting {
    private String value;
    private final int maxLength;

    public StringSetting(String name, String defaultValue) {
        super(name);
        this.value = defaultValue != null ? defaultValue : "";
        this.maxLength = 256;
    }

    public StringSetting(String name, String defaultValue, int maxLength) {
        super(name);
        this.maxLength = maxLength;
        this.value = defaultValue != null ? defaultValue : "";
        if (this.value.length() > maxLength) {
            this.value = this.value.substring(0, maxLength);
        }
    }

    public String getValue() { return value; }

    public void setValue(String value) {
        if (value == null) {
            this.value = "";
        } else if (value.length() > maxLength) {
            this.value = value.substring(0, maxLength);
        } else {
            this.value = value;
        }
    }

    public int getMaxLength() { return maxLength; }
}
