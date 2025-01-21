package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.EnumMap;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;
                                //Key      //Value                                                 //The Key Type for the map
    private static final EnumMap<PieceType,PieceMovesCalculator> MOVES_CALCULATORS = new EnumMap<>(PieceType.class);

    static {
        MOVES_CALCULATORS.put(PieceType.KING, new KingMovesCalculator());
        MOVES_CALCULATORS.put(PieceType.QUEEN, new QueenMovesCalculator());
        MOVES_CALCULATORS.put(PieceType.BISHOP, new BishopMovesCalculator());
        MOVES_CALCULATORS.put(PieceType.KNIGHT, new KnightMovesCalculator());
        MOVES_CALCULATORS.put(PieceType.ROOK, new RookMovesCalculator());
        MOVES_CALCULATORS.put(PieceType.PAWN, new PawnMovesCalculator());
    }

    //Constructor
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */



    //Draw out Chess Board to get Logic Right
    public interface PieceMovesCalculator {
        //Listed out Methods for implement Classes to override
        //Here for example, the only method that is over-ridden in each calculator is valid Moves
        Collection<ChessMove> validMoves(ChessBoard board, ChessPosition myPosition);
    }


           //Return Type         //Name of Method
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition)  {
        PieceMovesCalculator calculator = MOVES_CALCULATORS.get(type);

        if(calculator != null) {
            return calculator.validMoves(board, myPosition);
        }
        return new ArrayList<>();
    }

}
