package client;

import dataaccess.DataAccessException;
import exception.ResponseException;
import model.AuthData;
import org.junit.jupiter.api.*;
import server.Server;
import serverFacade.ServerFacade;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void setUp() throws Exception {
        facade.clear();
    }


    @Test
    public void sampleTest() {
        assertTrue(true);
    }

    //login positive
    @Test
    void loginPositive() throws Exception{
        facade.register("bob", "bobisactuallybatman", "bob@example.com");
        var authData = facade.login("bob", "bobisactuallybatman");
        assertNotNull(authData);
        assertTrue(authData.authToken().length() > 10);
    }

    //login negative
    @Test
    void loginNegative() throws Exception{
        assertThrows(ResponseException.class, () -> facade.login("bobert", "letmein"));
    }

    //register positive
    @Test
    void registerPositive() throws Exception {
        var authData = facade.register("bobular", "IAmBatman", "bob@email.com");
        assertTrue(authData.authToken().length() > 10);
    }
    //register negative


    //clear positive

    //clear negative

    //logout positive

    //logout negative

    //createGame positive

    //createGame Negative

    //listGames positive

    //listGames negative

    //joinGame positive

    //joinGame negative

}
