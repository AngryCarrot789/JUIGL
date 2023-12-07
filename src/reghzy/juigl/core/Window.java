package reghzy.juigl.core;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import reghzy.juigl.Main;
import reghzy.juigl.core.msg.MessageQueue;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;

public class Window {
    private long handle;
    private String title;
    private int width;
    private int height;
    private GLCapabilities capabilities;

    private static final int WindowClosedMessage = MessageQueue.INSTANCE.registerMessage((id, param) -> {
        Main.onWindowsClosed();
    });

    public Window() {
        this.width = 1280;
        this.height = 720;
        this.title = "My window!";
    }

    public String getTitle() {
        return this.title;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    private void CreateHandleInternal() {
        if (this.handle != 0) {
            return;
        }

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 1);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 1);
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        this.handle = GLFW.glfwCreateWindow(this.width, this.height, this.title, 0, 0);
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
        MessageQueue.INSTANCE.pushMessage(WindowClosedMessage, 0);
    }

    private void onWindowClosedInternal() {
        this.DestroyHandleInternal();
    }

    private void onWindowSizeChanged(int width, int height) {
        this.width = width;
        this.height = height;
        this.setupViewport();
        Main.onWindowSizeChanged();
    }

    private void setupViewport() {
        GL11.glViewport(0, 0, this.width, this.height);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, this.width, this.height, 0, -1d, 1d);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    public void show() {
        this.CreateHandleInternal();
        GLFW.glfwShowWindow(this.handle);
    }

    public void close() {
        GLFW.glfwSetWindowShouldClose(this.handle, true);
        MessageQueue.INSTANCE.pushMessage(WindowClosedMessage, 0);
    }

    public void onShowInternal() {

    }

    public void swapBuffers() {
        glfwSwapBuffers(this.handle);
    }

    public boolean isWindowOpen() {
        return this.handle != 0;
    }

    public void draw() {
        Main.onReDrawApplicationWindow();
    }
}
