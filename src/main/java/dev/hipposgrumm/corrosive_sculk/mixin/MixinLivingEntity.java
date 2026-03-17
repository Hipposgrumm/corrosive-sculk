package dev.hipposgrumm.corrosive_sculk.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import dev.hipposgrumm.corrosive_sculk.capability.SculkDamageCapability;
import dev.hipposgrumm.corrosive_sculk.network.SculkDamageSyncPacket;
import dev.hipposgrumm.corrosive_sculk.util.NetworkHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
    @WrapOperation(method = "setHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getMaxHealth()F"))
    private float corrosive_sculk$dontHealthBeyondSculk(LivingEntity instance, Operation<Float> original) {
        AtomicInteger sculkHealth = new AtomicInteger();
        instance.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE)
                .ifPresent(damageCapability -> sculkHealth.set(damageCapability.getDamage()*2));
        return original.call(instance) - sculkHealth.get();
    }
}
