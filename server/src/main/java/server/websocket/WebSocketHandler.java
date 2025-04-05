package server.websocket;

import com.google.gson.Gson;
import dataaccess.*;
import model.*;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

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

    private void connectUser(String playerName, String messageJson, Session session) throws IOException{
        UserGameCommand connectCommand = new Gson().fromJson(messageJson, UserGameCommand.class);
        String authToken = connectCommand.authToken();
        Integer gameID = connectCommand.gameID();

        if(gameID == null){
            wsSessionError(session, "Error: Game ID is required to execute CONNECT ws command");
            return;

        }



        //connectionManager.add(playerName, session);
        var serverMessage1 = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, "");


        var message = String.format("%s has joined the game", playerName);
        var serverMessage2 = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        //connectionManager.broadcast(playerName, serverMessage2);
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
