package websocket.commands;

import com.google.gson.Gson;

import java.util.Objects;

/**
 * Represents a command a user can send the server over a websocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public record UserGameCommand(websocket.commands.UserGameCommand.CommandType commandType, String authToken,
                              Integer gameID) {

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserGameCommand)) {
            return false;
        }
        UserGameCommand that = (UserGameCommand) o;
        return commandType() == that.commandType() &&
                Objects.equals(authToken(), that.authToken()) &&
                Objects.equals(gameID(), that.gameID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandType(), authToken(), gameID());
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}
