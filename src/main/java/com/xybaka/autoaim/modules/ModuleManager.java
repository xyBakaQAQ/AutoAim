package com.xybaka.autoaim.modules;

import com.xybaka.autoaim.modules.combat.AutoAim;
import com.xybaka.autoaim.modules.render.ClickGUI;
import com.xybaka.autoaim.modules.render.HUD;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    // 使用单例模式，方便全局调用：ModuleManager.instance.getModules()
    public static final ModuleManager instance = new ModuleManager();

    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        //Combat
        modules.add(new AutoAim());

        //Render
        modules.add(new HUD());
        modules.add(new ClickGUI());
    }

    public List<Module> getModules() {
        return modules;
    }

    public Module getModuleByName(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public List<Module> getModulesByCategory(Category category) {
        List<Module> categoryModules = new ArrayList<>();
        for (Module m : modules) {
            if (m.getCategory() == category) categoryModules.add(m);
        }
        return categoryModules;
    }
}