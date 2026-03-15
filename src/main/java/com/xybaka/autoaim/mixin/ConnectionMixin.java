package com.xybaka.autoaim.mixin;

import com.xybaka.autoaim.util.RotationUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Connection.class)
public class ConnectionMixin {

    @ModifyVariable(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), argsOnly = true)
    private Packet<?> onSend(Packet<?> packet) {
        return RotationUtil.applySilentRotation(packet);
    }
}
