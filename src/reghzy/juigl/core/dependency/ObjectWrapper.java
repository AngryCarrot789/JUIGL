package reghzy.juigl.core.dependency;

/**
 * An optional that allows null values while still being a valid value
 */
public class ObjectWrapper {
    private Object value;
    private boolean hasValue;

    public ObjectWrapper(Object value, boolean hasValue) {
        this.value = value;
        this.hasValue = hasValue;
    }

    public Object getValue() {
        return this.hasValue ? this.value : null;
    }

    public void setValue(Object value) {
        this.value = value;
        this.hasValue = true;
    }

    public void clearValue() {
        this.value = null;
        this.hasValue = false;
    }

    public boolean hasValue() {
        return this.hasValue;
    }
}
