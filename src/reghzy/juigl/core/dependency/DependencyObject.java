package reghzy.juigl.core.dependency;

import reghzy.juigl.utils.ClassUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class DependencyObject {
    // Maps a class to all properties defined explicitly in that class (not including overridden properties)
    private static final HashMap<Class<? extends DependencyObject>, ArrayList<DependencyProperty>> CLASS_TO_PROP_LIST;

    private EffectiveValue[] valueTable;

    public DependencyObject() {

    }

    static {
        CLASS_TO_PROP_LIST = new HashMap<>();
    }

    public Object getValue(DependencyProperty property) {
        Object finalValue;
        EffectiveValue value = this.findEntryForProperty(property.getGlobalIndex());
        if (value != null && (finalValue = value.value) != DependencyProperty.UnsetValue) {
            return finalValue;
        }

        PropertyMeta meta = property.getMetaData(this);
        if (meta.hasDefaultValueSet() && (finalValue = meta.getDefaultValue()) != DependencyProperty.UnsetValue) {
            return finalValue;
        }

        return null;
    }

    public void setValue(DependencyProperty property, Object value) {
        if (value == DependencyProperty.UnsetValue) {
            this.clearValue(property);
        }
        else {
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
            int index = this.findEntryIndexForGlobalIndex(property.getGlobalIndex());
            if (index >= 0) {
                Object oldValue = this.getValue(property);
                if (!Objects.equals(oldValue, value)) {
                    this.valueTable[index].value = value;
                    this.onPropertyChangedInternal(property, property.getMetaData(this), oldValue, value);
                }
            }
            else {
                index = -(index + 1);
                if (this.valueTable == null) {
                    this.valueTable = new EffectiveValue[index + 1];
                }
                else if (this.valueTable.length < index + 1) {
                    this.valueTable = Arrays.copyOf(this.valueTable, Math.max(index + 1, this.valueTable.length * 2));
                }

                this.valueTable[index] = new EffectiveValue(value, property.getGlobalIndex());
                this.onPropertyChangedInternal(property, property.getMetaData(this), DependencyProperty.UnsetValue, value);
            }
        }
    }

    protected void onPropertyChangedInternal(DependencyProperty property, PropertyMeta meta, Object oldValue, Object newValue) {
        meta.onPropertyChanged(this, property, oldValue, newValue);
    }

    public void clearValue(DependencyProperty property) {
        int index = this.findEntryIndexForGlobalIndex(property.getGlobalIndex());
        if (index >= 0) {
            EffectiveValue entry = this.valueTable[index];
            Object oldValue = entry.value;
            if (oldValue != DependencyProperty.UnsetValue) {
                this.valueTable[index].value = DependencyProperty.UnsetValue;
                this.onPropertyChangedInternal(property, property.getMetaData(this), oldValue, DependencyProperty.UnsetValue);
            }
        }
    }

    private EffectiveValue findEntryForProperty(int globalIndex) {
        if (this.valueTable == null)
            return null;
        int index = this.findEntryIndexForGlobalIndex(globalIndex);
        return index >= 0 ? this.valueTable[index] : null;
    }

    private int findEntryIndexForGlobalIndex(int globalIndex) {
        if (this.valueTable == null) {
            return -1;
        }

        int min = 0;
        int max = this.valueTable.length - 1;
        while (min <= max) {
            int mid = (min + max) >>> 1;
            int cmp = Integer.compare(this.valueTable[mid].propIndex, globalIndex);
            if (cmp < 0)
                min = mid + 1;
            else if (cmp > 0)
                max = mid - 1;
            else return mid;
        }

        return -(min + 1);
    }
}
