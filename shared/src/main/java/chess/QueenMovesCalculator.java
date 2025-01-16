package chess;

import java.util.Collection;
import java.util.List;


public class QueenMovesCalculator implements ChessPiece.PieceMovesCalculator {
    //Queen, Bishop, and Rook use the same concept of distance moves for possible moves
    //Distance moves will need to


    @Override
    public Collection<ChessMove> validMoves(ChessBoard board, ChessPosition myPosition) {
        return List.of();
    }
}
