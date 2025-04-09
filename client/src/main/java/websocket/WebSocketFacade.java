package websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import exception.ResponseException;

import websocket.commands.*;
import websocket.messages.*;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class WebSocketFacade extends Endpoint{
    Session session;
    NotificationHandler notificationHandler;
    private final Gson gson = new Gson();

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    try {
                        ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
                        notificationHandler.notify(serverMessage);

                    } catch (Exception e) {
                        System.err.println("WebSocket JSON Deserialization Error: " + e.getMessage());
                        ErrorMessage errorForHandler = new ErrorMessage("Client Error: Could not parse server message. " + e.getMessage());
                        notificationHandler.notify(errorForHandler);
                    }
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, "WebSocket Connection Failed: " + ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        //System.out.println("WebSocket connection opened.");
    }

//    @Override
//    public void onClose(Session session, CloseReason closeReason) {
//        System.out.println("WebSocket connection closed: " + closeReason.getReasonPhrase());
//    }

    @Override
    public void onError(Session session, Throwable thr) {
        System.err.println("WebSocket error: " + thr.getMessage());
    }

    public void connect(String authToken, int gameID, ChessGame.TeamColor playerColor) throws ResponseException{
        try{
            var command = new ConnectCommand(authToken, gameID, playerColor);
            send(command);
        } catch (IOException e){
            throw new ResponseException(500, "Send Failed (Connect): " + e.getMessage());
        }
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws ResponseException {
        try {
            var command = new MakeMoveCommand(authToken, gameID, move);
            send(command);
        } catch (IOException ex) {
            throw new ResponseException(500, "Send Failed (Make Move): " + ex.getMessage());
        }
    }




    private void send(UserGameCommand command) throws IOException {
        if (session.isOpen()) {
            this.session.getBasicRemote().sendText(gson.toJson(command));
        } else {
            System.err.println("Error: WebSocket session is not open. Cannot send command.");
        }
    }
}
