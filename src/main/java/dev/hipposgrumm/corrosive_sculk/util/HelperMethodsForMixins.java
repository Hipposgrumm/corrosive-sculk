package dev.hipposgrumm.corrosive_sculk.util;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import dev.hipposgrumm.corrosive_sculk.capability.SculkDamageCapability;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class HelperMethodsForMixins {
    //? if >1.20.1 {
    /*private static final ResourceLocation[] SPRITES;
    static {
        // This is because I am lazy and also don't want the entire top of the class to be covered in this.
        String[] types = new String[] {"sculkheart", "sculkresist"};
        String[] states = new String[] {"", "_half", "_empty"};
        String[] mods = new String[] {"", "_hardcore", "_horse"};
        SPRITES = new ResourceLocation[0b00011010+1];
        for (int t=0;t<types.length;t++) for (int s=0;s<states.length;s++) for (int m=0;m<mods.length;m++) {
            SPRITES[(t<<4)+(s<<2)+m] =
                    //$ resourcelocation
                    new ResourceLocation
                            (CorrosiveSculk.MODID, String.format("%s%s%s", types[t], states[s], mods[m]));
        }
    }

    /// See constants defined in {@link SculkHeart}
    public static ResourceLocation getSprite(int type, int state, int modifier) {
        return SPRITES[(type<<4)+(state<<2)+modifier];
    }
    *///?} else {
    public static final ResourceLocation SCULK_HEARTS_TEXTURE =
            //$ resourcelocation
            ResourceLocation.fromNamespaceAndPath
                    (CorrosiveSculk.MODID, "textures/gui/sculk_hearts.png");
    //?}

    public static void loadStaticData() {} // Dummy function to tell Java to load the class and initialize static data.

    public static void drawSculkWarning(GuiGraphics guiGraphics, SculkDamageCapability.ClientData clientData, SculkHeart heart, WarnHeartData warn) {
        guiGraphics.setColor(1, 1, 1, clientData.getWarning() / 100f);
        //? if >1.20.1 {
        /*RenderSystem.enableBlend();
        guiGraphics.blitSprite(heart.getSprite(SculkHeart.FULL), warn.x(), warn.y(), 9, 9);
        RenderSystem.disableBlend();
        *///?} else {
        guiGraphics.blit(SCULK_HEARTS_TEXTURE, warn.x(), warn.y(), heart.xOffset(), 0, 9, 9, 64, 32);
        //?}
        guiGraphics.setColor(1, 1, 1, 1);
    }

    public record SculkHeart(Type type, boolean hardcore, boolean horse) {
        public static final int NORMAL = 0;
        public static final int RESIST = 1;
        public static final int FULL = 0;
        public static final int HALF = 1;
        public static final int EMPTY = 2;
      //public static final int NORMAL = 0;
        public static final int HARDCORE = 1;
        public static final int HORSE = 2;

        //? if >1.20.1 {
        /*public ResourceLocation getSprite(int state) {
            int modifier;
            if (horse) modifier = HORSE;
            else if (hardcore) modifier = HARDCORE;
            else modifier = NORMAL;
            return HelperMethodsForMixins.getSprite(switch (type) {
                case SCULK -> NORMAL;
                case SCULK_RESIST -> RESIST;
            }, state, modifier);
        }
        *///?} else {
        public int xOffset() {
            int off = 0;
            if (type == Type.SCULK_RESIST) off += 27;
            if (hardcore) off += 9;
            else if (horse) off += 18;
            return off;
        }
        //?}

        public enum Type {SCULK, SCULK_RESIST}
    }

    public record WarnHeartData(int warnedHeart, int x, int y) {}
}
