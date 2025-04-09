package websocket.commands;

public class LeaveCommand extends UserGameCommand {
    public LeaveCommand(String authToken, Integer gameID) {
        super(UserGameCommand.CommandType.LEAVE, authToken, gameID);
    }
}