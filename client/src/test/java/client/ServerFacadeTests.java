package client;

import exception.ResponseException;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;
import serverFacade.ServerFacade;

import java.util.List;

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
    static void stopServer() throws Exception{
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
        assertThrows(ResponseException.class, () -> facade.login("bobert", "LetMeIn"));
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
    @Test
    void createGamePositive() throws Exception{
        AuthData auth = facade.register(
                "bobButLikeMaybeBatman", "lobsterThermidor", "batBob@batmail.com");
        GameData gameData = facade.createGame(auth.authToken(), "BobsBatGame");
        assertEquals(1, gameData.gameID());
        assertNull(gameData.whiteUsername());
        assertNull(gameData.blackUsername());
    }

    @Test
    void createGameMultipleGames() throws Exception{
        AuthData auth = facade.register(
                "bobButLikeMaybeBatman", "lobsterThermidor", "batBob@batmail.com");
        GameData gameData = facade.createGame(auth.authToken(), "BobsBatGame");
        assertEquals(1, gameData.gameID());
        GameData gameData2 = facade.createGame(auth.authToken(), "Bobs Second BatGame");
        assertEquals(2, gameData2.gameID());
        assertNull(gameData.whiteUsername());
        assertNull(gameData.blackUsername());
    }

    //createGame Negative
    @Test
    void createGameNoGameName() throws Exception {
        AuthData auth = facade.register(
                "IThinkIAmBatman", "megaLobsterThermidor", "batBob@batmail.com");
        assertThrows(ResponseException.class,
                () -> facade.createGame(auth.authToken(), ""));
    }

    @Test
    void createGameNameAlreadyExists() throws Exception {
        AuthData auth = facade.register(
                "IAmBatman", "alfredTheButtlerWithTwoTs", "batBob@batmail.com");
        facade.createGame(auth.authToken(), "BatGame");
        assertThrows(ResponseException.class,
                () -> facade.createGame(auth.authToken(), "BatGame"));
    }

    @Test
    void createGameInvalidAuth() throws Exception {
        AuthData auth = facade.register(
                "IAmBatman", "alfredTheButtlerWithTwoTs", "batBob@batmail.com");
        assertThrows(ResponseException.class,
                () -> facade.createGame("Huzzah!", "BatGame"));
    }

    //listGames positive
    @Test
    void listGamesOnePositive() throws Exception{
        AuthData auth = facade.register(
                "IAmBatman", "alfredTheButtlerWithTwoTs", "batBob@batmail.com");
        facade.createGame(auth.authToken(), "BatGame");
        List<GameData> gameData = facade.listGames(auth.authToken());
        assertEquals(1, gameData.size());
    }

    @Test
    void listGamesMultipleGamesPositive() throws Exception{
        AuthData auth = facade.register(
                "IAmBatman", "alfredTheButtlerWithTwoTs", "batBob@batmail.com");
        GameData gameData = facade.createGame(auth.authToken(), "BatGame");
        GameData gameData2 = facade.createGame(auth.authToken(), "BatGameButDarkAndBrooding");
        GameData gameData3 = facade.createGame(auth.authToken(), "BatmanLivesHere");
        GameData gameData4 = facade.createGame(auth.authToken(), "BatCave");
        List<GameData> gameList = facade.listGames(auth.authToken());
        assertEquals(4, gameList.size());
        assertEquals(1, gameData.gameID());
        assertEquals(2, gameData2.gameID());
        assertEquals(3, gameData3.gameID());
        assertEquals(4, gameData4.gameID());

    }

    @Test
    void listGamesNoGamesPositive() throws Exception{
        AuthData auth = facade.register(
                "IAmBatman", "alfredTheButtlerWithTwoTs", "batBob@batmail.com");
        List<GameData> gameData = facade.listGames(auth.authToken());
        assertEquals(0, gameData.size());
    }

    @Test
    void listGamesProperGameIds() throws Exception {
        AuthData auth = facade.register(
                "IAmBatman", "alfredTheButtlerWithTwoTs", "batBob@batmail.com");
        facade.createGame(auth.authToken(), "BatGame");
        facade.createGame(auth.authToken(), "BatGameButDarkAndBrooding");
        facade.createGame(auth.authToken(), "BatmanLivesHere");
        facade.createGame(auth.authToken(), "BatCave");
        List<GameData> gameData = facade.listGames(auth.authToken());
        assertEquals(4, gameData.size());
    }

    //listGames negative
    @Test
    void listGamesInvalidAuth() throws Exception{
        assertThrows(ResponseException.class, () -> facade.listGames("sneakyAuth"));
    }

    @Test
    void listGamesNoAuth() throws Exception{
        assertThrows(ResponseException.class, () -> facade.listGames(""));
    }

    //joinGame positive
    @Test
    void joinGamePositive() throws Exception{
        AuthData authP1 = facade.register(
                "IAmBatman", "alfredTheButtlerWithTwoTs", "batBob@batmail.com");
        facade.createGame(authP1.authToken(), "BatGame");
        facade.createGame(authP1.authToken(), "BatmansSecondGame");
        facade.joinGame(authP1.authToken(),2, "white");

        AuthData authP2 = facade.register(
                "BatmansHomie", "hahaGoodPassword", "IKnowBatman@gmail");
        facade.joinGame(authP2.authToken(),2, "black");

        List<GameData> gameDataList = facade.listGames(authP1.authToken());
        assertEquals("IAmBatman", gameDataList.get(1).whiteUsername());
        assertEquals("BatmansHomie", gameDataList.get(1).blackUsername());
    }

    //joinGame negative
    @Test
    void joinGameDne() throws Exception{
        AuthData authP1 = facade.register(
                "IAmBatman", "alfredTheButtlerWithTwoTs", "batBob@batmail.com");
        assertThrows(ResponseException.class,
                () -> facade.joinGame(authP1.authToken(),1, "white"));
    }
}
