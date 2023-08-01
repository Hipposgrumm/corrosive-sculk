package gg.hipposgrumm.corrosive_sculk.effects;

import gg.hipposgrumm.corrosive_sculk.sculk_damage_capability.SculkDamageCapability;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;

public class SculkResistanceEffect extends MobEffect {
    public SculkResistanceEffect() {
        super(MobEffectCategory.BENEFICIAL, 409156);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int level) {
        if (entity instanceof Player player) {
            player.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE)
                    .ifPresent(damageCapability -> damageCapability.removeProtection(level+1));
        }
        super.removeAttributeModifiers(entity, attributeMap, level);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int level) {
        if (entity instanceof Player player) {
            player.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE)
                    .ifPresent(damageCapability -> damageCapability.setProtection(level+1));
        }
        super.addAttributeModifiers(entity, attributeMap, level);
    }
}
