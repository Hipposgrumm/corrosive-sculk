package gg.hipposgrumm.corrosive_sculk.mixin;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import gg.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import gg.hipposgrumm.corrosive_sculk.mixin_helpers.HeartType;
import gg.hipposgrumm.corrosive_sculk.sculk_damage_capability.SculkDamageCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(Gui.class)
public abstract class HeartRenderMixin {
     @Shadow protected abstract void renderHearts(GuiGraphics p_282497_, Player p_168690_, int p_168691_, int p_168692_, int p_168693_, int p_168694_, float p_168695_, int p_168696_, int p_168697_, int p_168698_, boolean p_168699_);

     private static ResourceLocation SCULK_HEARTS_TEXTURE = new ResourceLocation(CorrosiveSculk.MODID, "textures/gui/hearts.png");

     private HeartType heart = HeartType.VANILLA;
     private int randomInt = 0;
     private int index = 0;
     private Pair<Integer,Integer> lastHeartPosition = new Pair<>(0,0);
     private List<Pair<HeartType,Boolean>> heartsList = new ArrayList<>();
     private float sculkWarningTransparency = 0.8F;
     private boolean sculkWarningDirection = true;

     @Redirect(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAbsorptionAmount()F"))
     private float corrosivesculk_addProtectionHearts(Player instance) {
          return instance.getAbsorptionAmount() + SculkDamageCapability.ClientData.getProtection()*2;
     }

     private void drawHeartWarning(GuiGraphics instance, int x, int y) {
          if (SculkDamageCapability.ClientData.getWarning()) {
               instance.setColor(1,1,1,sculkWarningTransparency);
               instance.blit(SCULK_HEARTS_TEXTURE, x, y, HeartType.SCULK.getxOffset(), 0, 9, 9);
               instance.setColor(1,1,1,1);
               int fps = Minecraft.getInstance().getFps();
               if (sculkWarningDirection) {
                    sculkWarningTransparency += 0.2/fps;
                    if (sculkWarningTransparency >= 0.9) sculkWarningDirection = false;
               } else {
                    sculkWarningTransparency -= 0.2/fps;
                    if (sculkWarningTransparency <= 0.8) sculkWarningDirection = true;
               }
          } else {
               sculkWarningTransparency = 0.8F;
          }
     }

     @Redirect(method = "renderHeart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
     private void corrosivesculk_renderCustomHearts(GuiGraphics instance, ResourceLocation texture_location, int screenX, int screenY, int textureX, int textureY, int width, int height) {
          boolean isSame = sameHeart(screenX, screenY) || heartsList.size()==0;
          if (!isSame && (index >= 1 && (heartsList.get(index-1).getSecond()))) {
               drawHeartWarning(instance, lastHeartPosition.getFirst(), lastHeartPosition.getSecond());
          }
          if (!isSame) lastHeartPosition = new Pair<>(screenX, screenY);
          if (!isSame) heart = heartsList.get(index).getFirst();
          if (heartsList.size()>0 && heart == HeartType.VANILLA) {
               instance.blit(texture_location, screenX, screenY, textureX, textureY, width, height);
          } else if (heartsList.size()>0) {
               instance.blit(SCULK_HEARTS_TEXTURE, screenX, screenY, heart.getxOffset(), textureY, 9, 9);
          }
          if (!isSame) index++;
     }

     private boolean sameHeart(int x, int y) {
          if (index == 0) return false;
          return (x == lastHeartPosition.getFirst()) && (y == lastHeartPosition.getSecond());
     }

     @Redirect(method = "renderHearts", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
     private int corrosivesculk_getRandomIntValue(RandomSource instance, int i) {
          randomInt = instance.nextInt(i);
          return randomInt;
     }

     @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
     private void corrosivesculk_initSetupHearts(GuiGraphics p_282497_, Player player, int p_168691_, int p_168692_, int p_168693_, int p_168694_, float p_168695_, int p_168696_, int p_168697_, int p_168698_, boolean p_168699_, CallbackInfo ci) {
          // p_168695_ is base health.
          // p_168698_ is extra health.
          int sculkResist = SculkDamageCapability.ClientData.getProtection();
          if ((sculkResist > 0) && (p_168698_==player.getAbsorptionAmount())) {
               ci.cancel();
               renderHearts(p_282497_,player,p_168691_,p_168692_,p_168693_,p_168694_,p_168695_,p_168696_,p_168697_,p_168698_+(sculkResist*2),p_168699_);
          } else {
               int extraHearts = ((int) player.getAbsorptionAmount() / 2) + sculkResist;
               int totalHearts = (int) (player.getMaxHealth() / 2) + extraHearts;
               for (int i = 1; i <= totalHearts; i++) {
                    if ((i > extraHearts) && (((i - 1) - extraHearts) < SculkDamageCapability.ClientData.getDamage())) {
                         heartsList.add(new Pair<>(HeartType.SCULK, false));
                    } else if (i <= sculkResist) {
                         heartsList.add(new Pair<>(HeartType.SCULK_RESIST, shouldWarnHeart(i,sculkResist,player.getAbsorptionAmount()/2,SculkDamageCapability.ClientData.getDamage())));
                    } else {
                         heartsList.add(new Pair<>(HeartType.VANILLA, shouldWarnHeart(i,sculkResist,player.getAbsorptionAmount()/2,SculkDamageCapability.ClientData.getDamage())));
                    }
               }
          }
     }

     private static boolean shouldWarnHeart(int index, int sculkResist, float absorption, int damage) {
          if (absorption > 0 && sculkResist <= 0) {
               return index <= absorption;
          } else {
               return index == damage+(sculkResist>0?1-damage:1);
          }
     }

     @Inject(method = "renderHearts", at = @At("TAIL"))
     private void corrosivesculk_renderWarningAndCleanUp(GuiGraphics p_282497_, Player p_168690_, int p_168691_, int p_168692_, int p_168693_, int p_168694_, float p_168695_, int p_168696_, int p_168697_, int p_168698_, boolean p_168699_, CallbackInfo ci) {
          if (heartsList.size()>0 && index >= 1 && heartsList.get(index-1).getSecond()) {
               drawHeartWarning(p_282497_, lastHeartPosition.getFirst(), lastHeartPosition.getSecond());
          }

          heart = HeartType.VANILLA;
          randomInt = 0;
          index = 0;
          lastHeartPosition = new Pair<>(0,0);
          heartsList.clear();
     }

     @Mixin(ForgeGui.class)
     public static class HeartAddingMixin {
          @Dynamic
          @Redirect(method = "renderHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAbsorptionAmount()F"))
          private float corrosivesculk_addProtectionHearts(Player instance) {
               return instance.getAbsorptionAmount() + SculkDamageCapability.ClientData.getProtection()*2;
          }
     }
}
