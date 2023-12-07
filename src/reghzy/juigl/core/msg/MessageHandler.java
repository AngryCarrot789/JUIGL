package reghzy.juigl.core.msg;

public interface MessageHandler {
    /**
     * Called when a message queue processes a message with the ID associated with this handler
     * @param id The message id
     * @param param An additional parameter
     */
    void onMessage(int id, int param);
}
