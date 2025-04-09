package websocket.messages;

/**
 * A server message representing a general notification to be sent over the WebSocket.
 */
public class NotificationMessage extends ServerMessage {
    private final String notification;

    public NotificationMessage(String notification) {
        super(ServerMessageType.NOTIFICATION, notification);
        this.notification = notification;
    }

    public String getNotification() {
        return notification;
    }
}