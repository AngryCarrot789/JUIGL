package reghzy.juigl.core.dependency;

public final class UIPropertyMetaFlags {
    public static final int None = 0;
    public static final int AffectsMeasure = 1;
    public static final int AffectsArrange = 2;
    public static final int AffectsRender = 4;

    public static final int AffectsEntireLayout = AffectsMeasure | AffectsArrange | AffectsRender;

    private UIPropertyMetaFlags() { }
}
