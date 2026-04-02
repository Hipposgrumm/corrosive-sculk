package dev.hipposgrumm.corrosive_sculk.mixin;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.hipposgrumm.corrosive_sculk.util.HelperMethodsForMixins;
import dev.hipposgrumm.corrosive_sculk.util.HelperMethodsForMixins.SculkHeart;
import dev.hipposgrumm.corrosive_sculk.util.HelperMethodsForMixins.WarnHeartData;
import dev.hipposgrumm.corrosive_sculk.capability.SculkDamageCapability;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui {
    //? if neoforge {
    /*@WrapOperation(method = "renderHealthLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAbsorptionAmount()F"))

    *///?} else {
    @WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAbsorptionAmount()F"))
    //?}
    private float corrosive_sculk$addProtectionHearts(Player instance, Operation<Float> original) {
        // Add hearts for rendering sculk resistance.
        float hearts = original.call(instance);
        SculkDamageCapability.ClientData data = SculkDamageCapability.ENTITIES.get(instance.getId());
        if (data != null) hearts += data.getMaxProtection()*2;
        return hearts;
    }

    @Expression("? + ? <= 4")
    @ModifyExpressionValue(method = "renderHearts", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean corrosive_sculk$shakeHeartsDespiteResistance( // and also initialize data for the heart rendering
            boolean original, @Local(ordinal = 4, argsOnly = true) int currentHealth, @Local(ordinal = 6, argsOnly = true) int absorption,
                                                                  @Local(ordinal = /*? if >1.20.1 {*//*10*//*?} else {*/11/*?}*/) int currentHeart,
                                                                  @Local(argsOnly = true) Player player,
                                                                  @Share("isSculkHeart") LocalBooleanRef isSculkHeart,
                                                                  @Share("lastDrawnHeart") LocalIntRef lastDrawnHeart,
                                                                  @Share("totalHearts") LocalIntRef totalHearts,
                                                                  @Share("clientData") LocalRef<SculkDamageCapability.ClientData> clientDataRef,
                                                                  @Share("hardcore") LocalBooleanRef hardcore) {
        SculkDamageCapability.ClientData clientData = clientDataRef.get();
        if (clientData == null) {
            lastDrawnHeart.set(-1);
            isSculkHeart.set(true); // Set sculk hearts begin.
            totalHearts.set(currentHeart); // Set the total heart count (for use later on).

            // Don't do these every time for every heart.
            clientData = SculkDamageCapability.ENTITIES.getOrDefault(player.getId(), SculkDamageCapability.ClientData.EMPTY);
            clientDataRef.set(clientData);
            hardcore.set(player.level().getLevelData().isHardcore());
        }

        return currentHealth + absorption - (clientData.getMaxProtection()*2) <= 4;
    }

    @WrapOperation(method = "renderHearts", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Gui$HeartType;"+/*? if >1.20.1 {*//*"IIZZZ)V"*//*?} else {*/"IIIZZ)V"/*?}*/))
    private void corrosive_sculk$drawSculkHearts(Gui instance, GuiGraphics guiGraphics, Gui.HeartType type, int x, int y, /*? if >1.20.1 {*//*boolean _hardcore*//*?} else {*/int textureY/*?}*/, boolean isHighlighted, boolean isHalfHeart, Operation<Void> original,
                                                 @Local(ordinal = /*? if >1.20.1 {*//*10*//*?} else {*/11/*?}*/) int currentHeart,
                                                 @Local(argsOnly = true) Player player,
                                                 @Share("isSculkHeart") LocalBooleanRef isSculkHeart,
                                                 @Share("lastDrawnHeart") LocalIntRef lastDrawnHeart,
                                                 @Share("totalHearts") LocalIntRef totalHearts,
                                                 @Share("clientData") LocalRef<SculkDamageCapability.ClientData> clientDataRef,
                                                 @Share("hardcore") LocalBooleanRef hardcore,
                                                 @Share("hasWarnedHeart") LocalBooleanRef hasWarnedHeart,
                                                 @Share("warnedHeart") LocalRef<WarnHeartData> warnedHeartRef,
                                                 @Share("absorbHeart") LocalRef<WarnHeartData[]> absorbHeartRef) {
        // This can initialize most important things, namely things that need to be initialized immediately.
        SculkDamageCapability.ClientData clientData = clientDataRef.get();

        currentHeart = totalHearts.get() - currentHeart; // Flip it around so that it goes from 0 to max.
        if (lastDrawnHeart.get() == currentHeart) return; // Skip drawing the heart if already drawn once.

        int heartState = SculkHeart.NORMAL;
        SculkHeart sculkHeart = null;
        boolean warnHeart = false;
        if (isSculkHeart.get()) { // If the next heart is being affected by the mod.
            int checkedHeart = currentHeart - clientData.getMaxProtection(); // Quick and dirty way to check if it's below protection hearts.
            if (checkedHeart < 0) { // Sculk Resistance
                if (checkedHeart < -clientData.getProtection()) {
                    heartState = SculkHeart.EMPTY;
                } else {
                    if (!hasWarnedHeart.get()) // If not already set.
                        warnHeart = true;
                }
                sculkHeart = new SculkHeart(SculkHeart.Type.SCULK_RESIST, hardcore.get(), false);
            } else {
                int absorbHearts = Mth.ceil(player.getAbsorptionAmount() / 2f);
                checkedHeart -= absorbHearts;
                if (checkedHeart < 0) { // Absorption
                    WarnHeartData[] warns = absorbHeartRef.get();
                    if (warns != null || !hasWarnedHeart.get()) {
                        if (warns == null) {
                            warns = new WarnHeartData[absorbHearts];
                            absorbHeartRef.set(warns);
                        }
                        warns[-1-checkedHeart] = new WarnHeartData(currentHeart, x, y);
                    }
                } else if (checkedHeart < clientData.getDamage()) { // Sculk Damage
                    sculkHeart = new SculkHeart(SculkHeart.Type.SCULK, hardcore.get(), false);
                } else {
                    if (!hasWarnedHeart.get()) // If the warning isn't already set to be rendered.
                        warnHeart = true; // First normal heart, signify that this is the one the warning goes on.
                    isSculkHeart.set(false); // Indicate that we're done drawing sculk hearts.
                }
            }
        }

        if (sculkHeart != null) {
            //? if >1.20.1 {
            /*guiGraphics.blitSprite(sculkHeart.getSprite(heartState), x, y, 9, 9);
            *///?} else {
            guiGraphics.blit(HelperMethodsForMixins.SCULK_HEARTS_TEXTURE, x, y, sculkHeart.xOffset(), heartState*9, 9, 9, 64, 32);
            //?}
            lastDrawnHeart.set(currentHeart); // Don't run through drawing this heart again, if it's a sculk heart.
        } else {
            original.call(instance, guiGraphics, type, x, y, /*? if >1.20.1 {*//*_hardcore*//*?} else {*/textureY/*?}*/, isHighlighted, isHalfHeart);
        }

        // Warning is drawn after the heart.
        if (warnHeart) {
            hasWarnedHeart.set(true);
            warnedHeartRef.set(new WarnHeartData(currentHeart, x, y));
        }
    }

    @Inject(method = "renderHearts", at = @At("TAIL"))
    private void corrosive_sculk$finishSculkHearts(GuiGraphics guiGraphics, Player player, int x, int y, int height, int offsetHeartIndex, float maxHealth, int currentHealth, int displayHealth, int absorptionAmount, boolean renderHighlight, CallbackInfo ci,
                                                   @Share("clientData") LocalRef<SculkDamageCapability.ClientData> clientDataRef,
                                                   @Share("hardcore") LocalBooleanRef hardcore,
                                                   @Share("warnedHeart") LocalRef<WarnHeartData> warnedHeartRef,
                                                   @Share("warnAbsorbHearts") LocalBooleanRef warnAbsorb,
                                                   @Share("absorbHeart") LocalRef<WarnHeartData[]> absorbHeartRef) {
        // Draw the heart warnings.
        // Can be multiple for absorption hearts.

        SculkDamageCapability.ClientData clientData = clientDataRef.get();
        if (clientData.getWarning() > 0) {
            SculkHeart heart = new SculkHeart(SculkHeart.Type.SCULK, hardcore.get(), false);
            WarnHeartData[] abswarn = absorbHeartRef.get();
            if (abswarn != null) {
                for (WarnHeartData w:abswarn) {
                    HelperMethodsForMixins.drawSculkWarning(guiGraphics, clientData, heart, w);
                }
                return;
            }
            WarnHeartData warn = warnedHeartRef.get();
            if (warn != null) {
                HelperMethodsForMixins.drawSculkWarning(guiGraphics, clientData, heart, warn);
            }
        }
    }

    @ModifyVariable(remap = false, method = "renderVehicleHealth", at = @At("STORE"), ordinal = 0)
    private int corrosive_sculk$initHorseHearts(int remainingHearts,
                                                         @Local LivingEntity horse,
                                                         @Share("isSculkHeart") LocalBooleanRef isSculkHeart,
                                                         @Share("lastDrawnHeart") LocalIntRef lastDrawnHeart,
                                                         @Share("totalHearts") LocalIntRef totalHearts,
                                                         @Share("clientData") LocalRef<SculkDamageCapability.ClientData> clientDataRef) {
        SculkDamageCapability.ClientData clientData = clientDataRef.get();
        if (clientData == null) {
            lastDrawnHeart.set(-1);
            isSculkHeart.set(true); // Set sculk hearts begin.
            clientData = SculkDamageCapability.ENTITIES.getOrDefault(horse.getId(), SculkDamageCapability.ClientData.EMPTY);
            clientDataRef.set(clientData);
            remainingHearts += clientData.getMaxProtection();
            totalHearts.set(remainingHearts); // This value decrements every row so we have to save it at the start.
        }

        return remainingHearts;
    }

    @WrapOperation(remap = false, method = "renderVehicleHealth", at = @At(
            remap = true, value = "INVOKE", target =
            //? if >1.20.1 {
            /*"Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"
            *///?} else {
            "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"
            //?}
    ))
    private void corrosive_sculk$changeHorseHearts(GuiGraphics instance, ResourceLocation texture_location, int x, int y, /*? if <=1.20.1 {*/int textureX, int textureY,/*?}*/ int width, int height, Operation<Void> original,
                                                            @Local(ordinal = 5) int row,
                                                            @Local(ordinal = 7) int currentHeart,
                                                            @Share("lastDrawnHeart") LocalIntRef lastDrawnHeart,
                                                            @Share("totalHearts") LocalIntRef totalHearts,
                                                            @Share("clientData") LocalRef<SculkDamageCapability.ClientData> clientDataRef,
                                                            @Share("warnedHeart") LocalRef<WarnHeartData> warnedHeartRef) {
        // This can initialize most important things, namely things that need to be initialized immediately.
        SculkDamageCapability.ClientData clientData = clientDataRef.get();

        currentHeart += row/2;
        currentHeart = totalHearts.get() - currentHeart - 1;
        if (lastDrawnHeart.get() == currentHeart) return; // Skip drawing the heart if already drawn once.

        // Counting hearts in decrementing order (last heart is 0).
        int heartState = SculkHeart.NORMAL;
        SculkHeart sculkHeart = null;
        boolean warnHeart = false;
        int checkedHeart = currentHeart - clientData.getMaxProtection(); // Quick and dirty way to check if it's below protection hearts.
        if (checkedHeart < 0) { // Sculk Resistance
            if (checkedHeart < -clientData.getProtection()) {
                heartState = SculkHeart.EMPTY;
            } else warnHeart = true;
            sculkHeart = new SculkHeart(SculkHeart.Type.SCULK_RESIST, false, true);
        } else if (checkedHeart < clientData.getDamage()) { // Sculk Damage
            sculkHeart = new SculkHeart(SculkHeart.Type.SCULK, false, true);
        }

        if (sculkHeart != null) {
            //? if >1.20.1 {
            /*instance.blitSprite(sculkHeart.getSprite(heartState), x, y, 9, 9);
            *///?} else {
            instance.blit(HelperMethodsForMixins.SCULK_HEARTS_TEXTURE, x, y, sculkHeart.xOffset(), heartState*9, 9, 9, 64, 32);
            //?}
            lastDrawnHeart.set(currentHeart); // Don't run through drawing this heart again, if it's a sculk heart.
        } else {
            original.call(instance, texture_location, x, y, /*? if <=1.20.1 {*/textureX, textureY,/*?}*/ width, height);
            warnHeart = true;
        }

        if (warnHeart) {
            // The warning is drawn after original heart.
            // Since we're in reverse, always be setting this.
            // Due to how horse hearts are rendered, we can get away with rendering this last.
            warnedHeartRef.set(new WarnHeartData(currentHeart, x, y));
        }
    }

    @Inject(remap = false, method = "renderVehicleHealth", at = @At("TAIL"))
    private void corrosive_sculk$finishHorseSculkHearts(GuiGraphics guiGraphics, CallbackInfo ci,
                                                                 @Share("clientData") LocalRef<SculkDamageCapability.ClientData> clientDataRef,
                                                                 @Share("warnedHeart") LocalRef<WarnHeartData> warnedHeartRef) {
        // Draw the warning, which will always be last.
        WarnHeartData warn = warnedHeartRef.get();
        SculkDamageCapability.ClientData clientData = clientDataRef.get();
        if (warn != null && clientData.getWarning() > 0) {
            SculkHeart heart = new SculkHeart(SculkHeart.Type.SCULK, false, true);
            HelperMethodsForMixins.drawSculkWarning(guiGraphics, clientData, heart, warn);
        }
    }
}
