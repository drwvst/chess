package ui;

import chess.*;
import model.*;
import websocket.WebSocketFacade;
import websocket.NotificationHandler;
import exception.ResponseException;
import serverfacade.ServerFacade;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static ui.EscapeSequences.*;


public class ChessClient {
    public AuthData currentUser;
    public GameData activeChessGameData;
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.SIGNEDOUT;
    private boolean quitting = false;

    private WebSocketFacade ws;
    private final NotificationHandler notificationHandler;
    private ChessGame.TeamColor playerColor = null;

    public ChessClient(String serverUrl, NotificationHandler handler) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.notificationHandler = handler;
    }

    public String eval(String input){
        if (quitting) return "quit";

        try {
            var tokens = input.toLowerCase().split(" ");
            var command = (tokens.length > 0) ? tokens[0] : "help";
            var parameters = Arrays.copyOfRange(tokens, 1, tokens.length);

            return switch (state) {
                case SIGNEDOUT -> evalSignedOut(command, parameters);
                case SIGNEDIN -> evalSignedIn(command, parameters);
                case GAMESTATE, OBSERVATION -> evalInGame(command, parameters);
            };
        } catch (ResponseException ex) {
            return SET_TEXT_COLOR_RED + "Error: " + ex.getMessage() + RESET_TEXT_COLOR;
        } catch (Exception ex) {
            return SET_TEXT_COLOR_RED + "Unexpected Error: " + ex.getMessage() + RESET_TEXT_COLOR;
        }
    }

    private String evalSignedOut(String command, String... parameters) throws ResponseException {
        return switch (command) {
            case "register" -> register(parameters);
            case "login" -> login(parameters);
            case "quit" -> quit();
            default -> help();
        };
    }

    private String evalSignedIn(String command, String... parameters) throws ResponseException {
        return switch (command) {
            case "creategame" -> createGame(parameters);
            case "listgames" -> listGames(); // No parameters needed
            case "joingame" -> joinGame(parameters);
            case "observegame" -> observeGame(parameters);
            case "logout" -> logout(); // No parameters needed
            case "quit" -> quit();
            default -> help();
        };
    }


    private String evalInGame(String command, String... parameters) throws ResponseException {
        return switch (command) {
//            case "makemove" -> makeMove(parameters);
//            case "leave" -> leaveGame();
//            case "resign" -> resignGame();
//            case "redraw" -> redrawBoard();
//            case "highlight" -> highlightMoves(parameters);
            // Add other in-game commands (show legal moves)
            default -> help();
        };
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
                activeChessGameData = game;
                state = State.GAMESTATE;
                return displayBoard(activeChessGameData, playerColor) + String.format("Game %d joined successfully!", gameID);
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

    public String displayBoard(GameData chessGame, String playerColor) {
        StringBuilder boardString = new StringBuilder();
        ChessBoard board = chessGame.game().getBoard();

        boolean isWhitePlayer = playerColor.equalsIgnoreCase("white");

        boardString.append("\n").append(SET_BG_COLOR_GREY).append("   ");

        playerColorCoordinateSet(boardString, isWhitePlayer);

        for (int row = 1; row <= 8; row++) {
            int boardRow = isWhitePlayer ? (9 - row) : row;

            boardString.append(SET_BG_COLOR_GREY).append(SET_TEXT_COLOR_BLACK).append(" ").append(boardRow).append(" ");

            for (int col = 1; col <= 8; col++) {
                int boardCol = isWhitePlayer ? col : (9 - col);

                ChessPiece piece = board.getPiece(new ChessPosition(boardRow, boardCol));

                if ((boardRow + boardCol) % 2 == 0) {
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

            boardString.append(SET_BG_COLOR_GREY)
                    .append(SET_TEXT_COLOR_BLACK).append(" ").append(boardRow).append(" ").append(RESET_BG_COLOR)
                    .append("\n");
        }

        boardString.append(SET_BG_COLOR_GREY).append("   ");

        playerColorCoordinateSet(boardString, isWhitePlayer);

        boardString.append(RESET_BG_COLOR);
        boardString.append(RESET_TEXT_COLOR);
        boardString.append("\n");
        return boardString.toString();
    }

    private void playerColorCoordinateSet(StringBuilder boardString, boolean isWhitePlayer) {
        if (isWhitePlayer) {
            for (char c = 'a'; c <= 'h'; c++) {
                boardString.append(SET_TEXT_COLOR_BLACK).append(" ").append(c).append("\u2003");
            }
        } else {
            for (char c = 'h'; c >= 'a'; c--) {
                boardString.append(SET_TEXT_COLOR_BLACK).append(" ").append(c).append("\u2003");
            }
        }
        boardString.append("   ").append(RESET_BG_COLOR).append("\n");
    }


    private String getPieceChar(ChessPiece piece, String playerColor) {
        boolean isBlackPiece = piece.getTeamColor() == ChessGame.TeamColor.BLACK;

        return switch (piece.getPieceType()) {
            case KING -> isBlackPiece ? SET_TEXT_COLOR_BLACK + BLACK_KING : SET_TEXT_COLOR_WHITE + BLACK_KING;
            case QUEEN -> isBlackPiece ? SET_TEXT_COLOR_BLACK + BLACK_QUEEN : SET_TEXT_COLOR_WHITE + BLACK_QUEEN;
            case ROOK -> isBlackPiece ? SET_TEXT_COLOR_BLACK + BLACK_ROOK : SET_TEXT_COLOR_WHITE + BLACK_ROOK;
            case BISHOP -> isBlackPiece ? SET_TEXT_COLOR_BLACK + BLACK_BISHOP : SET_TEXT_COLOR_WHITE + BLACK_BISHOP;
            case KNIGHT -> isBlackPiece ? SET_TEXT_COLOR_BLACK + BLACK_KNIGHT : SET_TEXT_COLOR_WHITE + BLACK_KNIGHT;
            case PAWN -> isBlackPiece ? SET_TEXT_COLOR_BLACK + BLACK_PAWN : SET_TEXT_COLOR_WHITE + BLACK_PAWN;
            default -> EMPTY;
        };
    }

    public String quitGame(String... params){
        activeChessGameData = null;
        state = State.SIGNEDIN;
        return String.format(SET_TEXT_COLOR_GREEN +
                "You have exited the game and returned to the main menu.\n%s",help());
    }

    public String observeGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length != 1) {
            throw new ResponseException(400, "Expected: <gameID>");
        }
        int gameID = Integer.parseInt(params[0]);

        List<GameData> games = server.listGames(currentUser.authToken());
        for(GameData game : games){
            if(game.gameID() == gameID){
                activeChessGameData = game;
                state = State.GAMESTATE;
                return displayBoard(activeChessGameData, "white") +
                        String.format(SET_TEXT_COLOR_GREEN + "Observing game %d", gameID);
            }
        }
        return String.format(SET_TEXT_COLOR_RED + "Sorry, game %d does not exist!", gameID);
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
                - ObserveGame <gameID>
                - Logout
                - Help
                - Quit
                """;
        } else if(state == State.OBSERVATION){
            return SET_TEXT_COLOR_BLUE + """
                    - QuitGame
                    - Help
                    """;
        }else { //GAMESTATE
            return SET_TEXT_COLOR_BLUE + """
                    - QuitGame
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
