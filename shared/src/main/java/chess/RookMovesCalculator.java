package chess;

import java.util.Collection;

import static chess.ChessPiece.qbrValidMoves;

public class RookMovesCalculator implements ChessPiece.PieceMovesCalculator {
    //List of All Possible Moves
    private static final ChessPosition[] POSSIBLE_ROOK_MOVES = {
            new ChessPosition(1, 0), //Forward
            new ChessPosition(-1, 0), //Backward
            new ChessPosition(0, 1), //Right
            new ChessPosition(0, -1), //Left
    };
    //With the king and the knight, you only have to test if the spot you're trying to go to is on the board, and not one of your own pieces
    //put a public static function in ChessPiece that will check those conditions that you can call in here and Knight

    @Override
    public Collection<ChessMove> validMoves(ChessBoard board, ChessPosition myPosition) {
        //What to Check For with the King
        //Is the Potential Move in bounds
        //Is there one of its own pieces there
        return qbrValidMoves(board, myPosition, POSSIBLE_ROOK_MOVES);
    }
}
