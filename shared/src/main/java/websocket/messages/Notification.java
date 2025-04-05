package websocket.messages;

import com.google.gson.Gson;

public record Notification(Type type, String message) {
    public enum Type {
        USER_CONNECTED,
        NEW_OBSERVER,
        USER_MADE_MOVE,
        USER_LEFT_GAME,
        USER_RESIGNED,
        PLAYER_IN_CHECK,
        PLAYER_IN_CHECKMATE
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}