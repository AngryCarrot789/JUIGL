package reghzy.juigl.core.dispatcher;

public enum DispatchPriority {
    Inactive,
    Idle,
    Background,
    Input,
    Render,
    Send;

    DispatchPriority() {
    }
}
