package service;


import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.GameStatus;

import javax.xml.crypto.Data;
import java.util.List;
import java.util.Objects;

public class GameService {
//    private final GameDAO gameDAO = GameDAO.getInstance();
//    private final AuthDAO authDAO = AuthDAO.getInstance();
    private final MySQLGameDAO mySQLGameDAO = MySQLGameDAO.getInstance();
    private final MySQLAuthDAO mySQLAuthDAO = MySQLAuthDAO.getInstance();

    public List<GameData> listGames(String authToken) throws DataAccessException{
        validateAuthToken(authToken);
        return mySQLGameDAO.listGames();
    }

    public GameData createGame(String authToken, String gameName) throws DataAccessException{
        validateAuthToken(authToken);
        if(gameName == null || gameName.isBlank()){
            throw new DataAccessException("bad Request");
        }
        return mySQLGameDAO.createGame(gameName);
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
        AuthData authData = validateAuthToken(authToken);

        if (gameID <= 0) {
            throw new DataAccessException("bad request");
        }

        GameData game = mySQLGameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("bad request");
        }

        if (playerColor == null || playerColor.isBlank()) {
            throw new DataAccessException("bad request");
        }

        GameStatus currentStatus = game.status();
        GameData updatedGame;

        if ("WHITE".equalsIgnoreCase(playerColor)) {
            if (game.whiteUsername() != null && !game.whiteUsername().equals(authData.username())) {
                throw new DataAccessException("already taken");
            }
            updatedGame = new GameData(game.gameID(), authData.username(), game.blackUsername(), game.gameName(), game.game(), currentStatus);
        } else if ("BLACK".equalsIgnoreCase(playerColor)) {
            if (game.blackUsername() != null && !game.blackUsername().equals(authData.username())) {
                throw new DataAccessException("already taken");
            }
            updatedGame = new GameData(game.gameID(), game.whiteUsername(), authData.username(), game.gameName(), game.game(), currentStatus);
        } else {
            throw new DataAccessException("bad request");
        }

        mySQLGameDAO.updateGame(updatedGame);
    }



    private AuthData validateAuthToken(String authToken) throws DataAccessException {
        try {
            return mySQLAuthDAO.getAuthToken(authToken);
        } catch (DataAccessException e) {
            throw new DataAccessException("unauthorized");
        }
    }
}
