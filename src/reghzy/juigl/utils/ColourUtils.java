package reghzy.juigl.utils;

public class ColourUtils {
    public static float rgbToFloat(int value) {
        if (value >= 255)
            return 1F;
        if (value <= 0)
            return 0F;
        return (float) value / 255F;
    }
}
