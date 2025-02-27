package service;


import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

import java.util.List;

public class GameService {
    private final GameDAO gameDAO = GameDAO.getInstance();
    private final AuthDAO authDAO = AuthDAO.getInstance();

    public List<GameData> listGames(String authToken) throws DataAccessException{
        validateAuthToken(authToken);
        return gameDAO.listGames();
    }

    public GameData createGame(String authToken, String gameName) throws DataAccessException{
        validateAuthToken(authToken);
        if(gameName == null || gameName.isBlank()){
            throw new DataAccessException("bad Request");
        }
        return gameDAO.createGame(gameName);
    }


    private AuthData validateAuthToken(String authToken) throws DataAccessException {
        AuthData authData = authDAO.getAuthToken(authToken);
        if (authData == null) {
            throw new DataAccessException("unauthorized");
        }
        return authData;
    }
}
