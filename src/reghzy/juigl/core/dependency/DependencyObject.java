package reghzy.juigl.core.dependency;

import reghzy.juigl.core.dispatcher.DispatcherObject;
import reghzy.juigl.utils.ClassUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class DependencyObject extends DispatcherObject {
    // Maps a class to all properties defined explicitly in that class (not including overridden properties)
    private static final HashMap<Class<? extends DependencyObject>, ArrayList<DependencyProperty>> CLASS_TO_PROP_LIST;

    private EffectiveValueEntry[] values;
    private int valueCount;

    public DependencyObject() {

    }

    static {
        CLASS_TO_PROP_LIST = new HashMap<>();
    }

    protected int getInitialEffectiveValueCount() {
        return 2;
    }

    private void checkPropertyWriteAccess(DependencyProperty property) {
        if (property == null)
            throw new IllegalArgumentException("Property cannot be null");
        if (property.isReadOnly())
            throw new IllegalArgumentException("Property is read only. A key is required to modify the property's value");
    }

    private void checkPropertyWriteAccess(DependencyPropertyKey key) {
        if (key == null)
            throw new IllegalArgumentException("Property Key cannot be null");
        if (!key.getProperty().isKeyValid(key))
            throw new IllegalArgumentException("Property key is not allowed to modify this property in particular");
    }

    public Object getValue(DependencyProperty property) {
        Object finalValue;
        EffectiveValueEntry value = this.getValueEntry(this.findEntryIndexForProperty(property.getGlobalIndex()));
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
        this.checkPropertyWriteAccess(key);
        this.setValueCore(key.getProperty(), value);
    }

    public void setValue(DependencyProperty property, Object value) {
        this.checkPropertyWriteAccess(property);
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
        int entryIndex = this.findEntryIndexForProperty(property.getGlobalIndex());
        if (entryIndex >= 0) {
            EffectiveValueEntry entry = this.getValueEntry(entryIndex);
            Object oldValue = this.getValue(property);
            if (!Objects.equals(oldValue, value)) {
                assert entry != null;
                entry.value = value;
                this.onPropertyChangedInternal(property, property.getMetaData(this), oldValue, value);
            }
        }
        else {
            entryIndex = -(entryIndex + 1);
            this.InsertEntry(new EffectiveValueEntry(value, property.getGlobalIndex()), entryIndex);
            PropertyMeta meta = property.getMetaData(this);
            this.onPropertyChangedInternal(property, meta, meta.getDefaultValue(), value);
        }
    }

    protected void onPropertyChangedInternal(DependencyProperty property, PropertyMeta meta, Object oldValue, Object newValue) {
        meta.onPropertyChanged(this, property, oldValue, newValue);
    }

    public void clearValue(DependencyProperty property) {
        this.checkPropertyWriteAccess(property);
        this.clearValueCore(this.findEntryIndexForProperty(property.getGlobalIndex()), property, property.getMetaData(this));
    }

    public void clearValue(DependencyPropertyKey key) {
        this.checkPropertyWriteAccess(key);
        this.clearValueCore(this.findEntryIndexForProperty(key.getProperty().getGlobalIndex()), key.getProperty(), key.getProperty().getMetaData(this));
    }

    private void clearValueCore(int entryIndex, DependencyProperty property, PropertyMeta meta) {
        Object oldValue;
        EffectiveValueEntry entry = this.getValueEntry(entryIndex);
        if (entry == null || (oldValue = entry.value) == DependencyProperty.UnsetValue)
            return;
        entry.value = DependencyProperty.UnsetValue;
        this.RemoveEntry(entry.propIndex);
        this.onPropertyChangedInternal(property, meta, oldValue, meta.getDefaultValue());
    }

    private EffectiveValueEntry getValueEntry(int entryIndex) {
        if (entryIndex < 0)
            return null;
        return this.values[entryIndex];
    }

    private int findEntryIndexForProperty(int propertyIndex) {
        int max = this.valueCount;
        if (max < 1) {
            return -1;
        }

        int min = 0;
        while ((max - min) > 3) {
            int mid = (min + max) >>> 1;
            int idx = this.values[mid].propIndex;
            if (propertyIndex == idx)
                return mid;
            else if (propertyIndex < idx)
                max = mid;
            else
                min = mid + 1;
        }

        do {
            int idx = this.values[min].propIndex;
            if (idx == propertyIndex)
                return min;
            if (idx < propertyIndex)
                ++min;
            else
                break;
        } while (min < max);
        return -(min + 1);

        // int min = 0;
        // int max = this.valueTable.length - 1;
        // while (min <= max) {
        //     int mid = (min + max) >>> 1;
        //     int cmp = Integer.compare(this.valueTable[mid].propIndex, propertyIndex);
        //     if (cmp < 0)
        //         min = mid + 1;
        //     else if (cmp > 0)
        //         max = mid - 1;
        //     else return mid;
        // }
        // return -(min + 1);
    }

    private void InsertEntry(EffectiveValueEntry entry, int entryIndex) {
        if (this.valueCount > 0) {
            if (this.values.length == this.valueCount) {
                EffectiveValueEntry[] newArray = new EffectiveValueEntry[this.valueCount + 8];
                System.arraycopy(this.values, 0, newArray, 0, entryIndex);
                newArray[entryIndex] = entry;
                System.arraycopy(this.values, entryIndex, newArray, (entryIndex + 1), (this.valueCount - entryIndex));
                this.values = newArray;
            }
            else {
                System.arraycopy(this.values, entryIndex, this.values, (entryIndex + 1), (this.valueCount - entryIndex));
                this.values[entryIndex] = entry;
            }
        }
        else {
            if (this.values == null)
                this.values = new EffectiveValueEntry[this.getInitialEffectiveValueCount()];
            this.values[0] = entry;
        }

        this.valueCount++;
    }

    private void RemoveEntry(int i) {
        this.values[--this.valueCount].clear();
        System.arraycopy(this.values, i + 1, this.values, i, this.valueCount - i);
    }
}