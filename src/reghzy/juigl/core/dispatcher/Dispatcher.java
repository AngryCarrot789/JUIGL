package reghzy.juigl.core.dispatcher;

import reghzy.juigl.core.msg.MessageHandler;
import reghzy.juigl.core.msg.MessageQueue;

import javax.swing.text.StyledEditorKit;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public final class Dispatcher {
    private static final int PRIORITY_BACKGROUND = 2;//DispatchPriority.Background.ordinal();
    private static final int PRIORITY_HIGHEST = 5;// DispatchPriority.Send.ordinal();
    private static final int PRIORITY_LOWEST = 0;// DispatchPriority.Inactive.ordinal();

    private final Thread thread;
    private final ConcurrentLinkedQueue<DispatcherOperation<?>>[] queues;
    private volatile boolean isProcessingQueue;
    private final AtomicBoolean hasEnqueued;

    private static final int MessageKey;
    private static final ThreadLocal<Dispatcher> dispatchers;

    static {
        dispatchers = ThreadLocal.withInitial(Dispatcher::new);
        MessageKey = MessageQueue.INSTANCE.registerMessage(Dispatcher::onRequestProcessingMessage);
    }

    private static void onRequestProcessingMessage(int msg, int param) {
        getDispatcher().processQueue();
    }

    public Object reserved0;

    public Dispatcher() {
        this.thread = Thread.currentThread();
        this.hasEnqueued = new AtomicBoolean(false);
        this.queues = new ConcurrentLinkedQueue[PRIORITY_HIGHEST];
        for (int i = 1; i < PRIORITY_HIGHEST; i++) {
            this.queues[i] = new ConcurrentLinkedQueue<>();
        }
    }

    public static Dispatcher getDispatcher() {
        return dispatchers.get();
    }

    private ArrayList<DispatcherOperation<?>> getOrderedOperations() {
        ArrayList<DispatcherOperation<?>> list = new ArrayList<>(4);
        for (int i = 1; i < PRIORITY_HIGHEST; i++) {
            ConcurrentLinkedQueue<DispatcherOperation<?>> queue = this.queues[i];
            DispatcherOperation<?> operation;
            while ((operation = queue.poll()) != null && !operation.isCancelled())
                list.add(operation);
        }

        return list;
    }

    public boolean isCurrentThread() {
        return Thread.currentThread() == this.thread;
    }

    public void processQueue() {
        this.hasEnqueued.set(false);
        if (this.isProcessingQueue)
            throw new IllegalStateException("Already processing queue");
        this.isProcessingQueue = true;
        try {
            ArrayList<DispatcherOperation<?>> operations = this.getOrderedOperations();
            for (int i = 0, count = operations.size(); i < count; i++) {
                operations.get(i).invoke();
            }
        }
        finally {
            this.isProcessingQueue = false;
            this.hasEnqueued.set(false);
        }
    }

    public Future<Void> invokeLater(Runnable runnable) {
        return this.invokeLater(runnable, DispatchPriority.Send);
    }

    public Future<Void> invokeLater(Runnable runnable, DispatchPriority priority) {
        if (priority == DispatchPriority.Send && this.isCurrentThread()) {
            runnable.run();
            return CompletableFuture.completedFuture(null);
        }
        else {
            return this.invokeInternal(StandardRunnableHandler.getInstance(), runnable, null, priority);
        }
    }

    public <R, T> Future<T> invokeLater(Function<R, T> function, R parameter) {
        return this.invokeLater(function, parameter, DispatchPriority.Send);
    }

    public <R, T> Future<T> invokeLater(Function<R, T> function, R parameter, DispatchPriority priority) {
        if (priority == DispatchPriority.Send && this.isCurrentThread()) {
            return CompletableFuture.completedFuture(function.apply(parameter));
        }
        else {
            return this.invokeInternal(StandardFunctionHandler.getInstance(), function, parameter, priority);
        }
    }

    public <T> DispatcherOperation<T> invokeInternal(DispatchOperationHandler<T> handler, Object param0, Object param1, DispatchPriority priority) {
        DispatcherOperation<T> operation = new DispatcherOperation<T>(this, priority, handler, param0, param1);
        this.enqueue(operation);
        return operation;
    }

    private void enqueue(DispatcherOperation<?> operation) {
        int priority = operation.getPriority().ordinal();
        if (priority != 0) {
            this.queues[priority].offer(operation);
            if (this.hasEnqueued.compareAndSet(false, true)) {
                this.requestProcessing();
            }
        }
    }

    private void requestProcessing() {
        MessageQueue.INSTANCE.pushMessage(MessageKey);
    }

    private static final class StandardRunnableHandler implements DispatchOperationHandler {
        private static final StandardRunnableHandler INSTANCE = new StandardRunnableHandler();

        public static <T> DispatchOperationHandler<T> getInstance() {
            return INSTANCE;
        }

        @Override
        public Object invoke(Object param0, Object param1) {
            ((Runnable) param0).run();
            return null;
        }
    }

    private static final class StandardFunctionHandler implements DispatchOperationHandler {
        private static final StandardFunctionHandler INSTANCE = new StandardFunctionHandler();

        public static <T> DispatchOperationHandler<T> getInstance() {
            return INSTANCE;
        }

        @Override
        public Object invoke(Object param0, Object param1) {
            return ((Function) param0).apply(param1);
        }
    }
}
