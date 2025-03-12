package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class MySQLAuthDAOTest {
    private static final MySQLAuthDAO authDAO = MySQLAuthDAO.getInstance();

    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseManager.clear();
    }

    @Test
    void testCreateAuthPositive() throws DataAccessException {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        MySQLUserDAO.getInstance().createUser(user);

        AuthData auth = authDAO.createAuth("testUser");
        assertNotNull(auth);
        assertNotNull(auth.authToken());

        AuthData retrievedAuth = authDAO.getAuthToken(auth.authToken());
        assertEquals("testUser", retrievedAuth.username());
    }

    @Test
    void testGetAuthTokenValidToken() throws DataAccessException {
        UserData user = new UserData("validUser", "password123", "valid@example.com");
        MySQLUserDAO.getInstance().createUser(user);

        AuthData auth = authDAO.createAuth("validUser");

        AuthData retrievedAuth = authDAO.getAuthToken(auth.authToken());
        assertEquals(auth.authToken(), retrievedAuth.authToken());
        assertEquals("validUser", retrievedAuth.username());
    }

    @Test
    void testGetAuthTokenInvalidToken() {
        assertThrows(DataAccessException.class, () -> authDAO.getAuthToken("invalidToken"));
    }

    @Test
    void testDeleteAuthPositive() throws DataAccessException {
        UserData user = new UserData("deleteUser", "password123", "delete@example.com");
        MySQLUserDAO.getInstance().createUser(user);

        AuthData auth = authDAO.createAuth("deleteUser");
        assertNotNull(auth);

        assertDoesNotThrow(() -> authDAO.deleteAuth(auth.authToken()));
        assertThrows(DataAccessException.class, () -> authDAO.getAuthToken(auth.authToken()));
    }

    @Test
    void testDeleteAuthNonExistentToken() {
        assertThrows(DataAccessException.class, () -> authDAO.deleteAuth("fakeToken"));
    }
}
