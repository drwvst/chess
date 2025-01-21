package chess;

import java.awt.*;
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
        //What to Check For with the King
        //Is the Potential Move in bounds
        //Is there one of its own pieces there

        List<ChessMove> validMoves = new ArrayList<>(); //List of Valid Moves
        ChessPiece myPiece = board.getPiece(myPosition);

        for(ChessPosition moveOffset : PossibleKingMoves ) {
            int testRow = myPosition.getRow() + moveOffset.getRow();
            int testCol = myPosition.getColumn() + moveOffset.getColumn();

            //If In Bounds of the Board
            if(testRow >= 1 && testRow <= 8 && testCol >= 1 && testCol <= 8) {
                ChessPosition newPosition = new ChessPosition(testRow, testCol);
                //Save next space to get knowledge about it
                ChessPiece targetPiece = board.getPiece(newPosition);

                //check if target position is null or opponents piece
                if(targetPiece == null || targetPiece.getTeamColor() != myPiece.getTeamColor()) {
                    ChessMove newMove = new ChessMove(myPosition, newPosition, null);
                    validMoves.add(newMove);
                }
            }


        }
        return validMoves;
    }
}
