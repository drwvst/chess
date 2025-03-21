package service;

import dataaccess.*;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    private GameService gameService;
    private MySQLAuthDAO authDAO;
    private MySQLGameDAO gameDAO;
    private String validAuthToken;
    private String anotherAuthToken;

    @BeforeEach
    void setUp() throws DataAccessException {
        authDAO = MySQLAuthDAO.getInstance();
        gameDAO = MySQLGameDAO.getInstance();
        MySQLUserDAO userDAO = MySQLUserDAO.getInstance();
        gameService = new GameService();

        // Clear previous data
        DatabaseManager.clear();

        UserData user1 = new UserData("testDude", "password", "testDude@gmail.com");
        UserData user2 = new UserData("theOtherGuy", "password", "theOtherGuy@gmail.com");

        // Ensure test users exist in the database
        userDAO.createUser(user1);
        userDAO.createUser(user2);

        // Create test users
        AuthData auth1 = authDAO.createAuth("testDude");
        AuthData auth2 = authDAO.createAuth("theOtherGuy");

        validAuthToken = auth1.authToken();
        anotherAuthToken = auth2.authToken();
    }

    @Test
    void testListGamesNoGames() throws DataAccessException {
        List<GameData> games = gameService.listGames(validAuthToken);
        assertTrue(games.isEmpty());
    }

    @Test
    void testListGamesMultipleGames() throws DataAccessException {
        gameService.createGame(validAuthToken, "Game");
        gameService.createGame(validAuthToken, "AnotherGame");
        gameService.createGame(validAuthToken, "AnothaOne");
        gameService.createGame(validAuthToken, "YoudNeverGuess");
        gameService.createGame(validAuthToken, "QuitePossiblyAnotherGame");

        List<GameData> games = gameService.listGames(validAuthToken);
        assertEquals(5, games.size());
    }

    @Test
    void testCreateGamePositive() throws DataAccessException {
        GameData game = gameService.createGame(validAuthToken, "tisButAChessGame");
        assertNotNull(game);
        assertEquals("tisButAChessGame", game.gameName());
    }

    @Test
    void testCreateGameNegativeWithInvalidAuth() {
        assertThrows(DataAccessException.class, () -> gameService.createGame("outrageousAuthToken", "Chess"));
    }

    @Test
    void testCreateGameNegativeWithBlankName() {
        assertThrows(DataAccessException.class, () -> gameService.createGame(validAuthToken, ""));
    }

    @Test
    void testJoinGamePositive() throws DataAccessException {
        GameData game = gameService.createGame(validAuthToken, "Test Game");
        gameService.joinGame(anotherAuthToken, game.gameID(), "WHITE");
        GameData updatedGame = gameDAO.getGame(game.gameID());

        assertEquals("theOtherGuy", updatedGame.whiteUsername());  // User2 joined as WHITE
    }

    @Test
    void testJoinGameNegativeWithInvalidAuth() throws DataAccessException{
        GameData game = gameService.createGame(validAuthToken, "Test Game");

        assertThrows(DataAccessException.class, () -> gameService.joinGame("invalidToken", game.gameID(), "WHITE"));
    }

    @Test
    void testJoinGameNegativeWithInvalidGameID() {
        assertThrows(DataAccessException.class, () -> gameService.joinGame(validAuthToken, 9999, "WHITE"));
    }

    @Test
    void testJoinGameNegativeWithTakenSpot() throws DataAccessException {
        GameData game = gameService.createGame(validAuthToken, "Test Game");
        gameService.joinGame(validAuthToken, game.gameID(), "WHITE");

        assertThrows(DataAccessException.class, () -> gameService.joinGame(anotherAuthToken, game.gameID(), "WHITE"));  // Second user tries
    }

}
