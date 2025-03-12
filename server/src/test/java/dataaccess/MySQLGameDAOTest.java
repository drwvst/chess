package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MySQLGameDAOTest {
    private static final MySQLGameDAO gameDAO = MySQLGameDAO.getInstance();

    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseManager.clear();
    }

    @Test
    void testCreateGamePositive() throws DataAccessException {
        GameData game = gameDAO.createGame("Chess Match");
        assertNotNull(game);
        assertEquals("Chess Match", game.gameName());

        GameData retrievedGame = gameDAO.getGame(game.gameID());
        assertNotNull(retrievedGame);
        assertEquals("Chess Match", retrievedGame.gameName());
    }

    @Test
    void testGetGameValidGameID() throws DataAccessException {
        GameData game = gameDAO.createGame("My Chess Game");
        GameData retrievedGame = gameDAO.getGame(game.gameID());

        assertEquals(game.gameID(), retrievedGame.gameID());
        assertEquals("My Chess Game", retrievedGame.gameName());
    }

    @Test
    void testGetGameInvalidGameID() {
        assertThrows(DataAccessException.class, () -> gameDAO.getGame(9999));
    }


    @Test
    void testUpdateGameInvalidGameID() {
        GameData fakeGame = new GameData(9999, "FakeWhite", "FakeBlack", "Fake Game", new ChessGame());
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(fakeGame));
    }

    @Test
    void testListGamesEmptyDatabase() throws DataAccessException {
        List<GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty());
    }

    @Test
    void testListGamesNonEmptyDatabase() throws DataAccessException {
        gameDAO.createGame("Game1");
        gameDAO.createGame("Game2");

        List<GameData> games = gameDAO.listGames();
        assertEquals(2, games.size());
    }
}
