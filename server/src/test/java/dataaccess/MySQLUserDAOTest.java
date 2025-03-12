package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class MySQLUserDAOTest {
    private static final MySQLUserDAO USER_DAO = MySQLUserDAO.getInstance();

    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseManager.clear();
    }

    @Test
    void testCreateUserPositive() throws DataAccessException {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        assertDoesNotThrow(() -> USER_DAO.createUser(user));

        UserData retrievedUser = USER_DAO.getUser("testUser");
        assertNotNull(retrievedUser);
        assertEquals("testUser", retrievedUser.username());
    }

    @Test
    void testCreateUserDuplicateUsername() throws DataAccessException {
        UserData user1 = new UserData("duplicateUser", "password123", "email1@example.com");
        UserData user2 = new UserData("duplicateUser", "password456", "email2@example.com");

        USER_DAO.createUser(user1);
        assertThrows(DataAccessException.class, () -> USER_DAO.createUser(user2));
    }

    @Test
    void testGetUserUserExists() throws DataAccessException {
        UserData user = new UserData("existingUser", "securePass", "user@example.com");
        USER_DAO.createUser(user);

        UserData retrievedUser = USER_DAO.getUser("existingUser");
        assertEquals("existingUser", retrievedUser.username());
        assertEquals("securePass", retrievedUser.password());
    }

    @Test
    void testGetUserUserNotFound() {
        assertThrows(DataAccessException.class, () -> USER_DAO.getUser("nonExistentUser"));
    }
}