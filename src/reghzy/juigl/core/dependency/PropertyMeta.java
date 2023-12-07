package reghzy.juigl.core.dependency;

import java.util.ArrayList;

public class PropertyMeta {
    private final ObjectWrapper defaultValue;
    private ArrayList<PropertyChangedHandler> changeChangers;
    private boolean isMetaSealed;

    ArrayList<PropertyMeta> children;

    public PropertyMeta() {
        this(new ObjectWrapper(null, false));
    }

    public PropertyMeta(Object defaultValue) {
        this(new ObjectWrapper(defaultValue, true));
    }

    public PropertyMeta(Object defaultValue, PropertyChangedHandler changeHandler) {
        this(new ObjectWrapper(defaultValue, true));
        this.addChangeHandler(changeHandler);
    }

    private PropertyMeta(ObjectWrapper defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Object getDefaultValue() {
        return this.defaultValue.getValue();
    }

    public void setDefaultValue(Object value) {
        this.validateNotSealed();
        this.defaultValue.setValue(value);
    }

    public void clearDefaultValue() {
        this.validateNotSealed();
        this.defaultValue.clearValue();
    }

    /**
     * Returns whether there is a default value explicitly defined for this metadata. When this is
     * true, {@link PropertyMeta#getDefaultValue()} may return a non-null value, or null if that's
     * the default value. When this is false, it is guaranteed to be null
     * @return A boolean
     */
    public boolean hasDefaultValueSet() {
        return this.defaultValue.hasValue();
    }

    public PropertyMeta addChangeHandler(PropertyChangedHandler handler) {
        if (handler == null)
            throw new IllegalArgumentException("Handler cannot be null");
        if (this.changeChangers == null)
            this.changeChangers = new ArrayList<>();
        else if (this.changeChangers.contains(handler))
            return this;
        this.changeChangers.add(handler);
        return this;
    }

    public boolean removeChangeHandler(PropertyChangedHandler handler) {
        if (handler == null)
            throw new IllegalArgumentException("Handler cannot be null");
        int index;
        if (this.changeChangers != null && (index = this.changeChangers.indexOf(handler)) != -1) {
            this.changeChangers.remove(index);
            return true;
        }
        
        return false;
    }
    
    public void seal() {
        this.isMetaSealed = true;
    }

    public boolean isSealed() {
        return this.isMetaSealed;
    }

    private void validateNotSealed() {
        if (this.isMetaSealed) {
            throw new IllegalStateException("Metadata is sealed; it cannot be modified anymore");
        }
    }

    public void onPropertyChanged(DependencyObject owner, DependencyProperty property, Object oldValue, Object newValue) {
        if (this.children != null) {
            for (PropertyMeta child : this.children) {
                child.onPropertyChanged(owner, property, oldValue, newValue);
            }
        }

        if (this.changeChangers != null) {
            for (PropertyChangedHandler handler : this.changeChangers) {
                handler.onPropertyChanged(owner, property, oldValue, newValue);
            }
        }
    }

    public void mergeInternal(PropertyMeta baseMetadata) {
        if (!this.hasDefaultValueSet() && baseMetadata.hasDefaultValueSet()) {
            this.defaultValue.setValue(baseMetadata.getDefaultValue());
        }

        ArrayList<PropertyChangedHandler> baseList = baseMetadata.changeChangers;
        if (baseList != null && baseList.size() > 0) {
            if (this.changeChangers == null)
                this.changeChangers = new ArrayList<>();
            this.changeChangers.addAll(0, baseList);
        }
    }
}
