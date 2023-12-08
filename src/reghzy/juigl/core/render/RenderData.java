package reghzy.juigl.core.render;

import oracle.jrockit.jfr.events.Bits;
import org.lwjgl.opengl.GL11;
import reghzy.juigl.core.utils.BitUtils;
import reghzy.juigl.utils.Maths;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.text.Normalizer;
import java.util.Arrays;

// THIS WILL BE REMOVED AT SOME POINT AND REPLACED WITH MORE OPTIMISED RENDER INSTRUCTIONS THAT USE PROPER VBO/VAO/SHADERS ETC.
// This is just temporary, and it's very simple to work with which is why i'm using it at the
// moment. It's based on an ancient version of minecraft's tessellator

/**
 * Stores data about a render pass. A single vertex takes up TOTAL_VERTEX_BYTES bytes
 */
public class RenderData {
    public static final ByteBuffer LargeDirectBuffer = ByteBuffer.allocateDirect(4096).order(ByteOrder.nativeOrder());
    public static final boolean CONVERT_QUADS_TO_TRIS = true;
    private static final int FLAG_HAS_COLOUR = 1;
    private static final int FLAG_HAS_NORMAL = 2;
    private static final int FLAG_HAS_TEXTURE = 4;

    private final boolean canAutoResize;
    public final int drawMode; // The OpenGL drawing mode (typically GL_TRIANGLES/GL_QUAD)
    public int[] buffer;       // Stores the vertex, colour, texture and normal information. lazily allocated when required
    public int bufferCapacity; // The total numbers of elements in our buffer
    public int bufferIndex;    // The index into the raw buffer to be used for the next data
    public int vertCount;      // The number of vertices to be drawn in the next draw call
    public int addedVertCount; // Used to track calls to addVertex for quad-triangle conversion
    public int theColour;      // The RGBA colour to be used for the draw call (big-endian)
    public int theNormal;      // The normal applied to the face being drawn
    public float theTexU;      // The 1st texture coord
    public float theTexV;      // The 2st texture coord
    public int packedFlags;

    public RenderData(int initialBufferCapacity, int glMode) {
        this(initialBufferCapacity, glMode, false);
    }

    public RenderData(int initialBufferCapacity, int glMode, boolean canAutoResize) {
        if (initialBufferCapacity < 1)
            throw new IllegalArgumentException("initial capacity must be greater than 0");
        this.bufferCapacity = initialBufferCapacity;
        this.drawMode = glMode;
        this.canAutoResize = canAutoResize;
    }

    public static int CapacityForQuad(int quadCount) {
        return CONVERT_QUADS_TO_TRIS ? (quadCount * 6 * TOTAL_VERTEX_BYTES) : (quadCount * 4 * TOTAL_VERTEX_BYTES);
    }

    public void setTexture(float u, float v) {
        this.packedFlags |= FLAG_HAS_TEXTURE;
        this.theTexU = u;
        this.theTexV = v;
    }

    public void setColour(int colour) {
        this.packedFlags |= FLAG_HAS_COLOUR;
        this.theColour = colour;
    }

    private void setColourInternal(int r, int g, int b, int a) {
        this.setColour((Maths.clamp(a, 0, 255) << 24) | (Maths.clamp(b, 0, 255) << 16) | (Maths.clamp(g, 0, 255) << 8) | Maths.clamp(r, 0, 255));
    }

    public void setColour(int r, int g, int b, int a) {
        this.setColourInternal(Maths.clamp(r, 0, 255), Maths.clamp(g, 0, 255), Maths.clamp(b, 0, 255), Maths.clamp(a, 0, 255));
    }

    public void setColour(int r, int g, int b) {
        this.setColour(r, g, b, 255);
    }

    public void setColour(float r, float g, float b, float a) {
        this.setColourInternal((int) (Maths.clamp(r, 0F, 1F) * 255F), (int) (Maths.clamp(g, 0F, 1F) * 255F), (int) (Maths.clamp(b, 0F, 1F) * 255F), (int) (Maths.clamp(a, 0F, 1F) * 255F));
    }

    public void setColour(float r, float g, float b) {
        this.setColour(r, g, b, 1F);
    }

    public void setNormal(float u, float v, float w) {
        int fU = (int) (u * 127.0F);
        int fV = (int) (v * 127.0F);
        int fW = (int) (w * 127.0F);
        this.packedFlags |= FLAG_HAS_NORMAL;
        this.theNormal = (fU & 255) | (fV & 255) << 8 | (fW & 255) << 16;
    }

    public static final int TOTAL_VERTEX_INTS = 7;
    public static final int TOTAL_VERTEX_BYTES = TOTAL_VERTEX_INTS * 4;

    public void addVertex(float x, float y, float z) {
        int[] buf = this.buffer;
        if (buf == null) {
            this.buffer = buf = new int[this.bufferCapacity];
        }
        else if (this.bufferIndex >= (this.bufferCapacity + TOTAL_VERTEX_INTS)) {
            if (this.canAutoResize) {
                this.bufferCapacity *= 2;
                this.buffer = buf = Arrays.copyOf(this.buffer, this.bufferCapacity);
            }
            else {
                throw new RuntimeException("Buffer is not big enough. Cannot add new vertices");
            }
        }

        this.addedVertCount++;

        if (this.drawMode == GL11.GL_QUADS && CONVERT_QUADS_TO_TRIS && this.addedVertCount % 4 == 0) {
            for (int i = 0; i < 2; i++) {
                int j = TOTAL_VERTEX_INTS * (3 - i);
                buf[this.bufferIndex]     = buf[this.bufferIndex - j];
                buf[this.bufferIndex + 1] = buf[this.bufferIndex - j + 1];
                buf[this.bufferIndex + 2] = buf[this.bufferIndex - j + 2];
                if (BitUtils.getFlag(this.packedFlags, FLAG_HAS_TEXTURE)) {
                    buf[this.bufferIndex + 3] = buf[this.bufferIndex - j + 3];
                    buf[this.bufferIndex + 4] = buf[this.bufferIndex - j + 4];
                }
                if (BitUtils.getFlag(this.packedFlags, FLAG_HAS_COLOUR))
                    buf[this.bufferIndex + 5] = buf[this.bufferIndex - j + 5];
                this.vertCount++;
                this.bufferIndex += TOTAL_VERTEX_INTS;
            }
        }

        buf[this.bufferIndex] = Float.floatToRawIntBits(x);
        buf[this.bufferIndex + 1] = Float.floatToRawIntBits(y);
        buf[this.bufferIndex + 2] = Float.floatToRawIntBits(z);
        if (BitUtils.getFlag(this.packedFlags, FLAG_HAS_TEXTURE)) {
            buf[this.bufferIndex + 3] = Float.floatToRawIntBits(this.theTexU);
            buf[this.bufferIndex + 4] = Float.floatToRawIntBits(this.theTexV);
        }

        if (BitUtils.getFlag(this.packedFlags, FLAG_HAS_COLOUR))
            buf[this.bufferIndex + 5] = this.theColour;
        if (BitUtils.getFlag(this.packedFlags, FLAG_HAS_NORMAL))
            buf[this.bufferIndex + 6] = this.theNormal;

        this.bufferIndex += TOTAL_VERTEX_INTS;
        this.vertCount++;
    }

    /**
     * Draws this render data to the active OpenGL context
     * @param byteBuffer a buffer that is used for drawing to OpenGL. Must be a direct byte buffer
     * @param intBuffer An int buffer tied directly to byteBuffer. May be null, causing an int buffer to be autocreated
     */
    public void draw(ByteBuffer byteBuffer, IntBuffer intBuffer) {
        if (this.vertCount < 1) {
            return;
        }

        byteBuffer.position(0);
        if (intBuffer == null)
            intBuffer = byteBuffer.asIntBuffer();
        intBuffer.position(0);
        intBuffer.put(this.buffer, 0, this.bufferIndex);
        if (BitUtils.getFlag(this.packedFlags, FLAG_HAS_TEXTURE)) {
            byteBuffer.position(12);
            GL11.glTexCoordPointer(2, GL11.GL_FLOAT, TOTAL_VERTEX_BYTES, byteBuffer);
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        }

        if (BitUtils.getFlag(this.packedFlags, FLAG_HAS_COLOUR)) {
            byteBuffer.position(20);
            GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, TOTAL_VERTEX_BYTES, byteBuffer);
            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        }

        if (BitUtils.getFlag(this.packedFlags, FLAG_HAS_NORMAL)) {
            byteBuffer.position(24);
            GL11.glNormalPointer(GL11.GL_FLOAT, TOTAL_VERTEX_BYTES, byteBuffer);
            GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
        }

        byteBuffer.position(0);
        GL11.glVertexPointer(3, GL11.GL_FLOAT, TOTAL_VERTEX_BYTES, byteBuffer);
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDrawArrays(this.drawMode == GL11.GL_QUADS && CONVERT_QUADS_TO_TRIS ? 4 : this.drawMode, 0, this.vertCount);
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

        if (BitUtils.getFlag(this.packedFlags, FLAG_HAS_TEXTURE)) {
            GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        }

        if (BitUtils.getFlag(this.packedFlags, FLAG_HAS_COLOUR)) {
            GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        }

        if (BitUtils.getFlag(this.packedFlags, FLAG_HAS_NORMAL)) {
            GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        }
    }

    // public void close() {
    //     ByteBuffer buffer = this.buffer;
    //     if (buffer instanceof DirectBuffer)
    //         ((DirectBuffer) buffer).cleaner().clean();
    // }
}
