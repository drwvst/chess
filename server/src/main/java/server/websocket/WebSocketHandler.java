package server.websocket;

import com.google.gson.Gson;
import dataaccess.*;
import exception.ResponseException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import java.io.IOException;
import java.util.Timer;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws  IOException {
        UserGameCommand action = new Gson().fromJson(message, UserGameCommand.class);
        switch(action.commandType()){
            case CONNECT -> connectUser(action.userName(), session);
        }
    }

    private void connectUser(String playerName, Session session) throws IOException{
        connections.add(playerName, session);
        var message = String.format("%s has joined the game", playerName);
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(playerName, serverMessage);
    }

}
