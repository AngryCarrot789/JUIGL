package reghzy.juigl.core.dependency;

import reghzy.juigl.utils.ClassUtils;

import java.util.HashMap;

/**
 * A class used to store information about a dependency property in a class. Properties are an alternative
 * way to store values, instead of storing them in the classes themselves. By doing this, there's more ways to
 * control value change events and ways to implement property binding (e.g. binding the background to a parent's background)
 * <p>
 *     Properties don't store the actual value; that is left up to the {@link DependencyObject}, which contains the
 *     {@link DependencyObject#getValue(DependencyProperty)} and {@link DependencyObject#setValue(DependencyProperty, Object)}
 *     (and also {@link DependencyObject#clearValue(DependencyProperty)} for more advanced usages)
 * </p>
 * <p>
 *     When registering a property, you supply a name, value type, owner type, and an optional default metadata.
 *     The name and owner type form a unique property path that can only be registered once (same as fields).
 *     <p>
 *         The value type can be both object types and primitive types. When using primitive types (e.g. int), the property
 *         cannot store null values, whereas the boxed versions of the primitive type (e.g. {@link Integer}) can be null.
 *         The default metadata for primitive based properties will always be set as non-null (unless the metadata is already
 *         sealed, in which case, the property registration throws an exception)
 *     </p>
 * </p>
 */
public class DependencyProperty {
    private final String name;
    private final Class<?> ownerType;
    private final Class<?> valueType;
    private final Class<?> primitiveType;
    private final PropertyMeta defaultMetaData;
    private final int globalIndex;

    private static final HashMap<DependencyPropertyPath, DependencyProperty> RegisteredProperties;
    private static final Object RegistrationLock = new Object();

    public static final Object UnsetValue = new Object();

    DependencyProperty(String name, Class<?> valueType, Class<?> primitiveType, Class<?> ownerType, PropertyMeta defaultMetaData, int globalIndex) {
        this.name = name;
        this.valueType = valueType;
        this.primitiveType = primitiveType;
        this.ownerType = ownerType;
        this.defaultMetaData = defaultMetaData;
        this.globalIndex = globalIndex;
    }

    static {
        RegisteredProperties = new HashMap<>();
    }

    public static DependencyProperty getProperty(DependencyObject owner, String name) {
        return getProperty(owner.getClass(), name);
    }

    public static DependencyProperty getProperty(Class<?> ownerType, String name) {
        DependencyPropertyPath path = new DependencyPropertyPath(name, null);
        DependencyProperty property;
        synchronized (RegisteredProperties) {
            do {
                path.ownerType = ownerType;
                if ((property = RegisteredProperties.get(path)) != null)
                    return property;
            } while ((ownerType = ownerType.getSuperclass()) != null);
        }

        return null;
    }

    /**
     * Registers a new dependency property for a target owner type with the given name. See the {@link DependencyProperty} javadocs for more info
     * @param name            The property name
     * @param valueType       The type of value this property stores
     * @param ownerType       The class that owns this property
     * @return A new property, to be stored as a static final field in the owner type class
     * @throws RuntimeException An error occurred while registering a property
     */
    public static DependencyProperty register(String name, Class<?> valueType, Class<?> ownerType) {
        return registerCore(new DependencyPropertyPath(name, ownerType), valueType, null);
    }

    /**
     * Registers a new dependency property for a target owner type with the given name. See the {@link DependencyProperty} javadocs for more info
     * @param name The property name
     * @param valueType The type of value this property stores
     * @param ownerType The class that owns this property
     * @param defaultMetaData The default property metadata (may be null)
     * @return A new property, to be stored as a static final field in the owner type class
     * @throws RuntimeException An error occurred while registering a property
     */
    public static DependencyProperty register(String name, Class<?> valueType, Class<?> ownerType, PropertyMeta defaultMetaData) {
        return registerCore(new DependencyPropertyPath(name, ownerType), valueType, defaultMetaData);
    }

    private static DependencyProperty registerCore(DependencyPropertyPath path, Class<?> valueType, PropertyMeta defaultMetaData) {
        // just in case a class that registers dependency properties does so off of the main thread
        synchronized (RegistrationLock) {
            if (RegisteredProperties.get(path) != null) {
                throw new RuntimeException("Property already registered in class '" + path.getOwnerType().getName() + "' with name '" + path.getName() + "'");
            }

            Object defValue;
            if (defaultMetaData == null) {
                defaultMetaData = new PropertyMeta();
                if (valueType.isPrimitive()) {
                    defaultMetaData.setDefaultValue(ClassUtils.getDefaultValue(valueType));
                }
            }
            else if (valueType.isPrimitive()) {
                if ((defaultMetaData.hasDefaultValueSet() && (defValue = defaultMetaData.getDefaultValue()) != null)) {
                    if (!ClassUtils.isBoxedValueInstanceOfPrimitiveType(defValue, valueType)) {
                        throw new RuntimeException("Incompatible default value for a primitive-based value type");
                    }
                }
                else {
                    if (defaultMetaData.isSealed())
                        throw new RuntimeException("Cannot use a null default value for a primitive type. This would have been replaced if the meta was not sealed");
                    defaultMetaData.setDefaultValue(ClassUtils.getDefaultValue(valueType));
                }
            }
            else if (defaultMetaData.hasDefaultValueSet()) {
                defValue = defaultMetaData.getDefaultValue();
                if (defValue != null && !valueType.isInstance(defValue)) {
                    throw new RuntimeException("Incompatible default value for the value type");
                }
            }

            defaultMetaData.seal();
            DependencyProperty property = new DependencyProperty(
                    path.getName(),
                    ClassUtils.getBoxedType(valueType),
                    valueType.isPrimitive() ? valueType : null,
                    path.getOwnerType(),
                    defaultMetaData,
                    RegisteredProperties.size());
            RegisteredProperties.put(path, property);
            return property;
        }
    }

    public String getName() {
        return this.name;
    }

    public Class<?> getOwnerType() {
        return this.ownerType;
    }

    public Class<?> getValueType() {
        return this.valueType;
    }

    public int getGlobalIndex() {
        return this.globalIndex;
    }

    public PropertyMeta getDefaultMetaData() {
        return this.defaultMetaData;
    }

    // TODO: overridable metadata
    public PropertyMeta getMetaData(DependencyObject instance) {
        return this.defaultMetaData;
    }

    /**
     * Gets the underlying primitive type that this property was registered with. If the property was registered as the boxed-type, this returns null
     * @return The primitive type, or null
     */
    public Class<?> getPrimitiveType() {
        return this.primitiveType;
    }

    public boolean isValidValueType(Object value) {
        return this.valueType.isInstance(value);
    }
}