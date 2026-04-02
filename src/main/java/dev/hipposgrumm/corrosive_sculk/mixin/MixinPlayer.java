package dev.hipposgrumm.corrosive_sculk.mixin;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import dev.hipposgrumm.corrosive_sculk.capability.SculkDamageCapability;
import dev.hipposgrumm.corrosive_sculk.config.Config;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.atomic.AtomicInteger;

//? if fabric {
/*import dev.hipposgrumm.corrosive_sculk.util.PersistentDataAccessor;
*///?}

@Mixin(Player.class)
public class MixinPlayer {
    @WrapOperation(method = "isHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getMaxHealth()F"))
    private float corrosive_sculk$limitHurtHealth(Player instance, Operation<Float> original) {
        if (!Config.sculkHealCircumstance.hasSculkDamage) return original.call(instance);
        AtomicInteger sculkHealth = new AtomicInteger();
        SculkDamageCapability damageCapability =
                //? if neoforge {
                /*instance.getData(CorrosiveSculk.SCULK_DAMAGE_ATTACHMENT);
                *///?} elif forge {
                instance.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE).orElse(null);
                //?} else {
                /*((PersistentDataAccessor) instance).corrosive_sculk$getSculkData();
                *///?}
        sculkHealth.set(damageCapability.getDamage()*2);
        //noinspection MixinExtrasOperationParameters      // on fabric it sees the cast above and panics, specifically when the cast above is for `instance`
        return original.call(instance) - sculkHealth.get();
    }

    //? if fabric {
    /*@Expression("? != 0.0")
    @WrapOperation(method = "actuallyHurt", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean corrosive_sculk$applyAttackerDamage(float damage, float zero, Operation<Boolean> original, @Local(argsOnly = true) DamageSource source) {
        CorrosiveSculk.onEntityDamaged(damage, source, (LivingEntity) (Object) this);
        return original.call(damage, zero);
    }
    *///?}
}