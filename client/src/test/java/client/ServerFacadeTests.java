package client;

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
    @Test
    void registerMissingInformation() throws Exception{
        assertThrows(ResponseException.class,
                () -> facade.register("bobatman", "", "bob@email.com"));
    }
    @Test
    void registerDuplicateUser() throws Exception{
        facade.register("bobatman", "IDontHaveParents", "bob@email.com");
        assertThrows(ResponseException.class,
                () -> facade.register("bobatman", "IDontHaveParents", "bob@email.com"));
    }

    //clear positive
    @Test
    void clearPositive() throws Exception{
        facade.register("bobAndDefinitleyNotBatman", "IOnlyWearBlack", "notBatman@email.com");
        AuthData auth = facade.login("bobAndDefinitleyNotBatman", "IOnlyWearBlack");
        assertNotNull(auth);

        facade.clear();

        assertThrows(ResponseException.class,
                () -> facade.login("bobAndDefinitleyNotBatman", "IOnlyWearBlack"));
    }

    //clear negative
    @Test
    void clearNegative(){
        assertDoesNotThrow(() -> facade.clear());
    }

    //logout positive
    @Test
    void logoutPositive() throws Exception {
        AuthData auth = facade.register(
                "bobAndDefinitleyNotBatman",
                "IOnlyWearBlack",
                "notBatman@email.com");
        assertNotNull(auth);
        assertNotNull(auth.authToken());

        facade.logout(auth.authToken());
        assertThrows(ResponseException.class, () -> facade.listGames(auth.authToken()));
    }

    //logout negative
    @Test
    void logoutMissingInfo() throws Exception {
        facade.register("bobAndDefinitleyNotBatman", "IOnlyWearBlack", "bob@mail");
        assertThrows(ResponseException.class,
                () -> facade.logout(""));
    }

    @Test
    void logoutUserDne() throws Exception {
        assertThrows(ResponseException.class,
                () -> facade.logout("invalidAuth"));
    }


    //createGame positive

    //createGame Negative

    //listGames positive

    //listGames negative

    //joinGame positive

    //joinGame negative

}
