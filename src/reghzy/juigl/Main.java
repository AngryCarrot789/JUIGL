package reghzy.juigl;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import reghzy.juigl.core.LayoutManager;
import reghzy.juigl.core.Window;
import reghzy.juigl.core.msg.MessageQueue;
import reghzy.juigl.core.ui.UIComponent;
import reghzy.juigl.core.utils.HAlign;
import reghzy.juigl.core.utils.Thickness;
import reghzy.juigl.core.utils.VAlign;

import java.awt.*;

public class Main {
    public static Window mainWindow;
    private static PointerBuffer pBuffer;
    private static volatile boolean isAppRunning;

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
        mainWindow.setWidth(1280);
        mainWindow.setHeight(720);
        mainWindow.show();

        printLastError();
        // glEnable(GL13.GL_MULTISAMPLE);

        printLastError();

        GL11.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        // GL11.glEnable(GL11.GL_DEPTH_TEST);
        // GL11.glDepthFunc(GL11.GL_LESS);
        // GL11.glDepthMask(true);
        printLastError();

        // define UI

        mainWindow.setBackgroundColour(new Color(25, 25, 25));

        UIComponent myCmp = new UIComponent();
        myCmp.setHorizontalAlignment(HAlign.right);
        myCmp.setVerticalAlignment(VAlign.bottom);
        myCmp.setWidth(250);
        myCmp.setHeight(100);
        myCmp.setMargin(new Thickness(10));
        myCmp.setBackgroundColour(new Color(75, 75, 75));
        mainWindow.addChild(myCmp);

        UIComponent stretchComp = new UIComponent();
        stretchComp.setHorizontalAlignment(HAlign.left);
        stretchComp.setVerticalAlignment(VAlign.stretch);
        stretchComp.setWidth(250);
        stretchComp.setMargin(new Thickness(10));
        stretchComp.setBackgroundColour(new Color(45, 45, 45));
        mainWindow.addChild(stretchComp);

        // test to make sure dependency property system works
        UIComponent cmp = new UIComponent();
        System.out.println(cmp.getMargin());
        System.out.println(cmp.getWidth());
        System.out.println(cmp.getHeight());
        System.out.println(cmp.getHorizontalAlignment());
        System.out.println(cmp.getVerticalAlignment());

        // mainWindow.invalidateMeasure();
        // mainWindow.invalidateArrange();
        mainWindow.invalidateVisual();
        mainWindow.arrange(0, 0, mainWindow.getWidth(), mainWindow.getHeight());
        LayoutManager.getLayoutManager().updateLayout();
        mainWindow.draw();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            mainWindow.getDispatcher().invokeLater(() -> mainWindow.setWidth(1000));
        }).start();

        isAppRunning = true;
        do {
            GLFW.glfwWaitEvents();
            MessageQueue.INSTANCE.processQueue();
        } while (isAppRunning);

        GLFW.glfwTerminate();
    }

    public static void onWindowsClosed() {
        if (!mainWindow.isWindowOpen()) {
            isAppRunning = false;
        }
    }
}
