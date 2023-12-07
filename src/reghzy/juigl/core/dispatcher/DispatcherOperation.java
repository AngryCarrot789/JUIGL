package reghzy.juigl.core.dispatcher;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DispatcherOperation<T> implements Future<T> {
    private final DispatchOperationHandler<T> handler;
    private final DispatchPriority priority;
    private final Dispatcher dispatcher;
    private final Object param0;
    private final Object param1;
    private T retValue;

    private volatile boolean isCompleted;
    private volatile boolean isOperationCancelled;
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

    void invoke() {
        if (this.isOperationCancelled) {
            return;
        }

        try {
            this.retValue = this.handler.invoke(this.param0, this.param1);
        }
        catch (Exception e) {
            this.exception = e;
        }
        finally {
            this.isCompleted = true;
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        this.isOperationCancelled = true;
        this.isCompleted = true;
        return false;
    }

    @Override
    public boolean isCancelled() {
        return this.isOperationCancelled;
    }

    @Override
    public boolean isDone() {
        return this.isCompleted;
    }

    private void throwIfOnDispatcherThread() throws ExecutionException {
        if (this.dispatcher.isCurrentThread()) {
            throw new ExecutionException("Bad thread", new RuntimeException("get() cannot be called on the dispatcher owner thread, otherwise, a deadlock would occur"));
        }
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        this.throwIfOnDispatcherThread();
        while (!this.isCompleted)
            Thread.sleep(1);
        return this.getValueInternal();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        this.throwIfOnDispatcherThread();
        // TODO: this is horrible but it works for now. Maybe use similar code from ForkJoinTask?
        if (timeout > 0) {
            long beginMillis = System.currentTimeMillis(), endMillis = (beginMillis + unit.toMillis(timeout));
            while (!this.isCompleted && beginMillis < endMillis) {
                Thread.sleep(1);
            }

            if (!this.isCompleted) {
                throw new TimeoutException("Timed out waiting for dispatcher operation to complete");
            }
        }
        else {
            while (!this.isCompleted) {
                Thread.sleep(1);
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
