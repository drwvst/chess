package service;

import dataaccess.DataAccessException;
import model.*;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import dataaccess.*;

import static org.junit.jupiter.api.Assertions.*;


public class UserServiceTest {
    private UserService userService;

    @BeforeEach
    void setUp() throws DataAccessException {
        userService = new UserService();
        ClearService clearService = new ClearService();
        clearService.clear();
    }

    @Test
    void testPositiveRegister() throws DataAccessException{
        RegisterRequest request = new RegisterRequest("bobgustus","bobsSecretPassword","bob@gmail.com");
        RegisterResult result = userService.register(request);

        assertNotNull(result.authToken());
        assertEquals("bobgustus", result.username());
    }

    @Test
    void testRegisterUserAlreadyExists(){
        RegisterRequest request = new RegisterRequest("bobgustus","bobsSecretPassword","bob@gmail.com");

        assertDoesNotThrow(() -> userService.register(request));
        assertThrows(DataAccessException.class, () -> userService.register(request));
    }

    @Test
    void testRegisterWithMissingFields(){
        RegisterRequest request = new RegisterRequest("bobgustus","bobsSecretPassword","");

        assertThrows(DataAccessException.class, () -> userService.register(request));
    }

    @Test
    void testPositiveLogin() throws DataAccessException{
        RegisterRequest registerRequest = new RegisterRequest("bobgustus","bobsSecretPassword","bob@gmail.com");
        assertDoesNotThrow(() -> userService.register(registerRequest));

        LoginRequest request = new LoginRequest("bobgustus", "bobsSecretPassword");
        LoginResult result = userService.login(request);

        assertNotNull(result.authToken());
        assertEquals("bobgustus", result.username());
    }

    @Test
    void testFailedLogin() throws DataAccessException{
        RegisterRequest registerRequest = new RegisterRequest("bobgustus","bobsSecretPassword","bob@gmail.com");
        assertDoesNotThrow(() -> userService.register(registerRequest));

        LoginRequest request = new LoginRequest("bobgustusjr", "bobjr'sSecretPassword");

        assertThrows(DataAccessException.class, () -> userService.login(request));
    }

    @Test
    void testPositiveLogout() throws DataAccessException{
        RegisterRequest registerRequest = new RegisterRequest("bobgustus","bobsSecretPassword","bob@gmail.com");
        RegisterResult result = userService.register(registerRequest);

        assertDoesNotThrow(() -> userService.logout(result.authToken()));
        assertThrows(DataAccessException.class, () -> AuthDAO.getInstance().getAuthToken(result.authToken()));
    }

    @Test
    void testLogoutWithInvalidToken() {
        assertThrows(DataAccessException.class, () -> userService.logout("quitePossiblyAnInvalidToken"));
    }

}
