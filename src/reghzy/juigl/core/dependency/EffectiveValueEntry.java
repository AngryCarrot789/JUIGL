package reghzy.juigl.core.dependency;

/**
 * A dependency object value entry, that stores a value associated with a property
 */
public class EffectiveValueEntry {
    Object value;
    int propIndex;

    EffectiveValueEntry(Object value, int propIndex) {
        this.value = value;
        this.propIndex = propIndex;
    }

    public void clear() {
        this.propIndex = -1;
        this.value = null;
    }
}
