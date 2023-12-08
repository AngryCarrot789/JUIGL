package reghzy.juigl.core.dispatcher;

/**
 * An interface (usable as a functional interface) for a dispatcher operation callback/handler
 * @param <T> The return value type
 */
@FunctionalInterface
public interface DispatchOperationHandler<T> {
    /**
     * Invokes the callback on the dispatcher thread
     * @param param0 An optional first parameter, typically a functional interface object
     *               (e.g. {@link Runnable}) to delegate the call to
     * @param param1 An optional second parameter, typically null, but could also be the
     *               parameter to a {@link java.util.function.Function} when one is passed
     *               as param0, or this could be an array of parameters
     * @return An optional return value, typically null when param0 is a {@link Runnable}
     */
    T invoke(Object param0, Object param1);
}
