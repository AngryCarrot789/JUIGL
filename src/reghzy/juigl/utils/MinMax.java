package reghzy.juigl.utils;

import reghzy.juigl.core.ui.UIComponent;

public class MinMax {
    public double minW;
    public double maxW;
    public double minH;
    public double maxH;

    public MinMax(UIComponent c) {
        this(c.getMinWidth(), c.getMaxWidth(), c.getMinHeight(), c.getMaxHeight(), c.getWidth(), c.getHeight());
    }

    public MinMax(double minW, double maxW, double minH, double maxH, double w, double h) {
        maxW = Math.max(Math.min(Double.isNaN(w) ? Double.POSITIVE_INFINITY : w, maxW), minW);
        minW = Math.max(Math.min(maxW, Double.isNaN(w) ? 0d : w), minW);
        maxH = Math.max(Math.min(Double.isNaN(h) ? Double.POSITIVE_INFINITY : h, maxH), minH);
        minH = Math.max(Math.min(maxH, Double.isNaN(h) ? 0d : h), minH);

        this.minW = minW;
        this.maxW = maxW;
        this.minH = minH;
        this.maxH = maxH;
    }
}
