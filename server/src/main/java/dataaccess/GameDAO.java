package dataaccess;

import chess.ChessGame;
import model.GameData;
import java.util.*;

public class GameDAO {
    private static final GameDAO INSTANCE = new GameDAO();
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    private GameDAO() {}

    public static GameDAO getInstance() {
        return INSTANCE;
    }

    public List<GameData> listGames() {
        return new ArrayList<>(games.values());
    }

    public GameData createGame(String gameName){
        GameData newGame = new GameData(nextGameID++, null, null, gameName, new ChessGame());
        games.put(newGame.gameID(), newGame);
        return newGame;
    }

    public GameData getGame(int gameID){
        return games.get(gameID);
    }

    public void updateGame(GameData game){
        games.put(game.gameID(), game);
    }

    public void clear(){
        games.clear();
        nextGameID = 1;
    }
}
