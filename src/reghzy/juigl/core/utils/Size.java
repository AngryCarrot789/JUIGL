package reghzy.juigl.core.utils;

import org.joml.Vector2d;

public class Size {
    public final double width;
    public final double height;

    public Size(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public Size(double uniform) {
        this.width = this.height = uniform;
    }

    public Size() {
        this.width = this.height = 0;
    }

    public Size(Vector2d vec) {
        this.width = vec.x;
        this.height = vec.y;
    }

    public Vector2d toVec() {
        return new Vector2d(this.width, this.height);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Size))
            return false;
        Size s = (Size) o;
        return Double.compare(s.width, width) == 0 &&
               Double.compare(s.height, height) == 0;
    }

    @Override
    public int hashCode() {
        int hash;
        long x;
        x = Double.doubleToLongBits(width);
        hash = (int) (x ^ (x >>> 32));
        x = Double.doubleToLongBits(height);
        hash = 31 * hash + (int) (x ^ (x >>> 32));
        return hash;
    }
}
