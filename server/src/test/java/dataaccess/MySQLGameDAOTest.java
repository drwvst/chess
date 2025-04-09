package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.GameStatus;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MySQLGameDAOTest {
    private static final MySQLGameDAO GAME_DAO = MySQLGameDAO.getInstance();

    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseManager.clear();
    }

    @Test
    void testCreateGamePositive() throws DataAccessException {
        GameData game = GAME_DAO.createGame("Chess Match");
        assertNotNull(game);
        assertEquals("Chess Match", game.gameName());

        GameData retrievedGame = GAME_DAO.getGame(game.gameID());
        assertNotNull(retrievedGame);
        assertEquals("Chess Match", retrievedGame.gameName());
    }

    @Test
    void testGetGameValidGameID() throws DataAccessException {
        GameData game = GAME_DAO.createGame("My Chess Game");
        GameData retrievedGame = GAME_DAO.getGame(game.gameID());

        assertEquals(game.gameID(), retrievedGame.gameID());
        assertEquals("My Chess Game", retrievedGame.gameName());
    }

    @Test
    void testGetGameInvalidGameID() {
        assertThrows(DataAccessException.class, () -> GAME_DAO.getGame(9999));
    }


    @Test
    void testUpdateGameInvalidGameID() {
        GameData fakeGame = new GameData(9999, "FakeWhite", "FakeBlack", "Fake Game", new ChessGame(), GameStatus.ACTIVE);
        assertThrows(DataAccessException.class, () -> GAME_DAO.updateGame(fakeGame));
    }

    @Test
    void testListGamesEmptyDatabase() throws DataAccessException {
        List<GameData> games = GAME_DAO.listGames();
        assertTrue(games.isEmpty());
    }

    @Test
    void testListGamesNonEmptyDatabase() throws DataAccessException {
        GAME_DAO.createGame("Game1");
        GAME_DAO.createGame("Game2");

        List<GameData> games = GAME_DAO.listGames();
        assertEquals(2, games.size());
    }
}
