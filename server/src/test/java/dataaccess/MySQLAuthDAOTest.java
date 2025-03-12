package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class MySQLAuthDAOTest {
    private static final MySQLAuthDAO AUTH_DAO = MySQLAuthDAO.getInstance();

    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseManager.clear();
    }

    @Test
    void testCreateAuthPositive() throws DataAccessException {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        MySQLUserDAO.getInstance().createUser(user);

        AuthData auth = AUTH_DAO.createAuth("testUser");
        assertNotNull(auth);
        assertNotNull(auth.authToken());

        AuthData retrievedAuth = AUTH_DAO.getAuthToken(auth.authToken());
        assertEquals("testUser", retrievedAuth.username());
    }

    @Test
    void testGetAuthTokenValidToken() throws DataAccessException {
        UserData user = new UserData("validUser", "password123", "valid@example.com");
        MySQLUserDAO.getInstance().createUser(user);

        AuthData auth = AUTH_DAO.createAuth("validUser");

        AuthData retrievedAuth = AUTH_DAO.getAuthToken(auth.authToken());
        assertEquals(auth.authToken(), retrievedAuth.authToken());
        assertEquals("validUser", retrievedAuth.username());
    }

    @Test
    void testGetAuthTokenInvalidToken() {
        assertThrows(DataAccessException.class, () -> AUTH_DAO.getAuthToken("invalidToken"));
    }

    @Test
    void testDeleteAuthPositive() throws DataAccessException {
        UserData user = new UserData("deleteUser", "password123", "delete@example.com");
        MySQLUserDAO.getInstance().createUser(user);

        AuthData auth = AUTH_DAO.createAuth("deleteUser");
        assertNotNull(auth);

        assertDoesNotThrow(() -> AUTH_DAO.deleteAuth(auth.authToken()));
        assertThrows(DataAccessException.class, () -> AUTH_DAO.getAuthToken(auth.authToken()));
    }

    @Test
    void testDeleteAuthNonExistentToken() {
        assertThrows(DataAccessException.class, () -> AUTH_DAO.deleteAuth("fakeToken"));
    }
}
