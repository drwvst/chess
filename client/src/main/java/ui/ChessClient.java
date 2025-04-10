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
            return SET_TEXT_COLOR_RED + ex.getMessage() + RESET_TEXT_COLOR;
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
            case "makemove" -> makeMove(parameters);
            case "leave" -> leaveGame();
            case "resign" -> resignGame();
            case "redraw" -> redrawBoard();
            case "highlight" -> highlightMoves(parameters);
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
            throw new ResponseException(400, "Expected: <gameID> <WHITE|BLACK>");
        }

        int gameID;
        try {
            gameID = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            throw new ResponseException(400, "Invalid Game ID format. Please enter a number.");
        }

        String colorString = params[1].toUpperCase();
        ChessGame.TeamColor requestedColor;
        try {
            requestedColor = ChessGame.TeamColor.valueOf(colorString);
        } catch (IllegalArgumentException e) {
            throw new ResponseException(400, "Invalid color. Choose WHITE or BLACK.");
        }

        server.joinGame(currentUser.authToken(), gameID, colorString);

        ws = new WebSocketFacade(serverUrl, notificationHandler);
        ws.connect(currentUser.authToken(), gameID, requestedColor);

        this.playerColor = requestedColor; // Store player color
        // Need game data for context, but rely on LOAD_GAME message for initial display

        List<GameData> games = server.listGames(currentUser.authToken());
        for(GameData game : games){
            if(game.gameID() == gameID){
                activeChessGameData = game;
//                state = State.GAMESTATE;
//                return displayBoard(activeChessGameData, playerColor) + String.format("Game %d joined successfully!", gameID);
            }
        }
        state = State.GAMESTATE;
        return String.format(SET_TEXT_COLOR_GREEN + "Joining game %d as %s. Waiting for game state...", gameID, requestedColor);

        //throw new ResponseException(400, "Failed to retrieve game after joining.");
        //printBoard(ChessGame game, PlayerColor playerColor)
    }

    public String logout(String... params) throws ResponseException{
        assertSignedInOrInGame();
        if (params.length != 0) {
            throw new ResponseException(400, "To logout, simply type 'logout'");
        }
        if (ws != null) {
            try {
                ws.close();
            } catch (ResponseException e) {
                System.out.println(SET_TEXT_COLOR_YELLOW + "Warning: Error closing WebSocket during logout: " + e.getMessage() + RESET_TEXT_COLOR);
            } finally {
                ws = null;
            }
        }

        server.logout(currentUser.authToken());
        String username = currentUser.username();
        currentUser = null;
        activeChessGameData = null;
        playerColor = null;
        state = State.SIGNEDOUT;
        return String.format(SET_TEXT_COLOR_GREEN + "You have successfully logged out, %s!\n\n%s", username, help());
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
        int gameID;
        try {
            gameID = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            throw new ResponseException(400, "Invalid Game ID format. Please enter a number.");
        }

        List<GameData> games = server.listGames(currentUser.authToken());
        for(GameData game : games){
            if(game.gameID() == gameID){
                activeChessGameData = game;
                state = State.OBSERVATION;
                ws = new WebSocketFacade(serverUrl, notificationHandler);
                ws.connect(currentUser.authToken(), gameID, null);
                this.playerColor = null;

                return ChessBoardDrawer.displayBoard(activeChessGameData.game().getBoard(), "white") +
                        String.format(SET_TEXT_COLOR_GREEN + "Observing game %d", gameID);
            }
        }
        return String.format(SET_TEXT_COLOR_RED + "Sorry, game %d does not exist!", gameID);
    }

    // IN GAME WS METHODS
    public String makeMove(String... params) throws ResponseException {
        assertInGame();
        if(state == State.OBSERVATION) throw new ResponseException(400, "Observers cannot make moves.");
        if (params.length != 1) throw new ResponseException(400, "Expected: makeMove <ex: e2e4>");

        String moveString = params[0];
        ChessMove move;
        try {
            ChessPosition start = parsePosition(moveString.substring(0, 2));
            ChessPosition end = parsePosition(moveString.substring(2, 4));
            ChessPiece.PieceType promotion = null;
            if (moveString.length() == 5) {
                promotion = parsePromotionPiece(moveString.substring(4, 5));
            }
            move = new ChessMove(start, end, promotion);
        } catch (Exception e) {
            throw new ResponseException(400, "Invalid move format. Use algebraic notation (e.g., e2e4, a7a8q).");
        }

        ws.makeMove(currentUser.authToken(), activeChessGameData.gameID(), move);
        return "";
    }

    public String leaveGame() throws ResponseException {
        assertInGameOrObserving();
        if (ws != null) {
            ws.leave(currentUser.authToken(), activeChessGameData.gameID());
        }
        String leftMessage = String.format("You have left game: %s.", activeChessGameData.gameName());
        activeChessGameData = null;
        playerColor = null;
        state = State.SIGNEDIN;
        return SET_TEXT_COLOR_YELLOW + leftMessage + "\n" + help() + RESET_TEXT_COLOR;
    }

    public String resignGame() throws ResponseException {
        assertInGame();
        if(state == State.OBSERVATION) throw new ResponseException(400, "Observers cannot resign.");

        ws.resign(currentUser.authToken(), activeChessGameData.gameID());
        return SET_TEXT_COLOR_YELLOW + "Resignation request sent. Waiting for confirmation..." + RESET_TEXT_COLOR;
    }

    public String redrawBoard() throws ResponseException {
        assertInGameOrObserving();
        if (activeChessGameData == null || activeChessGameData.game() == null) {
            return SET_TEXT_COLOR_YELLOW + "No active game state to draw." + RESET_TEXT_COLOR;
        }
        if(getPlayerColor() == null){
            return SET_TEXT_COLOR_YELLOW + "Cannot redraw board as observer" + RESET_TEXT_COLOR;
        }
        return ChessBoardDrawer.displayBoard(activeChessGameData.game().getBoard(), getPlayerColor());
    }

    public String highlightMoves(String... params) throws ResponseException {
        assertInGameOrObserving();
        if (params.length != 1) throw new ResponseException(400, "Expected: highlight <position (ex: e2)>");
        if (activeChessGameData == null || activeChessGameData.game() == null) {
            return SET_TEXT_COLOR_YELLOW + "No active game state available." + RESET_TEXT_COLOR;
        }

        ChessPosition position;
        try {
            position = parsePosition(params[0]);
        } catch (Exception e) {
            throw new ResponseException(400, "Invalid position format. Use coordinate notation (ex: 'e2').");
        }

        ChessPiece piece = activeChessGameData.game().getBoard().getPiece(position);
        if (piece == null) {
            return SET_TEXT_COLOR_YELLOW + "No piece at " + params[0] + "." + RESET_TEXT_COLOR;
        }

        if (state == State.GAMESTATE && piece.getTeamColor() != this.playerColor) {
            return SET_TEXT_COLOR_YELLOW + "You can only highlight your own pieces." + RESET_TEXT_COLOR;
        }

        java.util.Collection<ChessMove> validMoves = activeChessGameData.game().validMoves(position);
        if (validMoves == null || validMoves.isEmpty()) {
            return SET_TEXT_COLOR_YELLOW + "No valid moves for the piece at " + params[0] + "." + RESET_TEXT_COLOR;
        }

        ChessGame.TeamColor perspective = (this.playerColor != null) ? this.playerColor : ChessGame.TeamColor.WHITE;
        return ChessBoardDrawer.drawBoardWithHighlights(activeChessGameData.game().getBoard(),
                perspective, position, validMoves);
    }

    public void updateActiveGame(ChessGame updatedGame) {
        if (this.activeChessGameData != null) {
            this.activeChessGameData = new GameData(
                    this.activeChessGameData.gameID(),
                    this.activeChessGameData.whiteUsername(),
                    this.activeChessGameData.blackUsername(),
                    this.activeChessGameData.gameName(),
                    updatedGame,
                    updatedGame.isGameOver() ? GameStatus.FINISHED : GameStatus.ACTIVE
            );
            System.out.println("Client game state updated."); // Debug message
        } else {
            System.err.println("Warning: Received game update but no active game context set.");
        }
    }






    //Position Parse Methods
    private ChessPosition parsePosition(String pos) {
        int col = pos.toLowerCase().charAt(0) - 'a' + 1;
        int row = Integer.parseInt(pos.substring(1));
        if (col < 1 || col > 8 || row < 1 || row > 8) throw new IllegalArgumentException("Invalid position");
        return new ChessPosition(row, col);
    }

    private ChessPiece.PieceType parsePromotionPiece(String piece) {
        return switch (piece.toLowerCase()) {
            case "q" -> ChessPiece.PieceType.QUEEN;
            case "r" -> ChessPiece.PieceType.ROOK;
            case "b" -> ChessPiece.PieceType.BISHOP;
            case "n" -> ChessPiece.PieceType.KNIGHT;
            default -> throw new IllegalArgumentException("Invalid promotion piece");
        };
    }

    public String help() {
        if (state == State.SIGNEDOUT){
            return SET_TEXT_COLOR_BLUE + """
                    Available commands:
                        register <USERNAME> <PASSWORD> <EMAIL>   - Create an account
                        login <USERNAME> <PASSWORD>              - Log in to your account
                        quit                                     - Exit the application
                        help                                     - Show this message
                    """;
        } else if(state == State.SIGNEDIN){
            return SET_TEXT_COLOR_BLUE + """
                Available commands:
                    createGame <GAME_NAME>               - Create a new chess game
                    listGames                            - Show available games
                    joinGame <GAME_ID> <WHITE|BLACK>     - Join a game as a player
                    observeGame <GAME_ID>                - Watch a game
                    logout                               - Log out
                    quit                                 - Exit the application
                    help                                 - Show this message
                """;
        } else if(state == State.OBSERVATION || state == State.GAMESTATE){
            return SET_TEXT_COLOR_BLUE + """
                    Available commands (%s):
                       redraw                               - Redraw the chessboard
                       leave                                - Leave the current game
                       makeMove <FROM_TO[PROMO]>            - Make a move (ex: e2e4)
                       resign                               - Forfeit the match (players only)
                       highlight <POSITION>                 - Show legal moves for piece at position (ex: e2)
                       help                                 - Show this message
                    """.formatted(state == State.GAMESTATE ? "Playing" : "Observing") + RESET_TEXT_COLOR;
        }else {
            return SET_TEXT_COLOR_BLUE + """
                    - QuitGame
                    - Help
                    """;
        }
    }

    public String quit() throws ResponseException {
        quitting = true;
        if (ws != null) {
            try {
                if (state == State.GAMESTATE || state == State.OBSERVATION) {
                    ws.leave(currentUser.authToken(), activeChessGameData.gameID());
                }
                ws.close();
                ws = null;
            } catch (ResponseException | NullPointerException e) {
                System.out.println(SET_TEXT_COLOR_YELLOW + "Note: Error closing WebSocket during quit: " + e.getMessage() + RESET_TEXT_COLOR);
            }
        }
        if (state != State.SIGNEDOUT && currentUser != null) {
            try {
                server.logout(currentUser.authToken());
            } catch (ResponseException e) {
                System.out.println(SET_TEXT_COLOR_YELLOW + "Note: Error logging out during quit: " + e.getMessage() + RESET_TEXT_COLOR);
            }
        }
        System.out.println(SET_TEXT_COLOR_MAGENTA + "Exiting Chess Client. Goodbye!" + RESET_TEXT_COLOR);
        return "quit";
    }




    //Assert Methods
    private void assertSignedIn() throws ResponseException {
        if (state == State.SIGNEDOUT) {
            throw new ResponseException(400, "You must be signed in to perform this action.");
        }
    }

    private void assertSignedInOrInGame() throws ResponseException {
        if (state == State.SIGNEDOUT) {
            throw new ResponseException(400, "You must be signed in or in a game to perform this action.");
        }
    }

    private void assertInGame() throws ResponseException {
        if (state != State.GAMESTATE) {
            throw new ResponseException(400, "You must be playing in a game to perform this action.");
        }
        if (ws == null) {
            throw new ResponseException(500, "Internal Error: WebSocket not connected while in game state.");
        }
    }

    private void assertInGameOrObserving() throws ResponseException {
        if (state != State.GAMESTATE && state != State.OBSERVATION) {
            throw new ResponseException(400, "You must be playing or observing a game to perform this action.");
        }
        if (ws == null) {
            throw new ResponseException(500, "Internal Error: WebSocket not connected while in game/observe state.");
        }
    }

    public String getPlayerColor() {
        if(playerColor == ChessGame.TeamColor.WHITE){
            return "white";
        } else if (playerColor == ChessGame.TeamColor.BLACK){
            return "black";
        }
        return null;
    }

}
