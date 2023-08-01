package gg.hipposgrumm.corrosive_sculk.mixin;

import gg.hipposgrumm.corrosive_sculk.sculk_damage_capability.SculkDamageCapability;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(LivingEntity.class)
public class PreventEntityHealingSculkHeartsMixin {
    @Redirect(method = "setHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getMaxHealth()F"))
    private float corrosivesculk_sethealthSculkCap(LivingEntity instance) {
        AtomicReference<Float> returnValue = new AtomicReference<>((float) 0);
        if (instance instanceof Player player) {
            player.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE)
                    .ifPresent(damageCapability -> returnValue.set(instance.getMaxHealth() - (damageCapability.getDamage()*2)));
        } else {
            returnValue.set(instance.getMaxHealth());
        }
        return returnValue.get();
    }

    @Mixin(Player.class)
    public static class AccountForSculkedHeartsWhenHurtMixin {
        @Redirect(method = "isHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getMaxHealth()F"))
        private float corrosivesculk_isHurtAccountsForSculkedHearts(Player instance) {
            AtomicReference<Float> returnValue = new AtomicReference<>(instance.getMaxHealth());
            instance.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE)
                    .ifPresent(damageCapability -> returnValue.set(instance.getMaxHealth() - (damageCapability.getDamage()*2)));
            return returnValue.get();
        }
    }
}
