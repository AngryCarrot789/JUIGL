package reghzy.juigl.core.dispatcher;

public class DispatcherObject {
    private Dispatcher dispatcher;

    public DispatcherObject() {
        this.dispatcher = Dispatcher.getDispatcher();
    }

    public Dispatcher getDispatcher() {
        return this.dispatcher;
    }

    public boolean isOnOwnerThread() {
        return this.dispatcher.isCurrentThread();
    }
}
