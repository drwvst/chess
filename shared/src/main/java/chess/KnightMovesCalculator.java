package chess;

import java.util.Collection;
import java.util.List;

public class KnightMovesCalculator implements ChessPiece.PieceMovesCalculator {
    @Override
    public Collection<ChessMove> validMoves(ChessBoard board, ChessPosition myPosition) {
        return List.of();
    }
}
