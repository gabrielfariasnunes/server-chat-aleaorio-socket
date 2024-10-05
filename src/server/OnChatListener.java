package server;

public interface OnChatListener {
    public void onMessage(String message);
    public void onDisconnected()
    public void onChatCreated()
}
