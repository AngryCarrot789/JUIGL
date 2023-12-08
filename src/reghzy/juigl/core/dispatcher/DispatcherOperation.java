package reghzy.juigl.core.dispatcher;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DispatcherOperation<T> implements Future<T> {
    private final DispatchOperationHandler<T> handler;
    private final DispatchPriority priority;
    private final Dispatcher dispatcher;
    private final Object param0, param1;
    private T retValue;

    private volatile int flags; // 0: inactive, 1: success, 2: cancelled. flags != 0: completed
    private Throwable exception;

    public DispatcherOperation(Dispatcher dispatcher, DispatchPriority priority, DispatchOperationHandler<T> handler) {
        this(dispatcher, priority, handler, null, null);
    }

    public DispatcherOperation(Dispatcher dispatcher, DispatchPriority priority, DispatchOperationHandler<T> handler, Object param0, Object param1) {
        if (dispatcher == null)
            throw new IllegalArgumentException("Dispatcher cannot be null");
        if (priority == null)
            throw new IllegalArgumentException("Priority cannot be null");
        if (handler == null)
            throw new IllegalArgumentException("Operation handler cannot be null");

        this.dispatcher = dispatcher;
        this.priority = priority;
        this.handler = handler;
        this.param0 = param0;
        this.param1 = param1;
    }

    public DispatchPriority getPriority() {
        return this.priority;
    }

    public Dispatcher getDispatcher() {
        return this.dispatcher;
    }

    public Throwable getException() {
        return this.exception;
    }

    public T getReturnValue() {
        return this.retValue;
    }

    void invoke() {
        if (this.flags == 0) {
            try {
                this.retValue = this.handler.invoke(this.param0, this.param1);
            }
            catch (Exception e) {
                this.exception = e;
            }
            finally {
                this.flags = 1;
            }
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        this.flags = 2;
        return false;
    }

    @Override
    public boolean isCancelled() {
        return this.flags == 2;
    }

    @Override
    public boolean isDone() {
        return this.flags != 0;
    }

    private void throwIfOnDispatcherThread() throws ExecutionException {
        if (this.dispatcher.isCurrentThread()) {
            throw new ExecutionException("Bad thread", new RuntimeException("get() cannot be called on the dispatcher owner thread, otherwise, a deadlock would occur"));
        }
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        this.throwIfOnDispatcherThread();
        while (this.flags == 0)
            Thread.sleep(1);
        return this.getValueInternal();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        // TODO: maybe implement dispatcher frames, push new frame here?
        this.throwIfOnDispatcherThread();
        if (this.flags == 0) {
            if (timeout > 0) {
                // TODO: this is horrible but it works for now. Maybe use similar code from ForkJoinTask?
                long endMillis = System.currentTimeMillis() + unit.toMillis(timeout);
                while (this.flags == 0 && System.currentTimeMillis() < endMillis) {
                    Thread.sleep(1);
                }

                if (this.flags == 0) {
                    throw new TimeoutException("Timed out waiting for dispatcher operation to complete");
                }
            }
            else {
                while (this.flags == 0) {
                    Thread.sleep(1);
                }
            }
        }

        return this.getValueInternal();
    }

    private T getValueInternal() throws ExecutionException {
        if (this.exception != null)
            throw new ExecutionException("An exception occurred while executing the operation", this.exception);
        // can cancellation be handled here? maybe by throwing?
        return this.retValue;
    }
}
