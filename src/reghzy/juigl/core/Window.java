package reghzy.juigl.core;

import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import reghzy.juigl.Main;
import reghzy.juigl.core.dependency.DependencyProperty;
import reghzy.juigl.core.dependency.UIPropertyMeta;
import reghzy.juigl.core.msg.MessageQueue;
import reghzy.juigl.core.ui.Panel;
import reghzy.juigl.core.ui.UIComponent;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;

public class Window extends Panel {
    public static final DependencyProperty WidthProperty = UIComponent.WidthProperty.addOwner(Window.class, new UIPropertyMeta(0d, (o, p, ov, nv) -> ((Window) o).onWidthPropertyChanged((double) ov, (double) nv)));
    public static final DependencyProperty HeightProperty = UIComponent.HeightProperty.addOwner(Window.class, new UIPropertyMeta(0d, (o, p, ov, nv) -> ((Window) o).onHeightPropertyChanged((double) ov, (double) nv)));

    private long handle;
    private String title;
    private GLCapabilities capabilities;
    private boolean isEventUpdatingSize;

    private static final int WindowClosedMessage = MessageQueue.INSTANCE.registerMessage((id, param) -> Main.onWindowsClosed());

    public Window() {
        this.title = "My window!";
    }

    public String getTitle() {
        return this.title;
    }

    private void onWidthPropertyChanged(double oldWidth, double newWidth) {
        if (this.isEventUpdatingSize || this.handle == 0)
            return;
        int w = (int) newWidth, h = (int) this.getHeight();
        GLFW.glfwSetWindowSize(this.handle, w, h);
        this.onWindowSizeChangedCore(w, h);
    }

    private void onHeightPropertyChanged(double oldHeight, double newHeight) {
        if (this.isEventUpdatingSize || this.handle == 0)
            return;
        int w = (int) this.getWidth(), h = (int) newHeight;
        GLFW.glfwSetWindowSize(this.handle, w, h);
        this.onWindowSizeChangedCore(w, h);
    }

    private void CreateHandleInternal() {
        if (this.handle != 0) {
            return;
        }

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 1);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 1);
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        this.handle = GLFW.glfwCreateWindow((int) this.getWidth(), (int) this.getHeight(), this.title, 0, 0);
        if (this.handle == 0) {
            throw new RuntimeException("An exception occurred while creating internal window");
        }

        GLFW.glfwSetWindowSizeCallback(this.handle, (hWnd, w, h) -> Window.this.onWindowSizeChanged(w, h));
        GLFW.glfwSetWindowCloseCallback(this.handle, hWnd -> Window.this.onWindowClosedInternal());
        GLFW.glfwMakeContextCurrent(this.handle);
        this.capabilities = GL.createCapabilities();
        this.setupViewport();
    }

    private void DestroyHandleInternal() {
        if (this.handle == 0) {
            return;
        }

        GLFW.glfwDestroyWindow(this.handle);
        this.handle = 0;
        MessageQueue.INSTANCE.sendMessage(WindowClosedMessage, 0);
    }

    private void onWindowClosedInternal() {
        this.DestroyHandleInternal();
    }

    private void onWindowSizeChanged(int width, int height) {
        this.isEventUpdatingSize = true;
        try {
            this.setWidth(width);
            this.setHeight(height);
            this.onWindowSizeChangedCore(width, height);

            // Main app loop is waits on glfwWaitEvents while a window is being
            // resized due to how win32 works, so this is a workaround
            MessageQueue.INSTANCE.processQueue();
        }
        finally {
            this.isEventUpdatingSize = false;
        }
    }

    private void onWindowSizeChangedCore(int width, int height) {
        this.setupViewport();
        this.measure(width, height);
        this.arrange(0, 0, width, height);
        this.invalidateVisual();
        this.updateLayoutAndRender();
    }

    private void updateLayoutAndRender() {
        LayoutManager.getLayoutManager().updateLayout();
        this.draw();
    }

    @Override
    protected Vector2d measureOverride(double availableWidth, double availableHeight) {
        Vector2d measure = super.measureOverride(availableWidth, availableHeight);
        for (int i = 0; i < this.getVisualChildrenCount(); i++) {
            UIComponent child = this.getVisualChild(i);
            child.measure(availableWidth, availableHeight);
            measure.x = Math.max(measure.x, child.getDesiredWidth());
            measure.y = Math.max(measure.y, child.getDesiredHeight());
        }

        return measure;
    }

    @Override
    protected Vector2d arrangeOverride(double availableWidth, double availableHeight) {
        Vector2d arrange = super.arrangeOverride(availableWidth, availableHeight);
        for (int i = 0; i < this.getVisualChildrenCount(); i++) {
            UIComponent child = this.getVisualChild(i);
            child.arrange(0, 0, availableWidth, availableHeight);
            arrange.x = Math.max(arrange.x, child.getDesiredWidth());
            arrange.y = Math.max(arrange.y, child.getDesiredHeight());
        }

        arrange.x = Math.max(arrange.x, this.getWidth());
        arrange.y = Math.max(arrange.y, this.getHeight());
        return arrange;
    }

    private void setupViewport() {
        GL11.glViewport(0, 0, (int) this.getWidth(), (int) this.getHeight());
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, this.getWidth(), this.getHeight(), 0, -1d, 1d);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    public void show() {
        this.CreateHandleInternal();
        GLFW.glfwShowWindow(this.handle);
    }

    public void close() {
        GLFW.glfwSetWindowShouldClose(this.handle, true);
        MessageQueue.INSTANCE.sendMessage(WindowClosedMessage, 0);
    }

    public boolean isWindowOpen() {
        return this.handle != 0;
    }

    public void draw() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glPushMatrix();
        drawRecursive(this);
        GL11.glPopMatrix();
        glfwSwapBuffers(this.handle);
    }
}
