package server.websocket;

import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.*;
import model.*;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.*;
import websocket.messages.*;
import chess.ChessGame;

import java.io.IOException;
import java.util.Objects;

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
    public void onMessage(Session session, String messageJson) throws IOException {
        UserGameCommand command = null;
        try {
            command = new Gson().fromJson(messageJson, UserGameCommand.class);
        } catch (Exception e) {
            wsSessionError(session, "Error: Invalid command format.");
            return;
        }
        if (command == null || command.getCommandType() == null) {
            wsSessionError(session, "Error: Invalid command format - missing command type.");
            return;
        }

        String authToken = command.getAuthToken();
        AuthData authData = null;
        String username = null;

        try {
            authData = authDAO.getAuthToken(authToken);
            if (authData == null) {
                wsSessionError(session, "Error: Unauthorized - Invalid auth token.");
                return;
            }
            username = authData.username();
        } catch (DataAccessException e) {
            wsSessionError(session, "Error: Database error during authentication: " + e.getMessage());
            return;
        } catch (Exception e) {
            wsSessionError(session, "Error: Unexpected error during authentication: " + e.getMessage());
            return;
        }

        try {
            switch (command.getCommandType()) {
                case CONNECT -> connectUser(username, messageJson, session);
                case MAKE_MOVE -> MakeMoveHandler(username, messageJson, session);
                case LEAVE -> leaveHandler(username, messageJson, session);
                case RESIGN -> handleResign(username, messageJson, session);
                default -> wsSessionError(session, "Error: Unknown command type received: " + command.getCommandType());
            }
        } catch (DataAccessException e) {
            wsSessionError(session, "Error: Database error during command execution: " + e.getMessage());
        } catch (InvalidMoveException e) {
            wsSessionError(session, "Error: Invalid move - " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IOException sending message for user " + username + ": " + e.getMessage());
        } catch (Exception e) {
            wsSessionError(session, "Error: Unexpected server error during command execution: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void connectUser(String playerName, String messageJson, Session session) throws IOException, DataAccessException{
        UserGameCommand connectCommand = new Gson().fromJson(messageJson, UserGameCommand.class);
        //String authToken = connectCommand.authToken();
        Integer gameID = connectCommand.getGameID();

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
        ChessGame.TeamColor userColor = getPlayerColor(playerName, gameData); // Use helper
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
        NotificationMessage notification = new NotificationMessage(notifString);
        connectionManager.broadcast(gameID, playerName, notification);
    }

    private void MakeMoveHandler(String playerName, String messageJson, Session session) throws IOException,
            DataAccessException, InvalidMoveException {

        makeMoveCommand moveCommand = gson.fromJson(messageJson, makeMoveCommand.class);
        Integer gameID = moveCommand.getGameID();
        ChessMove move = moveCommand.getMove();

        if (gameID == null || move == null) {
            wsSessionError(session, "Game ID or Move data is NULL");
            return;
        }

        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            wsSessionError(session, "Error: Game " + gameID + " does not exist");
            return;
        }

        if (gameData.status() != GameStatus.ACTIVE) {
            wsSessionError(session, "Error: Game has already ended (" + gameData.status() + ")");
            return;
        }

        ChessGame.TeamColor playerColor = getPlayerColor(playerName, gameData);
        if (playerColor == null) {
            wsSessionError(session, "Error: Observers cannot make moves.");
            return;
        }

        ChessGame currentGame = gameData.game();
        if (currentGame.getTeamTurn() != playerColor) {
            wsSessionError(session, "It is not your turn. Please wait.");
            return;
        }

        currentGame.makeMove(move);

        //Update game in DAO
        GameData updatedGameData = new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                currentGame,
                gameData.status()
        );
        gameDAO.updateGame(updatedGameData);

        boolean gameEnded = false;
        String endMessage = null;
        ChessGame.TeamColor opponentColor = (playerColor == ChessGame.TeamColor.WHITE) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

        if (currentGame.isInCheckmate(opponentColor)) {
            gameEnded = true;
            endMessage = String.format("Checkmate! %s (%s) wins.", playerName, playerColor);
            gameDAO.updateGameStatus(gameID, GameStatus.FINISHED);
        } else if (currentGame.isInStalemate(opponentColor)) {
            gameEnded = true;
            endMessage = "Stalemate! The game is a draw.";
            gameDAO.updateGameStatus(gameID, GameStatus.FINISHED);
        } else if (currentGame.isInCheck(opponentColor)) {
            // Send check notification if game didn't end
            String checkMessage = String.format("%s is now in check!", opponentColor);
            broadcastNotification(gameID, null, checkMessage); // Notify everyone
        }


        //Broadcast game state
        broadcastGameUpdate(gameID, currentGame);

        String moveNotification = String.format("%s made move %s.", playerName, move.toString()); // Use ChessMove.toString()
        broadcastNotification(gameID, playerName, moveNotification);

        if (gameEnded && endMessage != null) {
            broadcastNotification(gameID, null, endMessage);
        }
    }

    private void leaveHandler(String playerName, String messageJson, Session session) throws DataAccessException, IOException {
        LeaveCommand leaveCmd = gson.fromJson(messageJson, LeaveCommand.class);
        Integer gameID = leaveCmd.getGameID();

        if (gameID == null) {
            wsSessionError(session, "Error: Game ID missing for LEAVE command.");
            return;
        }

        try {
            GameData gameData = gameDAO.getGame(gameID);

            if (gameData != null) {
                GameData updatedGameData = null;
                boolean playerLeft = false;


                if (Objects.equals(playerName, gameData.whiteUsername())) {
                    updatedGameData = new GameData(gameID, null, gameData.blackUsername(), gameData.gameName(), gameData.game(), gameData.status());
                    playerLeft = true;
                } else if (Objects.equals(playerName, gameData.blackUsername())) {
                    updatedGameData = new GameData(gameID, gameData.whiteUsername(), null, gameData.gameName(), gameData.game(), gameData.status());
                    playerLeft = true;
                }

                if (playerLeft && updatedGameData != null) {
                    gameDAO.updateGame(updatedGameData);
                    System.out.println("Updated game ID " + gameID + " - removed player " + playerName);
                }
            }
        } catch (DataAccessException e) {
            System.err.println("Database error trying to update game during leave for user " + playerName + " game "
                    + gameID + ": " + e.getMessage());
        }

        connectionManager.remove(playerName);

        String notificationText = String.format("%s left the game.", playerName);
        NotificationMessage notification = new NotificationMessage(notificationText);
        connectionManager.broadcast(gameID, playerName, notification);
    }

    private void handleResign(String username, String messageJson, Session session) throws DataAccessException, IOException {
        ResignCommand resignCmd = gson.fromJson(messageJson, ResignCommand.class);
        Integer gameID = resignCmd.getGameID();

        if (gameID == null) {
            wsSessionError(session, "Error: Game ID missing for RESIGN command.");
            return;
        }

        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            wsSessionError(session, "Error: Game not found (ID: " + gameID + ").");
            return;
        }

        if (gameData.status() != GameStatus.ACTIVE) {
            wsSessionError(session, "Error: Cannot resign, game is already over (" + gameData.status() + ").");
            return;
        }

        ChessGame.TeamColor resigningColor = getPlayerColor(username, gameData); // Use helper
        if (resigningColor == null) {
            wsSessionError(session, "Error: Observers cannot resign.");
            return;
        }

        gameDAO.updateGameStatus(gameID, GameStatus.FINISHED);
        System.out.println("Game ID " + gameID + " marked as finished due to resignation by " + username);

        String notificationText = String.format("%s (%s) has resigned. The game is over.", username, resigningColor);
        NotificationMessage notification = new NotificationMessage(notificationText);
        connectionManager.broadcast(gameID, null, notification);
    }


    //helpers
    private ChessGame.TeamColor getPlayerColor(String playerName, GameData gameData) {
        if (playerName.equals(gameData.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        } else if (playerName.equals(gameData.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        }
        return null;
    }

    private void broadcastGameUpdate(Integer gameID, ChessGame game) throws IOException {
        LoadGameMessage loadGameMsg = new LoadGameMessage(game);
        connectionManager.broadcast(gameID, null, loadGameMsg);
    }

    private void broadcastNotification(Integer gameID, String excludePlayer, String messageText) throws IOException {
        NotificationMessage notification = new NotificationMessage(messageText);
        connectionManager.broadcast(gameID, excludePlayer, notification);
    }


    private void wsSessionError(Session session, String errorMessage) {
        try {
            if (session != null && session.isOpen()) {
                ErrorMessage errorMsg = new ErrorMessage(errorMessage);
                String errorJsonString = gson.toJson(errorMsg);
                session.getRemote().sendString(errorJsonString);
            } else {
                System.err.println("Attempted to send error to closed/null session: " + errorMessage);
            }
        } catch (Exception e) {
            System.err.println("DOUBLE ERROR: Failed to send error message '" + errorMessage + "' to client: " + e.getMessage());
        }
    }

}
