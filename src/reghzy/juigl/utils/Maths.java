package reghzy.juigl.utils;

import org.joml.Vector3d;
import sun.misc.DoubleConsts;
import sun.misc.FloatConsts;

import java.util.Arrays;

public class Maths {
    public static final double PI = 3.1415926535897931d;
    public static final double PI_NEGATIVE = -3.141592653589d;
    public static final double PI_HALF = 1.5707963267945d;
    public static final double PI_HALF_NEGATIVE = -1.5707963267945d;
    public static final double PI_DOUBLE = 6.283185307178d;
    public static final double PI_DOUBLE_NEGATIVE = -6.283185307178d;
    public static final double RAD_TO_DEG_MULTIPLIER = (180d / Math.PI); // 57.2958
    public static final double DEG_TO_RAD_MULTIPLIER = (Math.PI / 180d); // 0.01745
    public static final double RAD_TO_DEG_FUCK_KNOWS_LOLOL = (360d / Math.PI); // 57.2958
    public static final double DEG_TO_RAD_FUCK_KNOWS_LOLOL = (Math.PI / 360d); // 0.01745
    private static final double D_SWS1 = DoubleConsts.SIGNIFICAND_WIDTH - 1;
    private static final float F_SWS1 = FloatConsts.SIGNIFICAND_WIDTH - 1;

    public static final double SMALL_ASF_D = 0.00001d;
    public static final float SMALL_ASF_F  = 0.00001f;

    public static final double EPSILON_D = Double.longBitsToDouble(4372995238176751616L);
    public static final float EPSILON_F = Float.intBitsToFloat(872415232);

    // public static int pow(int base, int exp) {
    //     int result = 1;
    //     while (true) {
    //         if ((exp & 1) != 0)
    //             result *= base;
    //         exp >>= 1;
    //         if (exp == 0)
    //             break;
    //         base *= base;
    //     }
    //     return result;
    // }

    public static double sigmoid(double x) {
        return 1d / (1d + Math.exp(-x));
    }

    public static double sigmoidDerivative(double x) {
        return x * (1d - x);
    }

    public static double exp(double x) {
        // e^x
        return Math.pow(Math.E, x);
    }

    public static int min(int a, int b) {
        return Math.min(a, b);
    }

    public static int min(int a, int b, int c) {
        return Math.min(a, Math.min(b, c));
    }

    public static int min(int a, int b, int c, int d) {
        return Math.min(a, Math.min(b, Math.min(c, d)));
    }

    public static int min(int a, int b, int c, int d, int e) {
        return Math.min(a, Math.min(b, Math.min(c, Math.min(d, e))));
    }

    public static int min(int a, int b, int c, int d, int e, int f) {
        return Math.min(a, Math.min(b, Math.min(c, Math.min(d, Math.min(e, f)))));
    }

    public static int min(int... values) {
        int length;
        if (values == null || (length = values.length) == 0) {
            throw new IllegalArgumentException("No arguments provided");
        }

        int min = values[0], val;
        for (int i = 1; i < length; i++) {
            if ((val = values[i]) < min) {
                min = val;
            }
        }

        return min;
    }

    public static int max(int a, int b) {
        return Math.max(a, b);
    }

    public static int max(int a, int b, int c) {
        return Math.max(a, Math.max(b, c));
    }

    public static int max(int a, int b, int c, int d) {
        return Math.max(a, Math.max(b, Math.max(c, d)));
    }

    public static int max(int a, int b, int c, int d, int e) {
        return Math.max(a, Math.max(b, Math.max(c, Math.max(d, e))));
    }

    public static int max(int a, int b, int c, int d, int e, int f) {
        return Math.max(a, Math.max(b, Math.max(c, Math.max(d, Math.max(e, f)))));
    }

    public static int max(int... values) {
        int length;
        if (values == null || (length = values.length) == 0) {
            throw new IllegalArgumentException("No arguments provided");
        }

        int max = values[0], val;
        for (int i = 1; i < length; i++) {
            if ((val = values[i]) > max) {
                max = val;
            }
        }
        return max;
    }

    // float

    public static float min(float a, float b) {
        return Math.min(a, b);
    }

    public static float min(float a, float b, float c) {
        return Math.min(a, Math.min(b, c));
    }

    public static float min(float a, float b, float c, float d) {
        return Math.min(a, Math.min(b, Math.min(c, d)));
    }

    public static float min(float a, float b, float c, float d, float e) {
        return Math.min(a, Math.min(b, Math.min(c, Math.min(d, e))));
    }

    public static float min(float a, float b, float c, float d, float e, float f) {
        return Math.min(a, Math.min(b, Math.min(c, Math.min(d, Math.min(e, f)))));
    }

    public static float min(float... values) {
        int length;
        if (values == null || (length = values.length) == 0) {
            throw new IllegalArgumentException("No arguments provided");
        }

        float min = values[0], val;
        for (int i = 1; i < length; i++) {
            if ((val = values[i]) < min) {
                min = val;
            }
        }

        return min;
    }

    public static float max(float a, float b) {
        return Math.max(a, b);
    }

    public static float max(float a, float b, float c) {
        return Math.max(a, Math.max(b, c));
    }

    public static float max(float a, float b, float c, float d) {
        return Math.max(a, Math.max(b, Math.max(c, d)));
    }

    public static float max(float a, float b, float c, float d, float e) {
        return Math.max(a, Math.max(b, Math.max(c, Math.max(d, e))));
    }

    public static float max(float a, float b, float c, float d, float e, float f) {
        return Math.max(a, Math.max(b, Math.max(c, Math.max(d, Math.max(e, f)))));
    }

    public static float max(float... values) {
        int length;
        if (values == null || (length = values.length) == 0) {
            throw new IllegalArgumentException("No arguments provided");
        }

        float max = values[0], val;
        for (int i = 1; i < length; i++) {
            if ((val = values[i]) > max) {
                max = val;
            }
        }
        return max;
    }

    public static double min(double a, double b) {
        return Math.min(a, b);
    }

    public static double min(double a, double b, double c) {
        return Math.min(a, Math.min(b, c));
    }

    public static double min(double a, double b, double c, double d) {
        return Math.min(a, Math.min(b, Math.min(c, d)));
    }

    public static double min(double a, double b, double c, double d, double e) {
        return Math.min(a, Math.min(b, Math.min(c, Math.min(d, e))));
    }

    public static double min(double a, double b, double c, double d, double e, double f) {
        return Math.min(a, Math.min(b, Math.min(c, Math.min(d, Math.min(e, f)))));
    }

    public static double min(double... values) {
        int length;
        if (values == null || (length = values.length) == 0) {
            throw new IllegalArgumentException("No arguments provided");
        }

        double min = values[0], val;
        for (int i = 1; i < length; i++) {
            if ((val = values[i]) < min) {
                min = val;
            }
        }
        return min;
    }

    public static double max(double a, double b) {
        return Math.max(a, b);
    }

    public static double max(double a, double b, double c) {
        return Math.max(a, Math.max(b, c));
    }

    public static double max(double a, double b, double c, double d) {
        return Math.max(a, Math.max(b, Math.max(c, d)));
    }

    public static double max(double a, double b, double c, double d, double e) {
        return Math.max(a, Math.max(b, Math.max(c, Math.max(d, e))));
    }

    public static double max(double a, double b, double c, double d, double e, double f) {
        return Math.max(a, Math.max(b, Math.max(c, Math.max(d, Math.max(e, f)))));
    }

    public static double max(double... values) {
        int length;
        if (values == null || (length = values.length) == 0) {
            throw new IllegalArgumentException("No arguments provided");
        }

        double max = values[0], val;
        for (int i = 1; i < length; i++) {
            if ((val = values[i]) > max) {
                max = val;
            }
        }

        return max;
    }

    /**
     * Clamps the given value between the given min and max values
     * @param value The value
     * @param min   The smallest possible value to be returned
     * @param max   The biggest possible value to be returned
     * @return The value, min, or max
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(Math.min(value, max), min);
    }

    /**
     * Clamps the given value between the given min and max values
     * @param value The value
     * @param min   The smallest possible value to be returned
     * @param max   The biggest possible value to be returned
     * @return The value, min, or max
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(Math.min(value, max), min);
    }

    /**
     * Clamps the given value between the given min and max values
     * @param value The value
     * @param min   The smallest possible value to be returned
     * @param max   The biggest possible value to be returned
     * @return The value, min, or max
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(Math.min(value, max), min);
    }

    public static int getQuarter(int value) {
        return value / 4;
    }

    public static int getThreeQuarters(int value) {
        return (value / 4) * 3;
    }

    /**
     * Checks if two double values are very close together (within 5 decimal places)
     * @param a Value A
     * @param b Value B
     * @return True is A and B are the same within 5 decimal places
     */
    public static boolean equals(double a, double b) {
        return equals(a, b, SMALL_ASF_D);
    }

    /**
     * Checks if two double values are very close together within the given difference (epsilon)
     * @param a Value A
     * @param b Value B
     * @return True if the difference between A and B is smaller or equal to the given epsilon
     */
    public static boolean equals(double a, double b, double epsilon) {
        return Math.abs(a - b) <= epsilon;
    }

    public static int pow(int value, int exponent) {
        while (exponent > 1) {
            exponent--;
            value *= value;
        }

        return value;
    }

    public static double round(double value, int places) {
        // round(1750.087463, 3) == 1750.087
        //   factor                = 1000     (10^3)
        //   round(value * factor) = 1750087  (1750087.463 up)
        //   1750087 / factor      = 1750.087 (1750087 / 1000)

        // round(1750.087463, -2) == 1800
        //   factor                = 0.01 (10^-2)
        //   round(value * factor) = 18   (17.50087 up)
        //   18 / factor           = 1800 (18 / 0.01)

        if (places == 0) {
            return Maths.round(value);
        }
        else {
            double factor = Math.pow(10d, places);
            return (double) Math.round(value * factor) / factor;
        }
    }

    public static float round(float value, int places) {
        if (places == 0) {
            return Maths.round(value);
        }
        else {
            double factor = Math.pow(10d, places);
            return (float) (Math.round(value * factor) / factor);
        }
    }

    public static boolean isBetween(int value, int min, int max) {
        return value >= min && value <= max;
    }

    public static boolean isBetween(double value, double min, double max) {
        return value >= min && value <= max;
    }

    public static boolean isBetween(long value, long min, long max) {
        return value >= min && value <= max;
    }

    // alternative name: maxOfMultiple

    /**
     * Clamps the given value to the highest value of the given multiple (e.g getMaxOfMultiple(37, 16) == 48)
     * @param value    The value
     * @param multiple The multiple
     * @return The value (will never be more than value + multiple, it will always be below)
     */
    public static int ceil(int value, int multiple) {
        int mod = value % multiple;
        return mod == 0 ? value : value + (multiple - mod);
    }

    /**
     * Linear interpolation between 2 values,
     * @param a          Start value
     * @param b          End value
     * @param multiplier Lerp multiplier (between 0 and 1, typically)
     * @return A linearly interpolated value, equal to or between start and end (typically)
     */
    public static double lerp(double a, double b, double multiplier) {
        return a + (multiplier * (b - a));
    }

    /**
     * Inverse linear interpolation. Returns the 'multiplier' part from the a, b and output of the lerp function
     * @param a     Start value
     * @param b     End value
     * @param value The value, equal to or between start and end
     * @return The multiplier value, between 0 and 1 (typically)
     */
    public static double lerpInverse(double a, double b, double value) {
        return (value - a) / (b - a);
    }

    /**
     * Linear interpolation between the x, y and z parts of the given vectors,
     * @param a          Start vertex
     * @param b          End vertex
     * @param multiplier Lerp multiplier (between 0 and 1, typically)
     * @return A linearly interpolated vertex, equal to or between start and end (typically)
     */
    public static Vector3d lerp(Vector3d a, Vector3d b, double multiplier) {
        return new Vector3d(lerp(a.x, b.x, multiplier), lerp(a.y, b.y, multiplier), lerp(a.z, b.z, multiplier));
    }

    /**
     * Inverse linear interpolation of 'value'. Returns a vector containing the 'multiplier' between a and b
     * @param a     Start vertex
     * @param b     End vertex
     * @param value The value, equal to or between start and end
     * @return The multiplier vector, containing the inverse lerp of the start and end vector's parts
     */
    public static Vector3d lerpInverseAlt(Vector3d a, Vector3d b, Vector3d value) {
        return new Vector3d(lerpInverse(a.x, b.x, value.x), lerpInverse(a.y, b.y, value.y), lerpInverse(a.z, b.z, value.z));
    }

    /**
     * Inverse linear interpolation of 'value'. Returns a 'multiplier' between a and b
     * @param a     Start vertex
     * @param b     End vertex
     * @param value The value, equal to or between start and end
     * @return The multiplier value, between 0 and 1 (typically)
     */
    public static double lerpInverse(Vector3d a, Vector3d b, Vector3d value) {
        Vector3d v = new Vector3d(b).sub(a);
        return new Vector3d(value).sub(a).dot(v) / v.lengthSquared();
    }

    public static long floor(double value) {
        int exponent = Math.getExponent(value);
        if (exponent < 0) {
            return (long) ((value == 0.0) ? value : ((value < 0.0) ? -1.0 : 0.0));
        }
        else if (exponent >= D_SWS1) { // significant width - 1
            return (long) value;
        }

        long bits = Double.doubleToRawLongBits(value);
        long mask = DoubleConsts.SIGNIF_BIT_MASK >> exponent;
        if ((mask & bits) == 0L) {
            return (long) value;
        }
        else {
            double result = Double.longBitsToDouble(bits & (~mask));
            if (-1.0 * value > 0.0) {
                result = result + -1.0;
            }

            return (long) result;
        }
    }

    public static int ifloor(double value) {
        return (int) floor(value);
    }

    public static int floor(float value) {
        int exponent = Math.getExponent(value);
        if (exponent < 0) {
            return (int) ((value == 0.0f) ? value : ((value < 0.0f) ? -1.0 : 0.0));
        }
        else if (exponent >= F_SWS1) { // significant width - 1
            return (int) value;
        }

        int bits = Float.floatToRawIntBits(value);
        int mask = FloatConsts.SIGNIF_BIT_MASK >> exponent;
        if ((mask & bits) == 0) {
            return (int) value;
        }
        else {
            float result = Float.intBitsToFloat(bits & (~mask));
            if (-1.0f * value > 0.0f) {
                result = result + -1.0f;
            }

            return (int) result;
        }
    }

    public static long ceil(double value) {
        int exponent = Math.getExponent(value);
        if (exponent < 0) {
            return ((value == 0.0) ? ((long) value) : ((value < 0.0) ? 0 : 1));
        }
        else if (exponent >= D_SWS1) {
            return (long) value;
        }

        long bits = Double.doubleToRawLongBits(value);
        long mask = DoubleConsts.SIGNIF_BIT_MASK >> exponent;
        if ((mask & bits) == 0L) {
            return (long) value;
        }
        else {
            double result = Double.longBitsToDouble(bits & (~mask));
            return value > 0.0 ? (long) (result + 1.0) : (long) result;
        }
    }

    public static int iceil(double value) {
        return (int) ceil(value);
    }

    public static int ceil(float value) {
        int exponent = Math.getExponent(value);
        if (exponent < 0) {
            return ((value == 0.0f) ? ((int) value) : ((value < 0.0f) ? 0 : 1));
        }
        else if (exponent >= F_SWS1) {
            return (int) value;
        }

        int bits = Float.floatToRawIntBits(value);
        int mask = FloatConsts.SIGNIF_BIT_MASK >> exponent;
        if ((mask & bits) == 0) {
            return (int) value;
        }
        else {
            float result = Float.intBitsToFloat(bits & (~mask));
            return value > 0.0f ? (int) (result + 1.0f) : (int) result;
        }
    }

    /**
     * Truncates a double-precision floating point number towards negative infinity. Examples:
     * <p><code>floor(4.5) == (int)4.5 == 4</code></p>
     * <p><code>floor(-4.5) == -5</code></p>
     * <p><code>(int)-4.5 == -4</code></p>
     */
    public static long fastfloor(double value) {
        long cast = (long) value;
        if ((double) cast == value) {
            return cast;
        }
        else {
            return cast - (Double.doubleToRawLongBits(value) >>> 63);
        }
    }

    /**
     * A faster alternative to {@link Math#floor(double)} or {@link Maths#ifloor(double)}
     * <p>
     * Truncates a double-precision floating point number towards negative infinity. Examples:
     * <p><code>floor(4.5) == (int)4.5 == 4</code></p>
     * <p><code>floor(-4.5) == -5</code></p>
     * <p><code>(int)-4.5 == -4</code></p>
     * </p>
     */
    public static int ifastfloor(double value) {
        int cast = (int) value;
        if ((double) cast == value) {
            return cast;
        }
        else {
            return cast - (int) (Double.doubleToRawLongBits(value) >>> 63);
        }
    }

    /**
     * Truncates a floating point number towards negative infinity. Examples:
     * <p><code>floor(4.5) == (int)4.5 == 4</code></p>
     * <p><code>floor(-4.5) == -5</code></p>
     * <p><code>(int)-4.5 == -4</code></p>
     */
    public static int fastfloor(float value) {
        int cast = (int) value;
        if ((float) cast == value) {
            return cast;
        }
        else {
            return cast - (Float.floatToRawIntBits(value) >>> 31);
        }
    }

    public static long fastceil(double num) {
        long cast = (long) num;
        if ((double) cast == num) {
            return cast;
        }
        else {
            return cast + (~Double.doubleToRawLongBits(num) >>> 63);
        }
    }

    public static int ifastceil(double num) {
        int cast = (int) num;
        if ((double) cast == num) {
            return cast;
        }
        else {
            return cast + (int) (~Double.doubleToRawLongBits(num) >>> 63);
        }
    }

    public static int fastceil(float num) {
        int cast = (int) num;
        if ((float) cast == num) {
            return cast;
        }
        else {
            return cast + (~Float.floatToRawIntBits(num) >>> 31);
        }
    }

    public static long round(double value) {
        return floor(value + 0.5d);
    }

    public static int iround(double value) {
        return ifloor(value + 0.5);
    }

    public static int round(float value) {
        return floor(value + 0.5f);
    }

    public static double pow2(double d) {
        return d * d;
    }

    public static float pow2(float d) {
        return d * d;
    }

    public static int pow2(int d) {
        return d * d;
    }

    public static double pow3(double d) {
        return d * d * d;
    }

    public static float pow3(float d) {
        return d * d * d;
    }

    public static int pow3(int d) {
        return d * d * d;
    }

    public static double toRadians(double angleDegrees) {
        return angleDegrees * DEG_TO_RAD_MULTIPLIER;
    }

    public static double toDegrees(double radians) {
        return radians * RAD_TO_DEG_MULTIPLIER;
    }

    public static double wrapRotRad(double angle) {
        angle %= Maths.PI;
        if (angle >= Maths.PI_HALF) {
            angle -= Maths.PI;
        }

        if (angle < Maths.PI_HALF_NEGATIVE) {
            angle += Maths.PI;
        }

        return angle;
    }

    public static double wrapRotDeg(double angle) {
        angle %= 360.0D;
        if (angle >= 180.0D) {
            angle -= 360.0D;
        }

        if (angle < -180.0D) {
            angle += 360.0D;
        }

        return angle;
    }

    public static boolean lineIntersects(Vector3d origin, Vector3d direction, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        // https://gamedev.stackexchange.com/a/18459/160952
        double t1 = (minX - origin.x) * (1d / direction.x);
        double t2 = (maxX - origin.x) * (1d / direction.x);
        double t3 = (minY - origin.y) * (1d / direction.y);
        double t4 = (maxY - origin.y) * (1d / direction.y);
        double t5 = (minZ - origin.z) * (1d / direction.z);
        double t6 = (maxZ - origin.z) * (1d / direction.z);
        double tMin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        double tMax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));
        if (tMax < 0d) {
            return false;
        }
        else if (tMin > tMax) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Extracts the double value's decimal part. Example:
     * <code>getDecimalPart(25.31) == 0.31</code>
     * @param value The number
     * @return The decimal part (will always be below 1.0)
     */
    public static double getDecimalPart(double value) {
        return value - (long) value;
    }

    /**
     * Extracts the float value's decimal part. Example:
     * <code>getDecimalPart(25.31f) == 0.31f</code>
     * @param value The number
     * @return The decimal part (will always be below 1.0f)
     */
    public static float getDecimalPart(float value) {
        return value - (int) value;
    }

    public static boolean isDecimal(double value) {
        return !equals(Maths.getDecimalPart(value), 0d);
        // return isDecimal(value, SMALL_ASF_D);
    }

    public static boolean isDecimal(float value) {
        return !equals(Maths.getDecimalPart(value), 0d);
        // return isDecimal(value, SMALL_ASF_F);
    }

    public static boolean isDecimal(double value, double epsilon) {
        return Math.abs(getDecimalPart(value)) > epsilon;
    }

    public static boolean isDecimal(float value, float epsilon) {
        return Math.abs(getDecimalPart(value)) > epsilon;
    }

    // counter-clockwise rotation

    /**
     * Converts a yaw rotation into it's X part. 90 would output -1.0d
     * @param yaw The yaw (in degrees)
     * @return The output, in radians
     */
    public static double yawToPointX(double yaw) {
        return Math.sin((-yaw * DEG_TO_RAD_MULTIPLIER) - Math.PI) * -1;
    }

    /**
     * Converts a yaw rotation into it's Z part. 90 would output 0d
     * @param yaw The yaw (in degrees)
     * @return The output, in radians
     */
    public static double yawToPointZ(double yaw) {
        return Math.cos((-yaw * DEG_TO_RAD_MULTIPLIER) - Math.PI) * -1;
    }

    /**
     * Divides the given number into the given number of parts and returns it as an array of
     * parts, where the summed value of all of the array's elements would equal the given value
     * <p>
     *     In cases where the value cannot be evenly divided by parts, the parts are ordered from highest to lowest in the output array
     * </p>
     * <p>
     *     Examples:
     *     <p><code>
     *         divide(25, 3) -> [9, 8, 8]
     *     </code></p>
     *     <p><code>
     *         divide(20, 5) -> [4, 4, 4, 4, 4]
     *     </code></p>
     * </p>
     * @param value The input value
     * @param parts The number of parts to split the value into (aka the size of the output array)
     * @return The value divided into parts
     */
    public static int[] divide(int value, int parts) {
        int mod = value % parts;
        int[] array = new int[parts];
        if (mod == 0) {
            Arrays.fill(array, value / parts);
        }
        else {
            Arrays.fill(array, (value - mod) / parts);
            for (int i = 0; i < mod; i++) {
                array[i] = array[i] + 1;
            }
        }

        return array;
    }

    /**
     * Maps value from range (a1 to a2) to (b1 to b2)
     * @param value The value between a1 and a2
     * @param a1 Min A
     * @param a2 Max A
     * @param b1 Min B
     * @param b2 Max B
     * @return A value between b1 and b2
     */
    public static double map(double value, double a1, double a2, double b1, double b2) {
        return b1 + (b2 - b1) / (a2 - a1) * (value - a1);
    }

    public static boolean isLessThan(int a, int b) {
        return a < b;
    }

    public static boolean isLessThan(long a, long b) {
        return a < b;
    }

    public static boolean isLessThan(double a, double b) {
        return a < b;
    }

    public static boolean isGreaterThan(int a, int b) {
        return a > b;
    }

    public static boolean isGreaterThan(long a, long b) {
        return a > b;
    }

    public static boolean isGreaterThan(double a, double b) {
        return a > b;
    }
}