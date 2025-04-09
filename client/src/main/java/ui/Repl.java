package ui;

import websocket.NotificationHandler;
import com.google.gson.Gson;
import websocket.messages.*;

import java.util.Scanner;
import static ui.EscapeSequences.*;

public class Repl implements NotificationHandler {
    private final ChessClient client;
    private final Gson gson = new Gson();

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl);
    }

    public void run() {
        System.out.println(SET_TEXT_COLOR_WHITE + WHITE_KING + "Welcome to Chess. Type Help to get started." + WHITE_KING);
        System.out.print(SET_TEXT_COLOR_BLUE + client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while(!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try{
                result = client.eval(line);
                System.out.println(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e){
                var msg = e.getMessage();
                System.out.print(msg);
            }
        }
        System.out.println();
        System.out.println(SET_TEXT_COLOR_WHITE + "Exiting Chess client.");
        System.out.println();
    }

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME:
                try {
                    if (message instanceof LoadGameMessage) {
                        LoadGameMessage loadData = (LoadGameMessage) message;
                        chess.ChessGame game = loadData.getGame();
                        System.out.println("\n" + SET_TEXT_COLOR_YELLOW + "Game state updated/loaded." + RESET_TEXT_COLOR);
                        System.out.println(ChessBoardDrawer.drawBoard(game.getBoard(), client.getPlayerColor()));
                    } else {
                        System.out.println("\n" + SET_TEXT_COLOR_RED + "Received LOAD_GAME message but in unexpected format.");
                    }

                } catch (Exception e) {
                    System.out.println("\n" + SET_TEXT_COLOR_RED + "Error processing LOAD_GAME: " + e.getMessage());
                    // e.printStackTrace();
                }
                break;

            case ERROR:
                try {
                    if (message instanceof ErrorMessage) {
                        ErrorMessage errorData = (ErrorMessage) message;
                        System.out.println("\n" + SET_TEXT_COLOR_RED + "Server Error: " + errorData.getErrorMessage() + RESET_TEXT_COLOR);
                    } else {
                        System.out.println("\n" + SET_TEXT_COLOR_RED + "Received ERROR message but in unexpected format.");
                    }
                } catch (Exception e) {
                    System.out.println("\n" + SET_TEXT_COLOR_RED + "Error processing ERROR message: " + e.getMessage());
                }
                break;

            case NOTIFICATION:
                // Notifications use the base ServerMessage 'message' field
                if (message.getMessage() != null) { // Need getMessage() in ServerMessage
                    System.out.println("\n" + SET_TEXT_COLOR_GREEN + "Notification: " + message.getMessage() + RESET_TEXT_COLOR);
                } else {
                    System.out.println("\n" + SET_TEXT_COLOR_YELLOW + "Received empty notification." + RESET_TEXT_COLOR);
                }
                break; // Added break

            default:
                System.out.println("\n" + SET_TEXT_COLOR_YELLOW + "Unknown message type received: " + message.getServerMessageType() + RESET_TEXT_COLOR);
                break;
        }
        printPrompt();
    }


    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR +  ">>> " + SET_TEXT_COLOR_GREEN);
    }

}
