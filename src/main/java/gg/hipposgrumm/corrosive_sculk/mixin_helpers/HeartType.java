package gg.hipposgrumm.corrosive_sculk.mixin_helpers;

public enum HeartType {
    VANILLA(0, true),
    SCULK(0, false),
    SCULK_RESIST(18, false);

    private final int xOffset;
    private final boolean useVanillaContainer;

    HeartType(int xOffset, boolean useVanillaContainer) {
        this.xOffset = xOffset;
        this.useVanillaContainer = useVanillaContainer;
    }

    public int getxOffset() {
        return this.xOffset;
    }

    public boolean useContainer() {
        return useVanillaContainer;
    }
}
