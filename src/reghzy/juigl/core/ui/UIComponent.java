package reghzy.juigl.core.ui;

import org.joml.Vector2d;
import org.lwjgl.opengl.GL11;
import reghzy.juigl.Main;
import reghzy.juigl.core.LayoutManager;
import reghzy.juigl.core.Window;
import reghzy.juigl.core.dependency.DependencyObject;
import reghzy.juigl.core.dependency.DependencyProperty;
import reghzy.juigl.core.dependency.DependencyPropertyKey;
import reghzy.juigl.core.dependency.PropertyMeta;
import reghzy.juigl.core.dependency.UIPropertyMeta;
import reghzy.juigl.core.dependency.UIPropertyMetaFlags;
import reghzy.juigl.core.render.ComponentRenderData;
import reghzy.juigl.core.render.RenderContext;
import reghzy.juigl.core.utils.HAlign;
import reghzy.juigl.core.utils.Thickness;
import reghzy.juigl.core.utils.VAlign;
import reghzy.juigl.utils.Maths;
import reghzy.juigl.utils.MinMax;

import java.awt.*;
import java.util.ArrayList;

/**
 * The base component for all UI-based objects
 */
public class UIComponent extends DependencyObject {
    public static final DependencyProperty MarginProperty = DependencyProperty.register("Margin", Thickness.class, UIComponent.class, new PropertyMeta(Thickness.ZERO));
    public static final DependencyProperty HorizontalAlignmentProperty = DependencyProperty.register("HorizontalAlignment", HAlign.class, UIComponent.class, new PropertyMeta(HAlign.stretch));
    public static final DependencyProperty VerticalAlignmentProperty = DependencyProperty.register("VerticalAlignment", VAlign.class, UIComponent.class, new PropertyMeta(VAlign.stretch));
    public static final DependencyProperty WidthProperty = DependencyProperty.register("Width", double.class, UIComponent.class, new UIPropertyMeta(Double.NaN, UIPropertyMetaFlags.AffectsMeasure));
    public static final DependencyProperty HeightProperty = DependencyProperty.register("Height", double.class, UIComponent.class, new UIPropertyMeta(Double.NaN, UIPropertyMetaFlags.AffectsMeasure));
    public static final DependencyProperty MinWidthProperty = DependencyProperty.register("MinWidth", double.class, UIComponent.class, new UIPropertyMeta(0d, UIPropertyMetaFlags.AffectsMeasure));
    public static final DependencyProperty MaxWidthProperty = DependencyProperty.register("MaxWidth", double.class, UIComponent.class, new UIPropertyMeta(Double.POSITIVE_INFINITY, UIPropertyMetaFlags.AffectsMeasure));
    public static final DependencyProperty MinHeightProperty = DependencyProperty.register("MinHeight", double.class, UIComponent.class, new UIPropertyMeta(0d, UIPropertyMetaFlags.AffectsMeasure));
    public static final DependencyProperty MaxHeightProperty = DependencyProperty.register("MaxHeight", double.class, UIComponent.class, new UIPropertyMeta(Double.POSITIVE_INFINITY, UIPropertyMetaFlags.AffectsMeasure));
    public static final DependencyProperty BackgroundColourProperty = DependencyProperty.register("BackgroundColour", Color.class, UIComponent.class, new UIPropertyMeta(null, UIPropertyMetaFlags.AffectsRender));
    public static final DependencyProperty OwnerWindowProperty = DependencyProperty.register("OwnerWindow", Window.class, UIComponent.class, new PropertyMeta(null));
    static final DependencyPropertyKey ParentPropertyKey = DependencyProperty.registerReadOnly("Parent", Panel.class, UIComponent.class, new UIPropertyMeta(null, UIPropertyMetaFlags.AffectsEntireLayout));
    public static final DependencyProperty ParentProperty = ParentPropertyKey.getProperty();

    public static int FORCE_LAYOUT_COUNT = 0;

    private double desiredWidth;
    private double desiredHeight;
    private double renderPosX;
    private double renderPosY;
    private double renderWidth;
    private double renderHeight;
    private double lastMeasureConstraintWidth;
    private double lastMeasureConstraintHeight;
    private double unclippedDesiredWidth;
    private double unclippedDesiredHeight;
    private double lastArrangePosX;
    private double lastArrangePosY;
    private double lastArrangeWidth;
    private double lastArrangeHeight;
    private boolean isMeasureUsingClip;
    private boolean needClipBounds;
    private boolean isVisualDirty;
    private boolean isMeasureDirty;
    private boolean isArrangeDirty;
    private boolean neverMeasured;
    private boolean neverArranged;
    private boolean isMeasureInProgress;
    private boolean isArrangeInProgress;
    private boolean isMeasureDuringArrange;

    private boolean isAbsVisualPositionValid;
    private double absRenderPosX;
    private double absRenderPosY;

    private ComponentRenderData renderData;

    public UIComponent() {
        this.neverMeasured = true;
        this.neverArranged = true;
    }

    public final Vector2d getAbsolutePosition() {
        if (this.isAbsVisualPositionValid) {
            return new Vector2d(this.absRenderPosX, this.absRenderPosY);
        }

        if (this.isArrangeDirty) {
            throw new RuntimeException("Cannot get render position while arrangement is dirty");
        }

        ArrayList<UIComponent> chain = new ArrayList<>();
        UIComponent next_component = this;
        do {
            chain.add(next_component);
        } while ((next_component = next_component.getParent()) != null);

        double x = 0, y = 0;
        for (UIComponent component : chain) {
            x = x + component.renderPosX;
            y = y + component.renderPosY;
        }

        this.absRenderPosX = x;
        this.absRenderPosY = y;
        this.isAbsVisualPositionValid = true;
        return new Vector2d(x, y);
    }

    public final void measure(double availableWidth, double availableHeight) {
        // check if layout is not forced, and we are already measured and the last constraint matches the new one
        if (FORCE_LAYOUT_COUNT < 1 && !this.isMeasureDirty && !this.neverMeasured && Maths.equals(this.lastMeasureConstraintWidth, availableWidth) && Maths.equals(this.lastMeasureConstraintHeight, availableHeight)) {
            return;
        }

        this.neverMeasured = false;
        this.invalidateArrange();
        this.lastMeasureConstraintWidth = availableWidth;
        this.lastMeasureConstraintHeight = availableHeight;
        this.isMeasureInProgress = true;
        Vector2d finalSize = this.measureCore(availableWidth, availableHeight);
        this.isMeasureInProgress = false;
        this.desiredWidth = finalSize.x;
        this.desiredHeight = finalSize.y;
        this.isMeasureDirty = false;
    }

    protected final Vector2d measureCore(double availableWidth, double availableHeight) {
        Thickness margin = this.getMargin();
        MinMax minMax = new MinMax(this);
        double minW = minMax.minW, maxW = minMax.maxW, minH = minMax.minH, maxH = minMax.maxH;

        double desiredWidth = Maths.clamp(Math.max(availableWidth - (margin.left + margin.right), 0d), minW, maxW);
        double desiredHeight = Maths.clamp(Math.max(availableHeight - (margin.top + margin.bottom), 0d), minH, maxH);
        Vector2d desiredSize = this.measureOverride(desiredWidth, desiredHeight);
        desiredSize.x = Math.max(desiredSize.x, minW);
        desiredSize.y = Math.max(desiredSize.y, minH);
        double measuredDesiredWidth = desiredSize.x, measuredDesiredHeight = desiredSize.y;
        boolean isClipRequired = false;
        if (desiredSize.x > maxW) {
            desiredSize.x = maxW;
            isClipRequired = true;
        }

        if (desiredSize.y > maxH) {
            desiredSize.y = maxH;
            isClipRequired = true;
        }

        desiredSize.x += (margin.left + margin.right);
        desiredSize.y += (margin.top + margin.bottom);
        if (desiredSize.x > availableWidth) {
            desiredSize.x = availableWidth;
            isClipRequired = true;
        }

        if (desiredSize.y > availableHeight) {
            desiredSize.y = availableHeight;
            isClipRequired = true;
        }

        if (isClipRequired || desiredSize.x < 0d || desiredSize.y < 0d) {
            this.unclippedDesiredWidth = measuredDesiredWidth;
            this.unclippedDesiredHeight = measuredDesiredHeight;
            this.isMeasureUsingClip = true;
        }
        else {
            this.isMeasureUsingClip = false;
        }

        if (desiredSize.x < 0d)
            desiredSize.x = 0d;
        if (desiredSize.y < 0d)
            desiredSize.y = 0d;
        return desiredSize;
    }

    protected Vector2d measureOverride(double availableWidth, double availableHeight) {
        return new Vector2d(0);
    }

    public final void arrange(double x, double y, double availableWidth, double availableHeight) {
        if (this.isMeasureDirty || this.neverMeasured) {
            this.isMeasureDuringArrange = true;
            if (this.neverMeasured) {
                this.measure(availableWidth, availableHeight);
            }
            else {
                this.measure(this.lastMeasureConstraintWidth, this.lastMeasureConstraintHeight);
            }
            this.isMeasureDuringArrange = false;
        }

        if (FORCE_LAYOUT_COUNT < 1 && !this.isArrangeDirty && !this.neverArranged &&
            Maths.equals(x, this.lastArrangePosX) && Maths.equals(y, this.lastArrangePosY) &&
            Maths.equals(availableWidth, this.lastArrangeWidth) && Maths.equals(availableHeight, this.lastArrangeHeight)) {
            return;
        }

        double lastArrX = this.lastArrangePosX;
        double lastArrY = this.lastArrangePosY;
        double lastArrW = this.lastArrangeWidth;
        double lastArrH = this.lastArrangeHeight;

        this.neverArranged = false;
        this.isArrangeInProgress = true;
        this.arrangeCore(x, y, availableWidth, availableHeight);
        this.isArrangeInProgress = false;
        this.lastArrangePosX = x;
        this.lastArrangePosY = y;
        this.lastArrangeWidth = availableWidth;
        this.lastArrangeHeight = availableHeight;
        this.isArrangeDirty = false;
        this.isAbsVisualPositionValid = false;

        if (this.isVisualDirty() || !Maths.equals(this.lastArrangePosX, lastArrX) || !Maths.equals(this.lastArrangePosY, lastArrY) || !Maths.equals(this.lastArrangeWidth, lastArrW) || !Maths.equals(this.lastArrangeHeight, lastArrH)) {
            this.doRenderComponent();
        }
    }

    protected final void arrangeCore(double posX, double posY, double availableWidth, double availableHeight) {
        Thickness margin = this.getMargin();
        availableWidth = Math.max(availableWidth - (margin.left + margin.right), 0);
        availableHeight = Math.max(availableHeight - (margin.top + margin.bottom), 0);
        // a copy of the original availableSize parameter
        double cpyAvailableWidth = availableWidth;
        double cpyAvailableHeight = availableHeight;

        double dsWidth, dsHeight;
        if (this.isMeasureUsingClip) {
            dsWidth = this.unclippedDesiredWidth;
            dsHeight = this.unclippedDesiredHeight;
        }
        else {
            dsWidth = Math.max(this.desiredWidth - (margin.left + margin.right), 0);
            dsHeight = Math.max(this.desiredHeight - (margin.top + margin.bottom), 0);
        }

        // Check if there isn't enough space available, and if so, require clipping
        this.needClipBounds = false;
        if (dsWidth > availableWidth) {
            availableWidth = dsWidth;
            this.needClipBounds = true;
        }

        if (dsHeight > availableHeight) {
            availableHeight = dsHeight;
            this.needClipBounds = true;
        }

        // If alignment is stretch, then arrange using all of the available size
        // Otherwise, only use our desired size, leaving extra space for other components
        HAlign hAlign = this.getHorizontalAlignment();
        VAlign vAlign = this.getVerticalAlignment();
        if (hAlign != HAlign.stretch)
            availableWidth = dsWidth;
        if (vAlign != VAlign.stretch)
            availableHeight = dsHeight;

        MinMax minMax = new MinMax(this);
        double maxW = minMax.maxW, maxH = minMax.maxH;

        double maxOrDesiredWidth = Math.max(dsWidth, maxW);
        if (availableWidth > maxOrDesiredWidth) {
            availableWidth = maxOrDesiredWidth;
            this.needClipBounds = true;
        }

        double maxOrDesiredHeight = Math.max(dsHeight, maxH);
        if (availableHeight > maxOrDesiredHeight) {
            availableHeight = maxOrDesiredHeight;
            this.needClipBounds = true;
        }

        Vector2d arrangeSize = arrangeOverride(availableWidth, availableHeight);
        this.renderWidth = arrangeSize.x;
        this.renderHeight = arrangeSize.y;

        // The actual arranged width/height exceeds our max width/height, so clip
        double finalWidth = Math.min(arrangeSize.x, maxW);
        double finalHeight = Math.min(arrangeSize.y, maxH);
        if (!this.needClipBounds && ((finalWidth < arrangeSize.x) || (finalHeight < arrangeSize.y)))
            this.needClipBounds = true;
        if (!this.needClipBounds && ((cpyAvailableWidth < finalWidth) || (cpyAvailableHeight < finalHeight)))
            this.needClipBounds = true;

        if (finalWidth > cpyAvailableWidth)
            hAlign = HAlign.left;
        if (finalHeight > cpyAvailableHeight)
            vAlign = VAlign.top;

        double x,y;
        if (hAlign == HAlign.center || hAlign == HAlign.stretch)
            x = (cpyAvailableWidth - finalWidth) / 2.0;
        else if (hAlign == HAlign.right)
            x = cpyAvailableWidth - finalWidth;
        else x = 0;

        if (vAlign == VAlign.center || vAlign == VAlign.stretch)
            y = (cpyAvailableHeight - finalHeight) / 2.0;
        else if (vAlign == VAlign.bottom)
            y = cpyAvailableHeight - finalHeight;
        else y = 0;

        this.renderPosX = x + posX + margin.left;
        this.renderPosY = y + posY + margin.top;
    }

    protected Vector2d arrangeOverride(double availableWidth, double availableHeight) {
        return new Vector2d(availableWidth, availableHeight);
    }

    @Override
    protected void onPropertyChangedInternal(DependencyProperty property, PropertyMeta meta, Object oldValue, Object newValue) {
        super.onPropertyChangedInternal(property, meta, oldValue, newValue);
        if (!(meta instanceof UIPropertyMeta)) {
            return;
        }

        UIPropertyMeta uiMeta = (UIPropertyMeta) meta;
        if (uiMeta.affectsMeasure())
            this.invalidateMeasure();
        if (uiMeta.affectsArrange())
            this.invalidateArrange();
        if (uiMeta.affectsRender())
            this.invalidateVisual();
    }

    public void doRenderComponent() {
        this.renderData = null;
        RenderContext ctx = new RenderContext(1);
        this.onRender(ctx);
        this.renderData = new ComponentRenderData(ctx.getRenderData());
        this.isVisualDirty = false;
    }

    protected void onRender(RenderContext dc) {
        Color bg = this.getBackgroundColour();
        if (bg != null) {
            dc.drawRect(0, 0, this.renderWidth, this.renderHeight, bg);
        }
    }

    public void invalidateMeasure() {
        if (this.isMeasureDirty || this.isMeasureInProgress)
            return;
        if (!this.neverMeasured)
            LayoutManager.getLayoutManager().measureQueue.add(this);
        this.isMeasureDirty = true;
    }

    public void invalidateArrange() {
        if (this.isArrangeDirty || this.isArrangeInProgress)
            return;
        if (!this.neverArranged)
            LayoutManager.getLayoutManager().arrangeQueue.add(this);
        this.isArrangeDirty = true;
    }

    public void invalidateVisual() {
        this.invalidateArrange();
        this.isVisualDirty = true;
    }

    // Getters/Setters ////////////////////////////////////////////////////////////////////////////////////////////

    public boolean isMeasureDirty() { return this.isMeasureDirty; }
    public boolean isArrangeDirty() { return this.isArrangeDirty; }
    public boolean isVisualDirty() { return this.isVisualDirty; }
    public boolean hasNeverMeasured() { return this.neverMeasured; }
    public boolean hasNeverArranged() { return this.neverArranged; }
    public double getLastMeasureConstraintWidth() { return this.lastMeasureConstraintWidth; }
    public double getLastMeasureConstraintHeight() { return this.lastMeasureConstraintHeight; }
    public double getLastArrangePosX() { return this.lastArrangePosX; }
    public double getLastArrangePosY() { return this.lastArrangePosY; }
    public double getLastArrangeWidth() { return this.lastArrangeWidth; }
    public double getLastArrangeHeight() { return this.lastArrangeHeight; }
    public double getDesiredWidth() { return this.desiredWidth; }
    public double getDesiredHeight() { return this.desiredHeight; }
    public double getRenderPosX() { return this.renderPosX; }
    public double getRenderPosY() { return this.renderPosY; }
    public double getRenderWidth() { return this.renderWidth; }
    public double getRenderHeight() { return this.renderHeight; }

    public Thickness getMargin() { return (Thickness) getValue(MarginProperty); }
    public void setMargin(Thickness value) { setValue(MarginProperty, value); }

    public HAlign getHorizontalAlignment() { return (HAlign) getValue(HorizontalAlignmentProperty); }
    public void setHorizontalAlignment(HAlign value) { setValue(HorizontalAlignmentProperty, value); }

    public VAlign getVerticalAlignment() { return (VAlign) getValue(VerticalAlignmentProperty); }
    public void setVerticalAlignment(VAlign value) { setValue(VerticalAlignmentProperty, value); }

    public double getWidth() { return (double) getValue(WidthProperty); }
    public void setWidth(double value) { setValue(WidthProperty, value); }

    public double getHeight() { return (double) getValue(HeightProperty); }
    public void setHeight(double value) { setValue(HeightProperty, value); }

    public double getMinWidth() { return (double) getValue(MinWidthProperty); }
    public void setMinWidth(double value) { setValue(MinWidthProperty, value); }

    public double getMaxWidth() { return (double) getValue(MaxWidthProperty); }
    public void setMaxWidth(double value) { setValue(MaxWidthProperty, value); }

    public double getMinHeight() { return (double) getValue(MinHeightProperty); }
    public void setMinHeight(double value) { setValue(MinHeightProperty, value); }

    public Color getBackgroundColour() { return (Color) getValue(BackgroundColourProperty); }
    public void setBackgroundColour(Color value) { setValue(BackgroundColourProperty, value); }

    public double getMaxHeight() { return (double) getValue(MaxHeightProperty); }
    public void setMaxHeight(double value) { setValue(MaxHeightProperty, value); }

    // TODO: multi-window
    public Window getOwnerWindow() { return Main.mainWindow; }
    // public void setOwnerWindow(Window value) { setValue(OwnerWindowProperty, value); }

    public Panel getParent() { return (Panel) getValue(ParentProperty); }

    protected void onBeforeAddedToPanel(Panel parent) {
    }

    /**
     * Called when this component is added to a panel (meaning, its parent
     * changed). Our parent will equal the parameter object
     */
    protected void onAddedToPanel(Panel parent) {
        this.onParentChanged();
    }

    protected void onBeforeRemovedFromPanel(Panel parent) {
    }

    /**
     * Called when this component is removed from its panel (removed from its
     * parent). Our parent will be null, but the parameter object will be our previous parent
     */
    protected void onRemovedFromPanel(Panel parent) {
        this.onParentChanged();
    }

    /**
     * Called by both {@link UIComponent#onAddedToPanel(Panel)} and {@link UIComponent#onRemovedFromPanel(Panel)}
     */
    protected void onParentChanged() {
        this.invalidateMeasure();
        this.invalidateArrange();
        this.invalidateVisual();
    }

    public static void drawRecursive(UIComponent component) {
        GL11.glPushMatrix();

        Vector2d pos = component.getAbsolutePosition();
        GL11.glTranslated(pos.x, pos.y, 0d);

        if (component.renderData != null) {
            component.renderData.drawAll();
        }

        if (component instanceof Panel) {
            Panel panel = (Panel) component;
            for (int i = 0, count = panel.getVisualChildrenCount(); i < count; i++) {
                drawRecursive(panel.getVisualChild(i));
            }
        }

        GL11.glPopMatrix();
    }
}
