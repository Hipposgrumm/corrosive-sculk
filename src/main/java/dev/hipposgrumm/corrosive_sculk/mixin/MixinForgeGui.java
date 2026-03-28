package dev.hipposgrumm.corrosive_sculk.mixin;

//? if forgebase {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.hipposgrumm.corrosive_sculk.capability.SculkDamageCapability;
import dev.hipposgrumm.corrosive_sculk.util.HelperMethodsForMixins;
import dev.hipposgrumm.corrosive_sculk.util.HelperMethodsForMixins.SculkHeart;
import dev.hipposgrumm.corrosive_sculk.util.HelperMethodsForMixins.WarnHeartData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// data get entity 62ebbb7d-8bd7-4d92-b951-d0a799c37f92 ForgeCaps."corrosive_sculk:properties".damage
// data modify entity 62ebbb7d-8bd7-4d92-b951-d0a799c37f92 ForgeCaps."corrosive_sculk:properties".damage set value 0
@Mixin(ForgeGui.class)
public class MixinForgeGui {
    // This is separate on Forge for some reason.
    @WrapOperation(method = "renderHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAbsorptionAmount()F"))
    private float corrosive_sculk$addProtectionHearts_forgeGui(Player instance, Operation<Float> original) {
        // Add hearts for rendering sculk resistance.
        float hearts = original.call(instance);
        SculkDamageCapability.ClientData data = SculkDamageCapability.ENTITIES.get(instance.getId());
        if (data != null) hearts += data.getMaxProtection()*2;
        return hearts;
    }

    @ModifyVariable(remap = false, method = "renderHealthMount", at = @At("STORE"), ordinal = 4)
    private int corrosive_sculk$initHorseHearts_forgeGui(int remainingHearts,
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

    @WrapOperation(remap = false, method = "renderHealthMount", at = @At(remap = true, value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
    private void corrosive_sculk$changeHorseHearts_forgeGui(GuiGraphics instance, ResourceLocation texture_location, int x, int y, int textureX, int textureY, int width, int height, Operation<Void> original,
                                                            @Local(ordinal = 9) int row,
                                                            @Local(ordinal = 12) int currentHeart,
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
        SculkHeart sculkHeart = null;
        int checkedHeart = currentHeart - clientData.getMaxProtection(); // Quick and dirty way to check if it's below protection hearts.
        if (checkedHeart < 0) { // Sculk Resistance
            if (checkedHeart < -clientData.getProtection()) {
                textureY = 18;
            } else {
                textureY = 0;
            }
            sculkHeart = new SculkHeart(SculkHeart.Type.SCULK_RESIST, false, true);
        } else if (checkedHeart < clientData.getDamage()) { // Sculk Damage
            textureY = 0;
            sculkHeart = new SculkHeart(SculkHeart.Type.SCULK, false, true);
        }

        if (sculkHeart != null) {
            instance.blit(HelperMethodsForMixins.SCULK_HEARTS_TEXTURE, x, y, sculkHeart.xOffset(), textureY, 9, 9, 64, 32);
            lastDrawnHeart.set(currentHeart); // Don't run through drawing this heart again, if it's a sculk heart.
        } else {
            original.call(instance, texture_location, x, y, textureX, textureY, width, height);

            // The warning is drawn after original heart.
            // Since we're in reverse, always be setting this.
            // Due to how horse hearts are rendered, we can get away with rendering this last.
            warnedHeartRef.set(new WarnHeartData(currentHeart, x, y));
        }
    }

    @Inject(remap = false, method = "renderHealthMount", at = @At("TAIL"))
    private void corrosive_sculk$finishHorseSculkHearts_forgeGui(int width, int height, GuiGraphics guiGraphics, CallbackInfo ci,
                                                        @Share("clientData") LocalRef<SculkDamageCapability.ClientData> clientDataRef,
                                                        @Share("warnedHeart") LocalRef<WarnHeartData> warnedHeartRef) {
        // Draw the warning, which will always be last.
        WarnHeartData warn = warnedHeartRef.get();
        SculkDamageCapability.ClientData clientData = clientDataRef.get();
        if (warn != null && clientData.getWarning() > 0) {
            SculkHeart heart = new SculkHeart(SculkHeart.Type.SCULK, false, true);
            RenderSystem.enableBlend(); // Forge disables blend at the end of the function.
            HelperMethodsForMixins.drawSculkWarning(guiGraphics, clientData, heart, warn);
            RenderSystem.disableBlend();
        }
    }
}
//?}