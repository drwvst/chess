package chess;

import java.util.Collection;
import java.util.List;

public class KingMovesCalculator implements ChessPiece.PieceMovesCalculator {
    //List of All Possible Moves
    ChessPosition[] PossibleKingMoves = {
            new ChessPosition(0, 0)

    };
    //With the king and the knight, you only have to test if the spot you're trying to go to is on the board, and not one of your own pieces
    //put a public static function in ChessPiece that will check those conditions that you can call in here and Knight

    @Override
    public Collection<ChessMove> validMoves(ChessBoard board, ChessPosition myPosition) {
        return List.of();
    }
}
