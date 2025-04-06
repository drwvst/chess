package server.websocket;

import com.google.gson.Gson;
import dataaccess.*;
import model.*;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.*;
import chess.ChessGame;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private final MySQLAuthDAO authDAO;
    private final MySQLGameDAO gameDAO;
    private final ConnectionManager connectionManager;
    private final Gson gson = new Gson();

    //private final ConnectionManager connections = new ConnectionManager();

    public WebSocketHandler(){
        this.authDAO = new MySQLAuthDAO();
        this.gameDAO = new MySQLGameDAO();
        // this.gameService = new GameService(gameDAO, authDAO);
        this.connectionManager = new ConnectionManager();
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String messageJson) throws IOException, DataAccessException {
        UserGameCommand command = new Gson().fromJson(messageJson, UserGameCommand.class);
        String authToken = command.authToken();
        AuthData authData = authDAO.getAuthToken(authToken);
        String username = authData.username();

        switch(command.commandType()){
            case CONNECT -> connectUser(username, messageJson, session);
        }
    }

    private void connectUser(String playerName, String messageJson, Session session) throws IOException, DataAccessException{
        UserGameCommand connectCommand = new Gson().fromJson(messageJson, UserGameCommand.class);
        //String authToken = connectCommand.authToken();
        Integer gameID = connectCommand.gameID();

        if(gameID == null){
            wsSessionError(session, "Error: Game ID is required to execute CONNECT ws command");
            return;
        }
        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null){
            wsSessionError(session, "Error: Game " + gameID + " does not exist!");
            return;
        }

        String userRole;
        ChessGame.TeamColor userColor = null;
        if (playerName.equals(gameData.whiteUsername())) {
            userColor = ChessGame.TeamColor.WHITE;
        } else if (playerName.equals(gameData.blackUsername())) {
            userColor = ChessGame.TeamColor.BLACK;
        }
        if (userColor != null) {
            userRole = userColor.toString();
        } else {
            userRole = "observer";
        }

        connectionManager.add(playerName, gameID, session);
        //Sending LOAD_GAME ws message to ROOT CLIENT ONLY
        LoadGameMessage loadGameMessage = new LoadGameMessage(gameData.game());
        session.getRemote().sendString(gson.toJson(loadGameMessage));

        //Sending NOTIFICATION to all other clients in that chess game
        String notifString = String.format("%s joined the game as %s", playerName, userRole);
        ServerMessage serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notifString);
        connectionManager.broadcast(gameID, playerName, serverMessage);
    }





    private void wsSessionError(Session session, String errorMessage) {
        try {
            if (session != null && session.isOpen()) {
                ErrorMessage errorMsg = new ErrorMessage(errorMessage);
                session.getRemote().sendString(gson.toJson(errorMsg));
            } else {
                System.err.println("Attempted to send error to closed/null session.");
            }
        } catch (Exception e) {
            System.err.println("Error sending error message '" + errorMessage + "': " + e.getMessage());
        }
    }

    private record ErrorMessage(String message) {}

}
