package ui;

import model.*;
import exception.ResponseException;
import serverFacade.ServerFacade;

import java.util.Arrays;


public class ChessClient {
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

            return switch (command){
                case "register" -> register(parameters);
//                case "login" -> login(parameters);
//                case "creategame" -> createGame(parameters);
//                case "listgames" -> listGames(parameters);
//                case "joingame" -> joinGame(parameters);
//                case "logout" -> logout(parameters);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String register(String... parameters) throws ResponseException {
        if (parameters.length != 3) {
            throw new ResponseException(400, "Expected: register <username> <password> <email>");
        }

        var username = parameters[0];
        var password = parameters[1];
        var email = parameters[2];

        server.register(username, password, email);
        state = State.SIGNEDIN;

        return String.format("Congratulations! You are successfully registered and signed in as %s", username);
    }




    public String help() {
        if (state == State.SIGNEDOUT){
            return """
                    - register <username> <password> <email>
                    - login <username> <password>
                    - quit
                    """;
        }
        return """
                - createGame <authentication token> <game name>
                - listGames  <authenication token>
                - joinGame <authenication token> <gameID> <playerColor>
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
