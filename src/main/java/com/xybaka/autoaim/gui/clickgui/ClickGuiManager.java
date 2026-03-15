package com.xybaka.autoaim.gui.clickgui;

import com.xybaka.autoaim.gui.clickgui.mode.AutoAimClickGuiScreen;
import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.ModuleManager;
import com.xybaka.autoaim.modules.settings.ModeSetting;
import com.xybaka.autoaim.modules.settings.Setting;
import com.xybaka.autoaim.modules.settings.StringSetting;
import net.minecraft.client.gui.screens.Screen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClickGuiManager {
    private static final float SCROLL_STEP = 8.0f;

    private final String mode;
    private Category selectedCategory = Category.values()[0];
    private final Map<Module, Float> slideAnimations = new HashMap<>();
    private final Map<Module, String> openEnums = new HashMap<>();
    private final Map<Module, Float> toggleAnimations = new HashMap<>();
    private final Map<Category, Float> tabHoverAnimations = new HashMap<>();
    private float scrollOffset;
    private float scrollTarget;

    public ClickGuiManager(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }

    public Screen createScreen() {
        return switch (mode) {
            case "AutoAim" -> new AutoAimClickGuiScreen(this);
            default -> new AutoAimClickGuiScreen(this);
        };
    }

    public String getKeyName(int key) {
        if (key <= 0) {
            return "NONE";
        }
        try {
            int sc = org.lwjgl.glfw.GLFW.glfwGetKeyScancode(key);
            String n = org.lwjgl.glfw.GLFW.glfwGetKeyName(key, sc);
            if (n != null) {
                return n.toUpperCase();
            }
        } catch (Exception ignored) {
        }

        return switch (key) {
            case org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE -> "SPACE";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_TAB -> "TAB";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER -> "ENTER";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE -> "BACK";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE -> "ESC";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SUPER -> "LWIN";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SUPER -> "RWIN";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_MENU -> "MENU";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_INSERT -> "INS";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE -> "DEL";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_HOME -> "HOME";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_END -> "END";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP -> "PGUP";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN -> "PGDN";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_CAPS_LOCK -> "CAPS";
            default -> "K" + key;
        };
    }

    public Category getSelectedCategory() {
        return selectedCategory;
    }

    public void selectCategory(Category category) {
        if (category == selectedCategory) {
            return;
        }
        selectedCategory = category;
        slideAnimations.clear();
        openEnums.clear();
        scrollOffset = 0f;
        scrollTarget = 0f;
    }

    public List<Module> getVisibleModules() {
        return ModuleManager.instance.getModulesByCategory(selectedCategory);
    }

    public int getCategoryModuleCount(Category category) {
        return ModuleManager.instance.getModulesByCategory(category).size();
    }

    public boolean isExpanded(Module module) {
        return slideAnimations.containsKey(module);
    }

    public void expand(Module module) {
        slideAnimations.putIfAbsent(module, 0f);
    }

    public void collapse(Module module) {
        slideAnimations.remove(module);
        openEnums.remove(module);
    }

    public void toggleExpanded(Module module) {
        if (module.getSettings().isEmpty()) {
            return;
        }
        if (isExpanded(module)) {
            collapse(module);
        } else {
            expand(module);
        }
    }

    public float getSlideAnimation(Module module) {
        return slideAnimations.getOrDefault(module, 0f);
    }

    public void setSlideAnimation(Module module, float value) {
        slideAnimations.put(module, value);
    }

    public float getToggleAnimation(Module module, boolean enabledDefault) {
        return toggleAnimations.getOrDefault(module, enabledDefault ? 1f : 0f);
    }

    public void setToggleAnimation(Module module, float value) {
        toggleAnimations.put(module, value);
    }

    public float getTabHoverAnimation(Category category) {
        return tabHoverAnimations.getOrDefault(category, 0f);
    }

    public void setTabHoverAnimation(Category category, float value) {
        tabHoverAnimations.put(category, value);
    }

    public String getOpenEnum(Module module) {
        return openEnums.get(module);
    }

    public void toggleOpenEnum(Module module, Setting setting) {
        String current = openEnums.get(module);
        openEnums.put(module, setting.getName().equals(current) ? null : setting.getName());
    }

    public boolean isEnumOpen(Module module, Setting setting) {
        return setting.getName().equals(openEnums.get(module));
    }

    public int calcSettingsHeight(Module module) {
        if (module == null || module.getSettings().isEmpty()) {
            return 0;
        }
        String openEnum = openEnums.get(module);
        int h = 6;
        for (Setting setting : module.getSettings()) {
            if (setting instanceof ModeSetting<?> enumSetting && setting.getName().equals(openEnum)) {
                h += 16 + enumSetting.getValues().length * AutoAimClickGuiScreen.ENUM_ROW_HEIGHT + 4;
            } else if (setting instanceof StringSetting) {
                h += AutoAimClickGuiScreen.STRING_SETTING_HEIGHT;
            } else {
                h += AutoAimClickGuiScreen.SETTING_ROW_HEIGHT;
            }
        }
        return h;
    }

    public int calcModuleListHeight() {
        int h = 0;
        for (Module module : getVisibleModules()) {
            h += AutoAimClickGuiScreen.MODULE_ROW_HEIGHT;
            if (isExpanded(module)) {
                h += Math.round(calcSettingsHeight(module) * getSlideAnimation(module));
            }
        }
        return h;
    }

    public float getScrollOffset() {
        return scrollOffset;
    }

    public float getScrollTarget() {
        return scrollTarget;
    }

    public void animateScroll(float factor) {
        scrollOffset = lerp(scrollOffset, scrollTarget, factor);
    }

    public void clampScroll(int viewportHeight) {
        float maxScroll = Math.max(0, calcModuleListHeight() - viewportHeight);
        scrollTarget = clamp(scrollTarget, 0f, maxScroll);
        scrollOffset = clamp(scrollOffset, 0f, maxScroll);
    }

    public void scrollBy(double delta, int viewportHeight) {
        clampScroll(viewportHeight);
        float normalized = (float) Math.max(-1.0, Math.min(1.0, delta));
        if (normalized == 0f) {
            return;
        }
        scrollTarget -= normalized * SCROLL_STEP;
        clampScroll(viewportHeight);
    }

    private static float lerp(float a, float b, float t) {
        float d = b - a;
        return Math.abs(d) < 0.4f ? b : a + d * t;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
