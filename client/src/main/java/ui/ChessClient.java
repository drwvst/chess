package ui;

import model.*;
import exception.ResponseException;
import serverFacade.ServerFacade;


public class ChessClient {
    //private String visitorName = null;
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.SIGNEDOUT;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
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
                """;
    }

    private void assertSignedIn() throws ResponseException {
        if (state == State.SIGNEDOUT) {
            throw new ResponseException(400, "You must sign in");
        }
    }

}
