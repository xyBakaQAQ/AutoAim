package com.xybaka.autoaim.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.settings.BooleanSetting;
import com.xybaka.autoaim.modules.settings.EnumSetting;
import com.xybaka.autoaim.modules.settings.NumberSetting;
import com.xybaka.autoaim.modules.settings.Setting;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ConfigManager {
    public static final ConfigManager instance = new ConfigManager();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File configDir;
    private final File configFile;

    private ConfigManager() {
        this.configDir = new File(Minecraft.getInstance().gameDirectory, "AutoAim");
        this.configFile = new File(configDir, "config.json");

        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }

    public void save(List<Module> modules) {
        JsonObject root = new JsonObject();

        for (Module module : modules) {
            JsonObject moduleJson = new JsonObject();
            moduleJson.addProperty("enabled", module.isEnabled());
            moduleJson.addProperty("key", module.getKey());

            JsonObject settingsJson = new JsonObject();
            for (Setting setting : module.getSettings()) {
                if (setting instanceof BooleanSetting bs) {
                    settingsJson.addProperty(bs.getName(), bs.isEnabled());
                } else if (setting instanceof NumberSetting ns) {
                    settingsJson.addProperty(ns.getName(), ns.getValue());
                } else if (setting instanceof EnumSetting<?> es) {
                    settingsJson.addProperty(es.getName(), es.getValue().name());
                }
            }
            moduleJson.add("settings", settingsJson);
            root.add(module.getName(), moduleJson);
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(List<Module> modules) {
        if (!configFile.exists()) return;

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            for (Module module : modules) {
                if (!root.has(module.getName())) continue;

                JsonObject moduleJson = root.getAsJsonObject(module.getName());

                if (moduleJson.has("key")) {
                    module.setKey(moduleJson.get("key").getAsInt());
                }

                if (moduleJson.has("settings")) {
                    JsonObject settingsJson = moduleJson.getAsJsonObject("settings");
                    for (Setting setting : module.getSettings()) {
                        if (!settingsJson.has(setting.getName())) continue;

                        if (setting instanceof BooleanSetting bs) {
                            bs.setEnabled(settingsJson.get(bs.getName()).getAsBoolean());
                        } else if (setting instanceof NumberSetting ns) {
                            ns.setValue(settingsJson.get(ns.getName()).getAsDouble());
                        } else if (setting instanceof EnumSetting<?> es) {
                            try {
                                setEnumValue(es, settingsJson.get(es.getName()).getAsString());
                            } catch (Exception ignored) {}
                        }
                    }
                }

                if (moduleJson.has("enabled") && moduleJson.get("enabled").getAsBoolean()) {
                    module.enable();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Enum<T>> void setEnumValue(EnumSetting<T> setting, String name) {
        for (T value : setting.getValues()) {
            if (value.name().equals(name)) {
                setting.setValue(value);
                break;
            }
        }
    }

    public File getConfigFile() {
        return configFile;
    }
}