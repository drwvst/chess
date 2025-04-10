package chess;

import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {

    private final ChessPosition startPosition;
    private final ChessPosition endPosition;
    private final ChessPiece.PieceType promotionPiece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessMove chessMove = (ChessMove) o;
        return Objects.equals(startPosition, chessMove.startPosition)
                && Objects.equals(endPosition, chessMove.endPosition) && promotionPiece == chessMove.promotionPiece;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPosition, endPosition, promotionPiece);
    }

    @Override
    public String toString() {
        String startAlg = positionToAlgebraic(startPosition);
        String endAlg = positionToAlgebraic(endPosition);
        String promoString = "";
        if (promotionPiece != null) {
            promoString = "=" + getPromotionChar(promotionPiece);
        }
        return startAlg + " to " + endAlg + promoString;
    }

    //Helpers
    private String positionToAlgebraic(ChessPosition pos) {
        if (pos == null) return "?";
        char file = (char) ('a' + pos.getColumn() - 1);
        return "" + file + pos.getRow();
    }

    private String getPromotionChar(ChessPiece.PieceType type) {
        return switch (type) {
            case QUEEN -> "Q";
            case ROOK -> "R";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            default -> ""; // Should not happen for promotion
        };
    }
}
