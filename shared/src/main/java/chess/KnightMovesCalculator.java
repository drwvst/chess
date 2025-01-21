package chess;

import java.util.Collection;

import static chess.ChessPiece.KKValidMoves;

public class KnightMovesCalculator implements ChessPiece.PieceMovesCalculator {
    //List of All Possible Moves
    private static final ChessPosition[] PossibleKnightMoves = {
            new ChessPosition(2, 1), // Forward right Top
            new ChessPosition(1, 2), // Forward right Bottom
            new ChessPosition(-1, 2), // Back right Top
            new ChessPosition(-2, 1), // Back right Bottom
            new ChessPosition(-2, -1), // Back left Bottom
            new ChessPosition(-1, -2), // Back left Top
            new ChessPosition(1, -2), // Forward left bottom
            new ChessPosition(2, -1) // Forward left top
    };
    //With the king and the knight, you only have to test if the spot you're trying to go to is on the board, and not one of your own pieces
    //put a public static function in ChessPiece that will check those conditions that you can call in here and Knight

    @Override
    public Collection<ChessMove> validMoves(ChessBoard board, ChessPosition myPosition) {
        //What to Check For with the Knight
        //Is the Potential Move in bounds
        //Is there one of its own pieces there

        return KKValidMoves(board, myPosition, PossibleKnightMoves);
    }
}
