package reghzy.juigl.core.dispatcher;

public interface DispatchOperationHandler<T> {
    T invoke(Object param0, Object param1);
}
