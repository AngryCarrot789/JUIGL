package reghzy.juigl.core.dependency;

import reghzy.juigl.utils.ClassUtils;
import reghzy.juigl.utils.Ref;
import sun.plugin.dom.exception.InvalidStateException;

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
    private final HashMap<Class<?>, PropertyMeta> metaMap;
    private final int globalIndex;

    private static final HashMap<DependencyPropertyPath, DependencyProperty> RegisteredProperties;
    private static final Object RegistrationLock = new Object();

    public static final Object UnsetValue = new NamedObject("UnsetValue");
    private DependencyPropertyKey accessKey;

    DependencyProperty(String name, Class<?> valueType, Class<?> primitiveType, Class<?> ownerType, PropertyMeta defaultMetaData, int globalIndex) {
        this.name = name;
        this.valueType = valueType;
        this.primitiveType = primitiveType;
        this.ownerType = ownerType;
        this.defaultMetaData = defaultMetaData;
        this.globalIndex = globalIndex;
        this.metaMap = new HashMap<>();
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
        return register(name, valueType, ownerType, null);
    }

    /**
     * Registers a new read-only dependency property for a target owner type with the given name. See the {@link DependencyProperty} javadocs for more info.
     * Read-only properties require an access key to actually modify, but can be read by anyone
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

    /**
     * Registers a new dependency property for a target owner type with the given name. See the {@link DependencyProperty} javadocs for more info
     * @param name      The property name
     * @param valueType The type of value this property stores
     * @param ownerType The class that owns this property
     * @return A new property, to be stored as a static final field in the owner type class
     * @throws RuntimeException An error occurred while registering a property
     */
    public static DependencyPropertyKey registerReadOnly(String name, Class<?> valueType, Class<?> ownerType) {
        return registerReadOnly(name, valueType, ownerType, null);
    }

    /**
     * Registers a new read-only dependency property for a target owner type with the given name. See the {@link DependencyProperty} javadocs for more info.
     * Read-only properties require an access key to actually modify, but can be read by anyone
     * @param name            The property name
     * @param valueType       The type of value this property stores
     * @param ownerType       The class that owns this property
     * @param defaultMetaData The default property metadata (may be null)
     * @return A new property, to be stored as a static final field in the owner type class
     * @throws RuntimeException An error occurred while registering a property
     */
    public static DependencyPropertyKey registerReadOnly(String name, Class<?> valueType, Class<?> ownerType, PropertyMeta defaultMetaData) {
        DependencyProperty property = registerCore(new DependencyPropertyPath(name, ownerType), valueType, defaultMetaData);
        DependencyPropertyKey key = new DependencyPropertyKey(property);
        property.accessKey = key;
        return key;
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

    public DependencyProperty addOwner(Class<?> ownerType, PropertyMeta metadata) {
        synchronized (RegistrationLock) {
            DependencyPropertyPath path = new DependencyPropertyPath(this.name, ownerType);
            if (RegisteredProperties.get(path) != null) {
                throw new RuntimeException("Property already registered in class '" + path.getOwnerType().getName() + "' with name '" + path.getName() + "'");
            }

            if (metadata != null) {
                this.overrideMetadata(ownerType, metadata);
            }

            RegisteredProperties.put(path, this);
            return this;
        }
    }

    private void prepareMetaOverride(Class<?> ownerType, PropertyMeta meta, Ref<PropertyMeta> baseMetaRef) {
        if (ownerType == null)
            throw new IllegalArgumentException("New owner type cannot be null");
        if (!DependencyObject.class.isAssignableFrom(ownerType))
            throw new IllegalArgumentException("New owner type is not an instance of dependency object: " + ownerType.getName());
        if (meta == null)
            throw new IllegalArgumentException("New metadata cannot be null");
        if (meta.isSealed())
            throw new IllegalStateException("The given metadata was sealed. It must not be sealed before being used as an override");
        if (this.metaMap.containsKey(ownerType))
            throw new RuntimeException("Metadata already overridden for owner type: " + ownerType.getName());
        if (!(baseMetaRef.value = this.getMetaData(ownerType.getSuperclass())).getClass().isAssignableFrom(meta.getClass()))
            throw new IllegalArgumentException("New metadata type does not match base metadata type");
    }

    public void overrideMetadata(Class<?> ownerType, PropertyMeta meta) {
        if (this.isReadOnly())
            throw new InvalidStateException("This property is read only; the meta cannot be overridden");
        Ref<PropertyMeta> baseMeta = new Ref<>();
        this.prepareMetaOverride(ownerType, meta, baseMeta);
        this.doMetaOverride(ownerType, meta, baseMeta.value);
    }

    private void doMetaOverride(Class<?> ownerType, PropertyMeta newMeta, PropertyMeta baseMeta) {
        newMeta.mergeInternal(baseMeta);
        newMeta.seal();
        this.metaMap.put(ownerType, newMeta);
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

    public PropertyMeta getMetaData(DependencyObject instance) {
        return this.getMetaData(instance.getClass());
    }

    public PropertyMeta getMetaData(Class<?> targetOwnerType) {
        if (this.metaMap.size() < 1)
            return this.defaultMetaData;
        for (Class<?> type = targetOwnerType; type != null; type = type.getSuperclass()) {
            PropertyMeta meta = this.metaMap.get(type);
            if (meta != null) {
                return meta;
            }
        }

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

    public boolean isReadOnly() {
        return this.accessKey != null;
    }

    public boolean isKeyValid(DependencyPropertyKey key) {
        return this.accessKey == key;
    }
}
