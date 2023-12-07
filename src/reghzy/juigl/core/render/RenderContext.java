package reghzy.juigl.core.render;

import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class RenderContext {
    private final ArrayList<RenderData> renderData;
    private final float zDepth;

    public RenderContext(float zDepth) {
        this.renderData = new ArrayList<>();
        this.zDepth = zDepth;
    }

    public void drawRect(double x, double y, double width, double height, float r, float g, float b, float a) {
        RenderData data = new RenderData(RenderData.CapacityForQuad(1), GL11.GL_QUADS);
        data.setColour(r, g, b, a);
        data.addVertex((float) x, (float) (y + height), this.zDepth);
        data.addVertex((float) (x + width), (float) (y + height), this.zDepth);
        data.addVertex((float) (x + width), (float) y, this.zDepth);
        data.addVertex((float) x, (float) y, this.zDepth);
        this.renderData.add(data);
    }

    public ArrayList<RenderData> getRenderData() {
        return this.renderData;
    }
}
