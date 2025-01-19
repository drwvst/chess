package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KingMovesCalculator implements ChessPiece.PieceMovesCalculator {
    //List of All Possible Moves
    private static final ChessPosition[] PossibleKingMoves = {
            new ChessPosition(0, 1), //Forward
            new ChessPosition(0, -1), //Backward
            new ChessPosition(1, 0), //Right
            new ChessPosition(-1, 0), //Left
            new ChessPosition(-1, -1), //Back Left
            new ChessPosition(1, -1), //Back Right
            new ChessPosition(-1, 1), //Front Left
            new ChessPosition(1, 1) //Front Right
    };
    //With the king and the knight, you only have to test if the spot you're trying to go to is on the board, and not one of your own pieces
    //put a public static function in ChessPiece that will check those conditions that you can call in here and Knight

    @Override
    public Collection<ChessMove> validMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        ChessMove newMove = new ChessMove([0][0],[0][1], null);
        moves.add(newMove);
        return moves;
    }
}
