package reghzy.juigl.core.render;

import java.nio.IntBuffer;
import java.util.ArrayList;

public class ComponentRenderData {
    private final ArrayList<RenderData> data;

    public ComponentRenderData(ArrayList<RenderData> data) {
        this.data = data;
    }

    public void drawAll() {
        IntBuffer buffer = RenderData.LargeDirectBuffer.asIntBuffer();
        for (RenderData data : this.data) {
            data.draw(RenderData.LargeDirectBuffer, buffer);
        }
    }
}
