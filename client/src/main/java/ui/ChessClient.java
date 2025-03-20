package ui;

import model.*;
import exception.ResponseException;
import serverFacade.ServerFacade;
import static ui.EscapeSequences.*;

import java.util.Arrays;


public class ChessClient {
    public AuthData currentUser;
    public GameData activeChessGame;
    //private String visitorName = null;
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.SIGNEDOUT;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public String eval(String input){
        try {
            var tokens = input.toLowerCase().split(" ");
            var command = (tokens.length > 0) ? tokens[0] : "help";
            var parameters = Arrays.copyOfRange(tokens, 1, tokens.length);

            if (state == State.SIGNEDOUT){
                return switch (command){
                    case "register" -> register(parameters);
                    case "login" -> login(parameters);
                    case "quit" -> "quit";
                    default -> help();
                };
            } else {
                return switch (command){
                case "creategame" -> createGame(parameters); //(needs Authtoken)
//                case "listgames" -> listGames(parameters); //(needs Authtoken)
//                case "joingame" -> joinGame(parameters); //(needs Authtoken)
//                case "logout" -> logout(parameters); //(needs Authtoken)
                    case "quit" -> "quit";
                    default -> help();
                };
            }

        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String register(String... parameters) throws ResponseException {
        if (parameters.length != 3) {
            throw new ResponseException(400, "Expected: <username> <password> <email>");
        }

        var username = parameters[0];
        var password = parameters[1];
        var email = parameters[2];

        currentUser = server.register(username, password, email);
        state = State.SIGNEDIN;

        return String.format(
                "Congratulations! You are successfully registered and signed in as %s\n%s", username, help());
    }

    public String login(String... params) throws ResponseException {
        if (params.length != 2) {
            throw new ResponseException(400, "Expected: <username> <password>");
        }
        var username = params[0];
        var password = params[1];

        currentUser = server.login(username,password);
        state = State.SIGNEDIN;

        return String.format("Successfully logged in as %s!\n%s", username, help());
    }

    public String createGame(String... params) throws ResponseException{
        assertSignedIn();
        if (params.length != 1) {
            throw new ResponseException(400, "Expected: <game name>");
        }
        var gameName = params[0];

        activeChessGame = server.createGame(currentUser.authToken(), gameName);

        return String.format(SET_TEXT_COLOR_GREEN + "Game created successfully: %s\n\n%s", gameName,help());
    }




    public String help() {
        if (state == State.SIGNEDOUT){
            return SET_TEXT_COLOR_BLUE + """
                    - Register <username> <password> <email>
                    - Login <username> <password>
                    - Help
                    - Quit
                    """;
        }
        return SET_TEXT_COLOR_BLUE + """
                - CreateGame <game name>
                - ListGames
                - JoinGame <gameID> <playerColor>
                - Logout <authentication token>
                - Help
                """;
    }

    private void assertSignedIn() throws ResponseException {
        if (state == State.SIGNEDOUT) {
            throw new ResponseException(400, "You must sign in");
        }
    }

}
