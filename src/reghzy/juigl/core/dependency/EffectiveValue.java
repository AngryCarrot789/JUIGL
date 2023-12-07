package reghzy.juigl.core.dependency;

/**
 * A dependency object value entry, that stores a value associated with a property
 */
public class EffectiveValue {
    Object value;
    int propIndex;

    EffectiveValue(Object value, int propIndex) {
        this.value = value;
        this.propIndex = propIndex;
    }
}
