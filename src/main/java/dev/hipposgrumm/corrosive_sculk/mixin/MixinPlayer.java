package dev.hipposgrumm.corrosive_sculk.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.hipposgrumm.corrosive_sculk.capability.SculkDamageCapability;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(Player.class)
public class MixinPlayer {
    @WrapOperation(method = "isHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getMaxHealth()F"))
    private float corrosive_sculk$limitHurtHealth(Player instance, Operation<Float> original) {
        AtomicInteger sculkHealth = new AtomicInteger();
        instance.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE)
                .ifPresent(damageCapability -> sculkHealth.set(damageCapability.getDamage()*2));
        return original.call(instance) - sculkHealth.get();
    }
}