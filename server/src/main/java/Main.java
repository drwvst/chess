import chess.*;
import server.Server;
//import org.eclipse.jetty.server.Server;


public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);

        Server server = new Server();
        int port = 8080;
        System.out.println("Starting server on port: " + port);
        int actualPort = server.run(port);

        if (actualPort == port) {
            System.out.println("Server successfully started on port: " + actualPort);
        } else {
            System.err.println("Server started on a different port: " + actualPort);
        }
    }
}