package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class MySQLUserDAOTest {
    private static final MySQLUserDAO userDAO = MySQLUserDAO.getInstance();

    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseManager.clear();
    }

    @Test
    void testCreateUser_Positive() throws DataAccessException {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        assertDoesNotThrow(() -> userDAO.createUser(user));

        UserData retrievedUser = userDAO.getUser("testUser");
        assertNotNull(retrievedUser);
        assertEquals("testUser", retrievedUser.username());
    }

    @Test
    void testCreateUser_DuplicateUsername() throws DataAccessException {
        UserData user1 = new UserData("duplicateUser", "password123", "email1@example.com");
        UserData user2 = new UserData("duplicateUser", "password456", "email2@example.com");

        userDAO.createUser(user1);
        assertThrows(DataAccessException.class, () -> userDAO.createUser(user2));
    }

    @Test
    void testGetUser_UserExists() throws DataAccessException {
        UserData user = new UserData("existingUser", "securePass", "user@example.com");
        userDAO.createUser(user);

        UserData retrievedUser = userDAO.getUser("existingUser");
        assertEquals("existingUser", retrievedUser.username());
        assertEquals("securePass", retrievedUser.password());
    }

    @Test
    void testGetUser_UserNotFound() {
        assertThrows(DataAccessException.class, () -> userDAO.getUser("nonExistentUser"));
    }
}