package kaktusz.kaktuszlogistics.util;

import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;

@SuppressWarnings("unused")
public class MathsUtils {

    @SuppressWarnings("ManualMinMaxCalculation") //readability nicer, less function calls
    public static float clamp(float t, float min, float max) {
        if(t < min)
            return min;
        if(t > max)
            return max;

        return t;
    }

    public static float lerp(float a, float b, float t) {
        return (1.0F - t)*a + t*b;
    }

    /**
     * Clamped lerp
     */
    public static float lerpc(float a, float b, float t) {
        return lerp(a, b, clamp(t, 0.0F, 1.0F));
    }

    public static float lerpInverse(float a, float b, float t) {
        return (t-a) / (b-a);
    }

    /**
     * Clamped lerpInverse
     */
    public static float lerpInverseC(float a, float b, float t) {
        return clamp(lerpInverse(a, b, t), 0.0F, 1.0F);
    }

    public static float randomRange(float min, float max) {
        return min + RandomUtils.nextFloat()*(max-min);
    }

    private static final DecimalFormat df = new DecimalFormat("##.##");
    public static String vectorReadableString(Vector v) {
        return "(" + df.format(v.getX()) + ", " + df.format(v.getY()) + ", " + df.format(v.getZ()) + ")";
    }
}
