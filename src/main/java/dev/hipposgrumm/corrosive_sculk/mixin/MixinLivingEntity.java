package dev.hipposgrumm.corrosive_sculk.mixin;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import dev.hipposgrumm.corrosive_sculk.capability.SculkDamageCapability;
import dev.hipposgrumm.corrosive_sculk.config.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.atomic.AtomicInteger;

//? if fabric
/*import dev.hipposgrumm.corrosive_sculk.util.PersistentDataAccessor;*/
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MixinLivingEntity
    //? if fabric
        /*implements PersistentDataAccessor*/
{
    @WrapOperation(method = "setHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getMaxHealth()F"))
    private float corrosive_sculk$dontHealthBeyondSculk(LivingEntity instance, Operation<Float> original) {
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
        return original.call(instance) - sculkHealth.get();
    }

    //? if fabric {
    /*@WrapOperation(method = "checkTotemDeathProtection", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"))
    private void corrosive_sculk$sculkProtectOnTotemUsed(LivingEntity instance, float val, Operation<Void> original) {
        CorrosiveSculk.onTotemUsed(instance);
        original.call(instance, val);
    }

    @Expression("? != 0.0")
    @WrapOperation(method = "actuallyHurt", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean corrosive_sculk$applyAttackerDamage(float damage, float zero, Operation<Boolean> original, @Local(argsOnly = true) DamageSource source) {
        CorrosiveSculk.onEntityDamaged(damage, source, (LivingEntity) (Object) this);
        return original.call(damage, zero);
    }


    // https://github.com/Tutorials-By-Kaupenjoe/Fabric-Tutorial-1.19/commit/cc78d61db69d4434debfd6d611b56f23e6712493#diff-1fe4674af1876ee373bfa257de9ba5ec1cb9b307d9a294b7ccf846c6e29fb3da
    @Unique private SculkDamageCapability corrosive_sculk$data;

    @Override
    public SculkDamageCapability corrosive_sculk$getSculkData() {
        if(this.corrosive_sculk$data == null) {
            this.corrosive_sculk$data = new SculkDamageCapability();
        }

        return corrosive_sculk$data;
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void corrosive_sculk$readSculkData(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(CorrosiveSculk.MODID, Tag.TAG_COMPOUND)) {
            corrosive_sculk$getSculkData().loadNBTData(tag.getCompound(CorrosiveSculk.MODID));
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void corrosive_sculk$writeSculkData(CompoundTag tag, CallbackInfo ci) {
        if(corrosive_sculk$data != null) {
            CompoundTag data = corrosive_sculk$data.saveNBTData();
            tag.put(CorrosiveSculk.MODID, data);
        }
    }
    *///?}
}
