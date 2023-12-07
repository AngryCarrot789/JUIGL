package reghzy.juigl.core.dependency;

import reghzy.juigl.core.utils.BitUtils;

public class UIPropertyMeta extends PropertyMeta {
    private int flags;

    public UIPropertyMeta() {
    }

    public UIPropertyMeta(Object defaultValue) {
        super(defaultValue);
    }

    public UIPropertyMeta(Object defaultValue, int flags) {
        super(defaultValue);
        this.flags = flags;
    }

    public UIPropertyMeta(Object defaultValue, PropertyChangedHandler changeHandler) {
        super(defaultValue, changeHandler);
    }

    public UIPropertyMeta(Object defaultValue, int flags, PropertyChangedHandler changeHandler) {
        super(defaultValue, changeHandler);
        this.flags = flags;
    }

    public UIPropertyMeta setFlags(int flags) {
        this.flags = flags;
        return this;
    }

    @Override
    public UIPropertyMeta addChangeHandler(PropertyChangedHandler handler) {
        super.addChangeHandler(handler);
        return this;
    }

    public boolean affectsMeasure() { return BitUtils.getFlag(this.flags, UIPropertyMetaFlags.AffectsMeasure); }
    public boolean affectsArrange() { return BitUtils.getFlag(this.flags, UIPropertyMetaFlags.AffectsArrange); }
    public boolean affectsRender() { return BitUtils.getFlag(this.flags, UIPropertyMetaFlags.AffectsRender); }
}
