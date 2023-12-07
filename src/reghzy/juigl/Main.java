package reghzy.juigl;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import reghzy.juigl.core.LayoutManager;
import reghzy.juigl.core.Window;
import reghzy.juigl.core.dispatcher.DispatchPriority;
import reghzy.juigl.core.dispatcher.Dispatcher;
import reghzy.juigl.core.msg.MessageQueue;
import reghzy.juigl.core.render.RenderData;
import reghzy.juigl.core.ui.Panel;
import reghzy.juigl.core.ui.UIComponent;
import reghzy.juigl.core.utils.HAlign;
import reghzy.juigl.core.utils.Thickness;
import reghzy.juigl.core.utils.VAlign;

import java.awt.*;

public class Main {
    public static RenderData renderData;
    public static Window mainWindow;
    private static PointerBuffer pBuffer;
    private static volatile boolean isAppRunning;

    private static Panel mainContent;

    public static void printLastError() {
        if (GLFW.glfwGetError(pBuffer) != GL11.GL_NO_ERROR) {
            System.out.println(pBuffer.getStringUTF8());
            pBuffer.clear();
        }
    }

    public static void main(String[] args) {
        pBuffer = PointerBuffer.allocateDirect(8192);
        if (!GLFW.glfwInit()) {
            System.out.println("Failed to init glfw");
            return;
        }

        mainWindow = new Window();
        mainWindow.show();

        printLastError();
        // glEnable(GL13.GL_MULTISAMPLE);

        float x = 20, y = 20, width = 300, height = 100F;
        renderData = new RenderData(RenderData.CapacityForQuad(1), GL11.GL_QUADS);
        renderData.setColour(255, 40, 90, 255);
        renderData.addVertex(x, y + height, 0.5F);
        renderData.addVertex(x + width, y + height, 0.5F);
        renderData.addVertex(x + width, y, 0.5F);
        renderData.addVertex(x, y, 0.5F);

        printLastError();

        GL11.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        // GL11.glEnable(GL11.GL_DEPTH_TEST);
        // GL11.glDepthFunc(GL11.GL_LESS);
        // GL11.glDepthMask(true);
        printLastError();

        Dispatcher.getDispatcher().invokeLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("lel!");
            }
        }, DispatchPriority.Render);

        // define UI

        mainContent = new Panel();
        mainContent.setBackgroundColour(Color.orange);

        UIComponent myCmp = new UIComponent();
        myCmp.setHorizontalAlignment(HAlign.right);
        myCmp.setVerticalAlignment(VAlign.bottom);
        myCmp.setWidth(250);
        myCmp.setHeight(100);
        myCmp.setMargin(new Thickness(10));
        myCmp.setBackgroundColour(Color.red);
        mainContent.addChild(myCmp);

        // test to make sure dependency property system works
        UIComponent cmp = new UIComponent();
        System.out.println(cmp.getMargin());
        System.out.println(cmp.getWidth());
        System.out.println(cmp.getHeight());
        System.out.println(cmp.getHorizontalAlignment());
        System.out.println(cmp.getVerticalAlignment());

        mainContent.invalidateVisual();
        mainContent.measure(mainWindow.getWidth(), mainWindow.getHeight());
        mainContent.arrange(0, 0, mainWindow.getWidth(), mainWindow.getHeight());
        LayoutManager.getLayoutManager().updateLayout();
        onReDrawApplicationWindow();

        isAppRunning = true;
        do {
            GLFW.glfwWaitEvents();
            MessageQueue.INSTANCE.processQueue();
        } while (isAppRunning);

        GLFW.glfwTerminate();
    }

    public static void onWindowSizeChanged() {
        mainContent.invalidateMeasure();
        mainContent.invalidateVisual();
        mainContent.arrange(0, 0, mainWindow.getWidth(), mainWindow.getHeight());
        LayoutManager.getLayoutManager().updateLayout();
        Main.onReDrawApplicationWindow();

        // Dispatcher.getDispatcher().invokeLater(Main::onReDrawApplicationWindow, DispatchPriority.Render);
    }

    public static void onReDrawApplicationWindow() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glPushMatrix();
        // renderData.draw(RenderData.LargeDirectBuffer, null);
        UIComponent.drawRecursive(mainContent);
        GL11.glPopMatrix();

        mainWindow.swapBuffers();
    }

    public static void onWindowsClosed() {
        if (!mainWindow.isWindowOpen()) {
            isAppRunning = false;
        }
    }
}
