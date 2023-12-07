package reghzy.juigl.utils;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class VeryUnsafe {
    public static Unsafe instance;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            instance = (Unsafe) f.get(null);
        }
        catch (Throwable e) {
            throw new Error("Could not access Unsafe. Some functionality relies on it!", e);
        }
    }
}
