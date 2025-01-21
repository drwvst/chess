package chess;

import java.util.Collection;
import java.util.List;

import static chess.ChessPiece.QBRValidMoves;


public class QueenMovesCalculator implements ChessPiece.PieceMovesCalculator {
    //List of All Possible Moves
    private static final ChessPosition[] PossibleQueenMoves = {
            new ChessPosition(1, 0), //Forward
            new ChessPosition(-1, 0), //Backward
            new ChessPosition(0, 1), //Right
            new ChessPosition(0, -1), //Left
            new ChessPosition(1, 1), // Forward Right
            new ChessPosition(1, -1), // Forward Left
            new ChessPosition(-1, 1), // Down Right
            new ChessPosition(-1, -1), // Down Left
    };
    //With the king and the knight, you only have to test if the spot you're trying to go to is on the board, and not one of your own pieces
    //put a public static function in ChessPiece that will check those conditions that you can call in here and Knight

    @Override
    public Collection<ChessMove> validMoves(ChessBoard board, ChessPosition myPosition) {
        return QBRValidMoves(board, myPosition, PossibleQueenMoves);
    }
}
