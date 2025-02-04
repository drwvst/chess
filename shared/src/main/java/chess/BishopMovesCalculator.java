package chess;

import java.util.Collection;

import static chess.ChessPiece.qbrValidMoves;

public class BishopMovesCalculator implements ChessPiece.PieceMovesCalculator {
    //List of All Possible Moves
    private static final ChessPosition[] POSSIBLE_BISHOP_MOVES = {
            new ChessPosition(1, 1), // Forward Right
            new ChessPosition(1, -1), // Forward Left
            new ChessPosition(-1, 1), // Down Right
            new ChessPosition(-1, -1), // Down Left
    };
    //With the king and the knight, you only have to test if the spot you're trying to go to is on the board, and not one of your own pieces
    //put a public static function in ChessPiece that will check those conditions that you can call in here and Knight

    @Override
    public Collection<ChessMove> validMoves(ChessBoard board, ChessPosition myPosition) {
        //What to Check For with the King
        //Is the Potential Move in bounds
        //Is there one of its own pieces there
        return qbrValidMoves(board, myPosition, POSSIBLE_BISHOP_MOVES);
    }
}
