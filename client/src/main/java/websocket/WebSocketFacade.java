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
            url = url.replace("http", "ws"); // Convert HTTP URL to WebSocket URL
            URI socketURI = new URI(url + "/connect"); // Standardized endpoint, adjust if server uses "/ws"
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
}
