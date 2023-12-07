package reghzy.juigl.core.dependency;

import reghzy.juigl.utils.ClassUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public class DependencyObject {
    // Maps a class to all properties defined explicitly in that class (not including overridden properties)
    private static final HashMap<Class<? extends DependencyObject>, ArrayList<DependencyProperty>> CLASS_TO_PROP_LIST;

    private SortedMap<DependencyProperty, EffectiveValue> entries;

    public DependencyObject() {

    }

    static {
        CLASS_TO_PROP_LIST = new HashMap<>();
    }

    public Object getValue(DependencyProperty property) {
        Object finalValue;
        EffectiveValue value = this.getEntryForProperty(property);
        if (value != null && (finalValue = value.value) != DependencyProperty.UnsetValue) {
            return finalValue;
        }

        PropertyMeta meta = property.getMetaData(this);
        if (meta.hasDefaultValueSet() && (finalValue = meta.getDefaultValue()) != DependencyProperty.UnsetValue) {
            return finalValue;
        }

        return null;
    }

    public void setValue(DependencyPropertyKey key, Object value) {
        if (key == null)
            throw new IllegalArgumentException("Property Key cannot be null");
        DependencyProperty property = key.getProperty();
        if (!property.isKeyValid(key))
            throw new IllegalArgumentException("Property key is not allowed to modify this property in particular");
        this.setValueCore(property, value);
    }

    public void setValue(DependencyProperty property, Object value) {
        if (property == null)
            throw new IllegalArgumentException("Property cannot be null");
        if (property.isReadOnly())
            throw new IllegalArgumentException("Property is read only. A key is required to modify the property's value");
        this.setValueCore(property, value);
    }

    private void setValueCore(DependencyProperty property, Object value) {
        if (value == DependencyProperty.UnsetValue) {
            this.clearValue(property);
            return;
        }

        // ensure value is compatible
        if (property.getPrimitiveType() != null) {
            if (value == null || !ClassUtils.isBoxedValueInstanceOfPrimitiveType(value, property.getPrimitiveType())) {
                throw new RuntimeException("New value is not compatible for this primitive-based property: !(" + ClassUtils.toTypeString(value) + " instanceof " + property.getPrimitiveType().getName() + ")");
            }
        }
        else if (value != null && !property.isValidValueType(value)) {
            throw new IllegalArgumentException("New value is not assignable to this property: !(" + ClassUtils.toTypeString(value) + " instanceof " + property.getValueType().getName() + ")");
        }

        // find existing entry to replace the value of, otherwise create one
        EffectiveValue entry = this.getEntryForProperty(property);
        if (entry != null) {
            Object oldValue = this.getValue(property);
            if (!Objects.equals(oldValue, value)) {
                entry.value = value;
                this.onPropertyChangedInternal(property, property.getMetaData(this), oldValue, value);
            }
        }
        else {
            this.getEntryMap().put(property, entry = new EffectiveValue(value, property.getGlobalIndex()));
            this.onPropertyChangedInternal(property, property.getMetaData(this), DependencyProperty.UnsetValue, value);
        }
    }

    protected void onPropertyChangedInternal(DependencyProperty property, PropertyMeta meta, Object oldValue, Object newValue) {
        meta.onPropertyChanged(this, property, oldValue, newValue);
    }

    public void clearValue(DependencyProperty property) {
        Object oldValue;
        EffectiveValue entry = this.getEntryForProperty(property);
        if (entry == null || (oldValue = entry.value) == DependencyProperty.UnsetValue)
            return;
        entry.value = DependencyProperty.UnsetValue;
        this.entries.remove(property);
        this.onPropertyChangedInternal(property, property.getMetaData(this), oldValue, DependencyProperty.UnsetValue);
    }

    private EffectiveValue getEntryForProperty(DependencyProperty property) {
        if (this.entries == null)
            return null;
        return entries.get(property);
    }

    private SortedMap<DependencyProperty, EffectiveValue> getEntryMap() {
        if (this.entries == null)
            this.entries = new TreeMap<>(Comparator.comparingInt(DependencyProperty::getGlobalIndex));
        return this.entries;
    }
}
