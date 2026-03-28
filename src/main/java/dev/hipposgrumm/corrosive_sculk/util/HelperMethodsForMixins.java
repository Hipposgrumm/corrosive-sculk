package dev.hipposgrumm.corrosive_sculk.util;

import dev.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import dev.hipposgrumm.corrosive_sculk.capability.SculkDamageCapability;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class HelperMethodsForMixins {
    public static final ResourceLocation SCULK_HEARTS_TEXTURE =
            //$ resourcelocation
            ResourceLocation.fromNamespaceAndPath
            (CorrosiveSculk.MODID, "textures/gui/sculk_hearts.png");

    public static void drawSculkWarning(GuiGraphics guiGraphics, SculkDamageCapability.ClientData clientData, SculkHeart heart, WarnHeartData warn) {
        guiGraphics.setColor(1, 1, 1, clientData.getWarning() / 100f);
        guiGraphics.blit(SCULK_HEARTS_TEXTURE, warn.x(), warn.y(), heart.xOffset(), 0, 9, 9, 64, 32);
        guiGraphics.setColor(1, 1, 1, 1);
    }

    public record SculkHeart(Type type, boolean hardcore, boolean horse) {
        public int xOffset() {
            int off = 0;
            if (type == Type.SCULK_RESIST) off += 27;
            if (hardcore) off += 9;
            else if (horse) off += 18;
            return off;
        }

        public enum Type {SCULK, SCULK_RESIST}
    }

    public record WarnHeartData(int warnedHeart, int x, int y) {}
}
