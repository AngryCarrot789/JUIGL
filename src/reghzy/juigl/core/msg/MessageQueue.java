package reghzy.juigl.core.msg;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageQueue {
    /**
     * A message that can be enqueued in order to signal to the message queue to
     * stop processing messages, and transfer control back to the operating system
     */
    public static final int BREAK_MESSAGE_LOOP_MSG = 0;
    public static final MessageQueue INSTANCE = new MessageQueue();
    private static final Long[] EMPTY_LONG_ARRAY = new Long[0];
    private final ConcurrentLinkedQueue<Long> messageQueue;

    private final AtomicBoolean isMessageQueued;
    private final AtomicBoolean isProcessing;
    private final HashMap<Integer, MessageHandler> handlerMap;
    private long hiddenWindowHandle;
    private int nextId = 1;

    public boolean IsProcessingEvents;

    public MessageQueue() {
        this.isMessageQueued = new AtomicBoolean(false);
        this.isProcessing = new AtomicBoolean(false);
        this.handlerMap = new HashMap<>();
        this.messageQueue = new ConcurrentLinkedQueue<>();
    }

    private long getHiddenWindowHandle() {
        if (this.hiddenWindowHandle == 0) {
            GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_MOUSE_PASSTHROUGH, GLFW.GLFW_TRUE);
            this.hiddenWindowHandle = GLFW.glfwCreateWindow(1, 1, "", 0, 0);
            GLFW.glfwDefaultWindowHints();
            if (this.hiddenWindowHandle == 0) {
                throw new RuntimeException("Failed to create message queue hidden window");
            }
        }

        return this.hiddenWindowHandle;
    }

    public int registerMessage(MessageHandler handler) {
        int id = this.nextId++;
        if (id < 0) {
            throw new Error("Too many messages registered");
        }

        this.handlerMap.put(id, handler);
        return id;
    }

    /**
     * Processes all messages in the queue, until the queue is completely empty. Must be called on the
     * owner thread, and only during a thread's main tick
     */
    public void processQueue() {
        if (!this.isProcessing.compareAndSet(false, true)) {
            throw new IllegalStateException("Already processing queue");
        }

        try {
            Long msg;
            while ((msg = this.messageQueue.poll()) != null) {
                int id = (int) (msg & Integer.MAX_VALUE);
                if (id == BREAK_MESSAGE_LOOP_MSG) {
                    break;
                }

                this.processMessage(id, (int) (msg >> 32));
            }
        }
        finally {
            this.isProcessing.set(false);
        }
    }

    /**
     * Processes the given message as if it had just arrived in the message queue
     * @param msg the message to process
     */
    public void unsafeProcessLiveMessage(int msg, int param) {
        this.processMessage(msg, param);
    }

    private void processMessage(int msg, int param) {
        MessageHandler handler = this.handlerMap.get(msg);
        if (handler != null) {
            handler.onMessage(msg, param);
        }
    }

    public void sendMessage(int msg, int param) {
        this.pushMessageInternal((long) msg | ((long) param << 32));
    }

    public void sendMessage(int msg) {
        this.pushMessageInternal(msg);
    }

    public void pushMessageInternal(long msg) {
        this.messageQueue.offer(msg);
        this.postWin32EmptyMessage();
    }

    private void postWin32EmptyMessage() {
        if (this.isMessageQueued.compareAndSet(false, true)) {
            GLFW.glfwPostEmptyEvent();
        }
    }
}
