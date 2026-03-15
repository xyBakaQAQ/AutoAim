package com.xybaka.autoaim.modules;

import com.xybaka.autoaim.config.ConfigManager;
import com.xybaka.autoaim.modules.client.Target;
import com.xybaka.autoaim.modules.client.Teams;
import com.xybaka.autoaim.modules.combat.AutoAim;
import com.xybaka.autoaim.modules.combat.NoRecoil;
import com.xybaka.autoaim.modules.movement.Sprint;
import com.xybaka.autoaim.modules.movement.invMove;
import com.xybaka.autoaim.modules.render.*;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static final ModuleManager instance = new ModuleManager();

    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        //Client
        modules.add(new Target());
        modules.add(new Teams());
        //Combat
        modules.add(new AutoAim());
        modules.add(new NoRecoil());
        //Movement
        modules.add(new Sprint());
        modules.add(new invMove());
        //Render
        modules.add(new HUD());
        modules.add(new TargetHud());
        modules.add(new ClickGUI());
        modules.add(new ESP());
        modules.add(new FullBright());
        modules.add(new Camera());
        modules.add(new NoFov());
        modules.add(new SkinOverlay());

        modules.forEach(Module::init);
        ConfigManager.instance.load(modules);
        get(Target.class).enable();
    }

    public void saveConfig() {
        ConfigManager.instance.save(modules);
    }

    public void loadConfig() {
        ConfigManager.instance.load(modules);
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
