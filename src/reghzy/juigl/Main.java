package reghzy.juigl;

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
    private static volatile boolean isAppRunning;

    public static void main(String[] args) {
        System.out.println("GLFW init");
        if (!GLFW.glfwInit()) {
            System.out.println("Failed to init glfw");
            return;
        }

        System.out.println("Creating window...");
        mainWindow = new Window();
        mainWindow.setWidth(1280);
        mainWindow.setHeight(720);
        mainWindow.show();

        // GL11.glEnable(GL13.GL_MULTISAMPLE);

        GL11.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        // GL11.glEnable(GL11.GL_DEPTH_TEST);
        // GL11.glDepthFunc(GL11.GL_LESS);
        // GL11.glDepthMask(true);

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

        mainWindow.invalidateVisual();
        mainWindow.arrange(0, 0, mainWindow.getWidth(), mainWindow.getHeight());
        LayoutManager.getLayoutManager().updateLayout();
        mainWindow.draw();

        new Thread(() -> {
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            // if you resize a window a bit but then don't move your mouse all within 2s of
            // running the app, the dispatcher won't run until you release the LMB or you
            // move your mouse again. This is due to how Win32 works... and i'm not sure how too get around it
            mainWindow.getDispatcher().invokeAsync(() -> mainWindow.setWidth(1000));
        }).start();

        System.out.println("App main");
        isAppRunning = true;
        do {
            MessageQueue.INSTANCE.IsProcessingEvents = true;
            GLFW.glfwWaitEvents();
            MessageQueue.INSTANCE.IsProcessingEvents = false;
            MessageQueue.INSTANCE.processQueue();
            // System.out.println(System.currentTimeMillis());
        } while (isAppRunning);

        GLFW.glfwTerminate();
    }

    public static void onWindowsClosed() {
        if (!mainWindow.isWindowOpen()) {
            isAppRunning = false;
        }
    }
}
