package com.xybaka.autoaim.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.render.TargetHud;
import com.xybaka.autoaim.modules.settings.BooleanSetting;
import com.xybaka.autoaim.modules.settings.ModeSetting;
import com.xybaka.autoaim.modules.settings.NumberSetting;
import com.xybaka.autoaim.modules.settings.Setting;
import com.xybaka.autoaim.modules.settings.StringSetting;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
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

    private String keyToString(int key) {
        if (key <= 0) return "NONE";
        try {
            for (Field f : GLFW.class.getDeclaredFields()) {
                if (f.getName().startsWith("GLFW_KEY_") && f.getType() == int.class) {
                    if (f.getInt(null) == key) return f.getName();
                }
            }
        } catch (Exception ignored) {}
        return "NONE";
    }

    private int stringToKey(String name) {
        if (name == null || name.equals("NONE")) return -1;
        try {
            Field f = GLFW.class.getDeclaredField(name);
            return f.getInt(null);
        } catch (Exception ignored) {}
        return -1;
    }

    public void save(List<Module> modules) {
        JsonObject root = new JsonObject();

        for (Module module : modules) {
            JsonObject moduleJson = new JsonObject();
            moduleJson.addProperty("enabled", module.isEnabled());
            moduleJson.addProperty("key", keyToString(module.getKey()));

            if (module instanceof TargetHud targetHud) {
                moduleJson.addProperty("hudX", targetHud.getStoredX());
                moduleJson.addProperty("hudY", targetHud.getStoredY());
            }

            JsonObject settingsJson = new JsonObject();
            for (Setting setting : module.getSettings()) {
                if (setting instanceof BooleanSetting bs) {
                    settingsJson.addProperty(bs.getName(), bs.isEnabled());
                } else if (setting instanceof NumberSetting ns) {
                    settingsJson.addProperty(ns.getName(), ns.getValue());
                } else if (setting instanceof ModeSetting<?> ms) {
                    settingsJson.addProperty(ms.getName(), ms.getDisplayName());
                } else if (setting instanceof StringSetting ss) {
                    settingsJson.addProperty(ss.getName(), ss.getValue());
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
                    module.setKey(stringToKey(moduleJson.get("key").getAsString()));
                }

                if (module instanceof TargetHud targetHud) {
                    int hudX = moduleJson.has("hudX") ? moduleJson.get("hudX").getAsInt() : targetHud.getStoredX();
                    int hudY = moduleJson.has("hudY") ? moduleJson.get("hudY").getAsInt() : targetHud.getStoredY();
                    targetHud.setStoredPosition(hudX, hudY);
                }

                if (moduleJson.has("settings")) {
                    JsonObject settingsJson = moduleJson.getAsJsonObject("settings");
                    for (Setting setting : module.getSettings()) {
                        if (!settingsJson.has(setting.getName())) continue;

                        if (setting instanceof BooleanSetting bs) {
                            bs.setEnabled(settingsJson.get(bs.getName()).getAsBoolean());
                        } else if (setting instanceof NumberSetting ns) {
                            ns.setValue(settingsJson.get(ns.getName()).getAsDouble());
                        } else if (setting instanceof ModeSetting<?> ms) {
                            ms.setValueByName(settingsJson.get(ms.getName()).getAsString());
                        } else if (setting instanceof StringSetting ss) {
                            ss.setValue(settingsJson.get(ss.getName()).getAsString());
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

    public File getConfigFile() {
        return configFile;
    }

    public File getConfigDir() {
        return configDir;
    }
}
