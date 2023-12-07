package reghzy.juigl.core.ui;

import org.joml.Vector2d;

import java.util.ArrayList;

/**
 * The base implementation for a component that can store child components. By default,
 * all child component measurements don't affect this panel's measurement, and their arrange
 * position is 0,0 and does not affect this panel's arrangement
 */
public class Panel extends UIComponent {
    private final ArrayList<UIComponent> visualChildren;

    public Panel() {
        this.visualChildren = new ArrayList<>();
    }

    @Override
    protected Vector2d arrangeOverride(double availableWidth, double availableHeight) {
        Vector2d arrange = super.arrangeOverride(availableWidth, availableHeight);
        for (int i = 0; i < this.visualChildren.size(); i++) {
            this.visualChildren.get(i).arrange(0, 0, availableWidth, availableHeight);
        }

        return arrange;
    }

    @Override
    protected Vector2d measureOverride(double availableWidth, double availableHeight) {
        Vector2d measure = super.measureOverride(availableWidth, availableHeight);
        for (int i = 0; i < this.visualChildren.size(); i++) {
            this.visualChildren.get(i).measure(availableWidth, availableHeight);
        }

        return measure;
    }

    public int getVisualChildrenCount() {
        return this.visualChildren.size();
    }

    public UIComponent getVisualChild(int index) {
        return this.visualChildren.get(index);
    }

    public boolean addChild(UIComponent component) {
        if (this.visualChildren.contains(component))
            return false;
        this.visualChildren.add(component);
        component.onBeforeAddedToPanel(this);
        component.setValue(ParentPropertyKey, this);
        component.onAddedToPanel(this);
        this.invalidateMeasure();
        return true;
    }

    public boolean removeChild(UIComponent component) {
        int index = this.visualChildren.indexOf(component);
        if (index == -1)
            return false;
        this.visualChildren.remove(index);
        component.onBeforeRemovedFromPanel(this);
        component.setValue(ParentPropertyKey, null);
        component.onRemovedFromPanel(this);
        this.invalidateMeasure();
        return true;
    }
}
