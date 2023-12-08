package reghzy.juigl.core.utils;

import java.text.MessageFormat;

public final class Thickness {
    public static final Thickness ZERO = new Thickness();
    public static final Thickness ONE = new Thickness(1);

    public final double left;
    public final double top;
    public final double right;
    public final double bottom;

    public Thickness(double left, double top, double right, double bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public Thickness(double horizontal, double vertical) {
        this(horizontal, vertical, horizontal, vertical);
    }

    public Thickness(double uniform) {
        this(uniform, uniform, uniform, uniform);
    }

    public Thickness() {
        this(0);
    }

    public double getHorizontal() {
        return this.left + this.right;
    }

    public double getVertical() {
        return this.top + this.bottom;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Thickness))
            return false;
        Thickness t = (Thickness) o;
        return Double.compare(t.left, left) == 0 &&
               Double.compare(t.top, top) == 0 &&
               Double.compare(t.right, right) == 0 &&
               Double.compare(t.bottom, bottom) == 0;
    }

    @Override
    public int hashCode() {
        int hash;
        long x;
        x = Double.doubleToLongBits(left);
        hash = (int) (x ^ (x >>> 32));
        x = Double.doubleToLongBits(top);
        hash = 31 * hash + (int) (x ^ (x >>> 32));
        x = Double.doubleToLongBits(right);
        hash = 31 * hash + (int) (x ^ (x >>> 32));
        x = Double.doubleToLongBits(bottom);
        hash = 31 * hash + (int) (x ^ (x >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0},{1},{2},{3}", this.left, this.top, this.right, this.bottom);
    }
}
