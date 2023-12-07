package reghzy.juigl.utils;

public class ClassUtils {
    private static final Character CharZero = (char)0;
    private static final Float FloatZero = (float)0;
    private static final Double DoubleZero = (double)0;

    public static Object getDefaultValue(Class<?> type) {
        if (!type.isPrimitive())
            return null;
        if (type == Boolean.TYPE)
            return false;
        if (type == Character.TYPE)
            return (char) 0;
        if (type == Byte.TYPE)
            return (byte) 0;
        if (type == Short.TYPE)
            return (short) 0;
        if (type == Integer.TYPE)
            return 0;
        if (type == Long.TYPE)
            return (long) 0;
        if (type == Float.TYPE)
            return (float) 0;
        if (type == Double.TYPE)
            return (double) 0;
        if (type == Void.TYPE)
            return null;
        throw new Error("Unknown primitive type");
    }

    public static boolean isDefaultValue(Object value) {
        if (value == null)
            return true;
        Class<?> type = value.getClass();
        if (!type.isPrimitive())
            return false;
        if (type == Boolean.TYPE || type == Boolean.class)
            return Boolean.FALSE.equals(value);
        if (type == Character.TYPE || type == Character.class)
            return CharZero.equals(value);
        if (type == Byte.TYPE || type == Byte.class)
            return Byte.valueOf((byte) 0).equals(value);
        if (type == Short.TYPE || type == Short.class)
            return Short.valueOf((short) 0).equals(value);
        if (type == Integer.TYPE || type == Integer.class)
            return Integer.valueOf(0).equals(value);
        if (type == Long.TYPE || type == Long.class)
            return Long.valueOf(0L).equals(value);
        if (type == Float.TYPE || type == Float.class)
            return FloatZero.equals(value);
        if (type == Double.TYPE || type == Double.class)
            return DoubleZero.equals(value);
        if (type == Void.TYPE || type == Void.class)
            return false;
        throw new Error("Unknown primitive type");
    }

    public static Class<?> getBoxedType(Class<?> type) {
        if (!type.isPrimitive())
            return type;
        if (type == Boolean.TYPE)
            return Boolean.class;
        if (type == Character.TYPE)
            return Character.class;
        if (type == Byte.TYPE)
            return Byte.class;
        if (type == Short.TYPE)
            return Short.class;
        if (type == Integer.TYPE)
            return Integer.class;
        if (type == Long.TYPE)
            return Long.class;
        if (type == Float.TYPE)
            return Float.class;
        if (type == Double.TYPE)
            return Double.class;
        if (type == Void.TYPE)
            return Void.class;
        throw new Error("Unknown primitive type");
    }

    public static Class<?> getUnboxedType(Class<?> type) {
        if (!type.isPrimitive())
            return type;
        if (type == Boolean.class)
            return Boolean.TYPE;
        if (type == Character.class)
            return Character.TYPE;
        if (type == Byte.class)
            return Byte.TYPE;
        if (type == Short.class)
            return Short.TYPE;
        if (type == Integer.class)
            return Integer.TYPE;
        if (type == Long.class)
            return Long.TYPE;
        if (type == Float.class)
            return Float.TYPE;
        if (type == Double.class)
            return Double.TYPE;
        if (type == Void.class)
            return Void.TYPE;
        throw new Error("Unknown primitive type");
    }

    public static boolean isBoxedValueInstanceOfPrimitiveType(Object inputObject, Class<?> primitiveType) {
        if (!primitiveType.isPrimitive())
            throw new IllegalArgumentException("Class is not a primitive type");
        return inputObject != null && getBoxedType(primitiveType) == inputObject.getClass();
    }

    public static String toTypeString(Object nullableValue) {
        return nullableValue != null ? nullableValue.getClass().getName() : "null";
    }
}
