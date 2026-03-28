package dev.hipposgrumm.corrosive_sculk.mixin;

//? if =1.20.1 && forge {
import dev.hipposgrumm.corrosive_sculk.capability.SculkDamageCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.mantle.client.ExtraHeartRenderHandler;

@Pseudo
@Mixin(ExtraHeartRenderHandler.class)
public class MixinDisableMantleHeartsRender {
    @Shadow(remap = false) @Final private Minecraft mc;

    @Inject(remap = false, method = "renderHealthbar", at = @At("HEAD"), cancellable = true)
    private void corrosive_sculk$disableMantleHeartRenderer(RenderGuiOverlayEvent.Pre event, CallbackInfo ci) {
        Entity entity = this.mc.getCameraEntity();
        if (entity == null) return;
        SculkDamageCapability.ClientData data = SculkDamageCapability.ENTITIES.getOrDefault(entity.getId(), SculkDamageCapability.ClientData.EMPTY);
        if (data.getDamage() > 0 || data.getMaxProtection() > 0 || data.getWarning() > 0) {
            ci.cancel();
        }
    }
}
//?}