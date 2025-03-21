package ui;

import chess.*;
import model.*;
import exception.ResponseException;
import serverFacade.ServerFacade;
import static ui.EscapeSequences.*;

import java.util.Arrays;
import java.util.List;


public class ChessClient {
    public AuthData currentUser;
    public ChessGame activeChessGame;
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
                    case "quit" -> quit();
                    default -> help();
                };
            } else if (state == State.SIGNEDIN){
                return switch (command){
                    case "creategame" -> createGame(parameters);
                    case "listgames" -> listGames(parameters);
                    case "joingame" -> joinGame(parameters);
                    case "logout" -> logout(parameters);
                    case "quit" -> quit();
                    default -> help();
                };
            } else { //in GAMESTATE
                return switch (command){
                    case "help" -> "you are in Gamestate";
                    //case "quitgame" -> quitGame();
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

        return String.format(SET_TEXT_COLOR_GREEN + "Congratulations! You are successfully " +
                "registered and signed in as %s\n%s", username, help());
    }

    public String login(String... params) throws ResponseException {
        if (params.length != 2) {
            throw new ResponseException(400, "Expected: <username> <password>");
        }
        var username = params[0];
        var password = params[1];

        currentUser = server.login(username,password);
        state = State.SIGNEDIN;

        return String.format(SET_TEXT_COLOR_GREEN + "Successfully logged in as %s!\n%s", username, help());
    }

    public String createGame(String... params) throws ResponseException{
        assertSignedIn();
        if (params.length != 1) {
            throw new ResponseException(400, "Expected: <game name>");
        }
        var gameName = params[0];

        server.createGame(currentUser.authToken(), gameName);

        return String.format(SET_TEXT_COLOR_GREEN + "Game created successfully: %s\n\n%s", gameName,help());
    }

    public String listGames(String... params) throws ResponseException{
        assertSignedIn();
        if (params.length != 0) {
            throw new ResponseException(400, "To list active games, simply type 'Listgames'");
        }

        List<GameData> gameDataList = server.listGames(currentUser.authToken());
        if(gameDataList.isEmpty()){
            return String.format(SET_TEXT_COLOR_RED + SET_TEXT_BOLD +
                    "There are currently no active games! \n\n" + RESET_TEXT_BOLD_FAINT + "%s", help());
        }

        StringBuilder outputList = new StringBuilder();
        for(GameData data : gameDataList){
            var whitePlayerString = (data.whiteUsername() == null) ?
                    SET_TEXT_COLOR_GREEN + "White player available!" : SET_TEXT_COLOR_LIGHT_GREY + data.whiteUsername();
            var blackPlayerString = (data.blackUsername() == null) ?
                    SET_TEXT_COLOR_GREEN + "Black player available!" : SET_TEXT_COLOR_LIGHT_GREY + data.blackUsername();

            if(data.whiteUsername() != null && data.blackUsername() != null){
                outputList.append(String.format(SET_TEXT_COLOR_BLUE + SET_TEXT_BOLD + "%d) %s" +
                        FUN_RIGHT_ARROW + "\n" + SET_TEXT_COLOR_LIGHT_GREY + RESET_TEXT_BOLD_FAINT +
                        "    White Player: " + whitePlayerString +
                        "\n    Black Player: " + blackPlayerString + "\n", data.gameID(), data.gameName()));
            } else {
                outputList.append(String.format(SET_TEXT_COLOR_BLUE + SET_TEXT_BOLD + "%d) %s" +
                        FUN_RIGHT_ARROW + "\n" + SET_TEXT_COLOR_WHITE + RESET_TEXT_BOLD_FAINT +
                        "    White Player: " + whitePlayerString + SET_TEXT_COLOR_WHITE +
                        "\n    Black Player: " + blackPlayerString + "\n", data.gameID(), data.gameName()));
            }
        }

        return String.format(SET_TEXT_COLOR_BLUE + SET_TEXT_BOLD + WHITE_KING + "Games" +
                WHITE_KING + "\n%s\n%s", outputList, help());
    }

    public String joinGame(String... params) throws ResponseException{
        assertSignedIn();
        if (params.length != 2) {
            throw new ResponseException(400, "Expected: <gameID> <playerColor>");
        }

        int gameID = Integer.parseInt(params[0]);
        var playerColor = params[1];


        server.joinGame(currentUser.authToken(), gameID, playerColor);

        List<GameData> games = server.listGames(currentUser.authToken());
        for(GameData game : games){
            if(game.gameID() == gameID){
                activeChessGame = game.game();
                state = State.GAMESTATE;
                return displayBoard(activeChessGame, playerColor) + String.format("Game %d joined successfully!", gameID);
            }
        }

        throw new ResponseException(400, "Failed to retrieve game after joining.");
        //printBoard(ChessGame game, PlayerColor playerColor)
    }

    public String logout(String... params) throws ResponseException{
        assertSignedIn();
        if (params.length != 0) {
            throw new ResponseException(400, "To logout, simply type 'logout'");
        }

        server.logout(currentUser.authToken());
        state = State.SIGNEDOUT;
        return String.format(SET_TEXT_COLOR_GREEN + "You have successfully logged out!\n\n%s", help());
    }

    public String displayBoard(ChessGame chessGame, String playerColor){
        StringBuilder boardString = new StringBuilder();
        ChessBoard board = chessGame.getBoard();
        for (int row = 8; row >= 1; row--) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));

                if((row+col) % 2 == 0){
                    boardString.append(SET_BG_COLOR_DARK_BROWN);
                } else {
                    boardString.append(SET_BG_COLOR_LIGHT_BROWN);
                }

                if (piece != null) {
                    String pieceChar = getPieceChar(piece, playerColor);
                    boardString.append(pieceChar);
                } else {
                    boardString.append(EMPTY);
                }
                boardString.append(RESET_BG_COLOR);
            }
            //boardString.append(RESET_BG_COLOR);
            boardString.append("\n");
        }
        boardString.append(RESET_BG_COLOR);
        boardString.append(RESET_TEXT_COLOR);
        boardString.append("\n");
        return boardString.toString();
    }

    private String getPieceChar(ChessPiece piece, String playerColor) {
        boolean isBlack = piece.getTeamColor() == ChessGame.TeamColor.BLACK;

        boolean showWhitePieces = playerColor.equalsIgnoreCase("white");

        return switch (piece.getPieceType()) {
            case KING -> (isBlack == showWhitePieces) ?
                    SET_TEXT_COLOR_BLACK + BLACK_KING : SET_TEXT_COLOR_WHITE + BLACK_KING;
            case QUEEN -> (isBlack == showWhitePieces) ?
                    SET_TEXT_COLOR_BLACK + BLACK_QUEEN : SET_TEXT_COLOR_WHITE + BLACK_QUEEN;
            case ROOK -> (isBlack == showWhitePieces) ?
                    SET_TEXT_COLOR_BLACK + BLACK_ROOK : SET_TEXT_COLOR_WHITE + BLACK_ROOK;
            case BISHOP -> (isBlack == showWhitePieces) ?
                    SET_TEXT_COLOR_BLACK + BLACK_BISHOP : SET_TEXT_COLOR_WHITE + BLACK_BISHOP;
            case KNIGHT -> (isBlack == showWhitePieces) ?
                    SET_TEXT_COLOR_BLACK + BLACK_KNIGHT : SET_TEXT_COLOR_WHITE + BLACK_KNIGHT;
            case PAWN -> (isBlack == showWhitePieces) ?
                    SET_TEXT_COLOR_BLACK + BLACK_PAWN : SET_TEXT_COLOR_WHITE + BLACK_PAWN;
            default -> EMPTY;
        };
    }





    public String help() {
        if (state == State.SIGNEDOUT){
            return SET_TEXT_COLOR_BLUE + """
                    - Register <username> <password> <email>
                    - Login <username> <password>
                    - Help
                    - Quit
                    """;
        } else if(state == State.SIGNEDIN){
            return SET_TEXT_COLOR_BLUE + """
                - CreateGame <game name>
                - ListGames
                - JoinGame <gameID> <playerColor>
                - Logout <authentication token>
                - Help
                - Quit
                """;
        } else { //GAMESTATE
            return SET_TEXT_COLOR_BLUE + """
                    - quitGame
                    - Help
                    """;
        }
    }

    public String quit() throws ResponseException {
        if (state == State.SIGNEDIN) {
            server.logout(currentUser.authToken());
            state = State.SIGNEDOUT;
            System.out.println(SET_TEXT_COLOR_GREEN + "You have been logged out. Goodbye!");
            return "quit";
        }
        return "quit";
    }


    private void assertSignedIn() throws ResponseException {
        if (state == State.SIGNEDOUT) {
            throw new ResponseException(400, "You must sign in");
        }
    }

}
