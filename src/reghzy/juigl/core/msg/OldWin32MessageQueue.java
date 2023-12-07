package reghzy.juigl.core.msg;

import com.sun.corba.se.spi.ior.iiop.IIOPFactories;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class OldWin32MessageQueue {
    private volatile long[] queue; // auto resized on demand, and shrunken every so often
    private volatile int msgCount;
    private final Object lock;
    private final AtomicBoolean isMessageQueued;
    private final AtomicBoolean isProcessing;

    public OldWin32MessageQueue() {
        this.queue = new long[32];
        this.lock = new Object();
        this.isMessageQueued = new AtomicBoolean(false);
        this.isProcessing = new AtomicBoolean(false);
    }

    public void processQueue() {
        if (!this.isProcessing.compareAndSet(false, true)) {
            throw new IllegalStateException("Already processing queue");
        }

        try {
            int count;
            long[] queue;
            synchronized (this.lock) {
                this.isMessageQueued.set(false);
                if ((count = this.msgCount) > 0) {
                    queue = this.queue;
                    this.msgCount = 0;
                }
                else {
                    return;
                }
            }

            for (int i = 0; i < count; i++) {
                this.processNext(queue[i]);
            }

            synchronized (this.lock) {
                if (this.msgCount > 0) {
                    if (queue == this.queue) {
                        int newCount = this.msgCount;
                        for (int i = count, j = 0; i < newCount; i++) {
                            queue[j++] = queue[i];
                        }

                        System.arraycopy(queue, count, queue, 0, count);
                    }
                    else { // resized while processing, possibly on another thread

                    }
                }
            }
        }
        finally {
            this.isProcessing.set(false);
        }
    }

    private void processNext(long msg) {
        // not implmeneted yet
    }

    public void pushMessage(long msg) {
        synchronized (this.lock) {
            long[] queue = this.queue;
            if (this.msgCount == queue.length) {
                this.queue = queue = Arrays.copyOf(this.queue, this.queue.length * 2);
            }

            queue[this.msgCount++] = msg;
        }

        if (this.isMessageQueued.compareAndSet(false, true)) {
            GLFW.glfwPostEmptyEvent();
        }
    }
}
