package com.xybaka.autoaim.modules.movement;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.settings.BooleanSetting;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class invMove extends Module {

    public final BooleanSetting sneak = new BooleanSetting("Sneak", false);

    public invMove() {
        super("invMove", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    public void tickKeys() {
        if (!isEnabled()) return;
        if (mc.screen == null) return;

        tickKey(mc.options.keyUp);
        tickKey(mc.options.keyDown);
        tickKey(mc.options.keyLeft);
        tickKey(mc.options.keyRight);
        tickKey(mc.options.keyJump);
        tickKey(mc.options.keySprint);

        if (sneak.isEnabled()) {
            tickKey(mc.options.keyShift);
        }
    }

    private void tickKey(KeyMapping key) {
        if (key.getKey().getType() == InputConstants.Type.KEYSYM
                && key.getKey().getValue() != InputConstants.UNKNOWN.getValue()) {
            boolean raw = InputConstants.isKeyDown(
                    mc.getWindow().getWindow(),
                    key.getKey().getValue());
            key.setDown(raw);
        }
    }
}