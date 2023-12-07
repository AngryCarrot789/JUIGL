package reghzy.juigl.core.dependency;

/**
 * A special access key for modifying a read-only dependency property. An instance of
 * this can only be created by the registerReadOnly method. It could also be created via reflection... ;)
 */
public final class DependencyPropertyKey {
    private final DependencyProperty property;

    DependencyPropertyKey(DependencyProperty property) {
        this.property = property;
    }

    public DependencyProperty getProperty() {
        return this.property;
    }
}
