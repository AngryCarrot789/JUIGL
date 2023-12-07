package reghzy.juigl.core.utils;

public final class BitUtils {
    private BitUtils() {
    }

    public static int add(int flags, int newBits) {
        return flags | newBits;
    }

    public static int sub(int flags, int newBits) {
        return flags & ~newBits;
    }

    public static boolean getFlag(int flags, int bit) {
        return (flags & bit) != 0;
    }

    public static int setFlag(int flags, int bit, boolean state) {
        return state ? (flags | bit) : (flags & ~bit);
    }
}
