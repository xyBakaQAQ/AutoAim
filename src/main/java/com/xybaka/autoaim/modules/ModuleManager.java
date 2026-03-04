package com.xybaka.autoaim.modules;

import com.xybaka.autoaim.modules.combat.AutoAim;
import com.xybaka.autoaim.modules.misc.Target;
import com.xybaka.autoaim.modules.misc.Teams;
import com.xybaka.autoaim.modules.movement.Sprint;
import com.xybaka.autoaim.modules.render.ClickGUI;
import com.xybaka.autoaim.modules.render.ESP;
import com.xybaka.autoaim.modules.render.HUD;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static final ModuleManager instance = new ModuleManager();

    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        //Combat
        modules.add(new AutoAim());

        //Misc
        modules.add(new Target());
        modules.add(new Teams());

        //Movement
        modules.add(new Sprint());

        //Render
        modules.add(new HUD());
        modules.add(new ClickGUI());
        modules.add(new ESP());

        modules.forEach(Module::init);

        get(Target.class).enable();
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

    public <T extends Module> T get(Class<T> clazz) {
        return modules.stream()
                .filter(m -> m.getClass() == clazz)
                .map(clazz::cast)
                .findFirst()
                .orElse(null);
    }
}