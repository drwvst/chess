package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PawnMovesCalculator implements ChessPiece.PieceMovesCalculator {

    private boolean isWithinBounds(ChessPosition position) {
        return position.getRow() >= 1 && position.getRow() <= 8
                && position.getColumn() >= 1 && position.getColumn() <= 8;
    }

    @Override
    public Collection<ChessMove> validMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> validMoves = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);

        int direction = switch (myPiece.getTeamColor()) {
            case WHITE -> 1;
            case BLACK -> -1;
        };

        //Single Move
        ChessPosition forwardPosition = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
        if (isWithinBounds(forwardPosition) && board.getPiece(forwardPosition) == null) {
            //Promotion Logic
            promotionOrStep(myPosition, validMoves, myPiece, forwardPosition);

            //Double Move
            if ((myPiece.getTeamColor() == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2) ||
                    myPiece.getTeamColor() == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7) {
                ChessPosition doubleForwardPosition = new ChessPosition(myPosition.getRow() + 2 * direction,
                        myPosition.getColumn());
                if (board.getPiece(forwardPosition) == null && board.getPiece(doubleForwardPosition) == null) {
                    validMoves.add(new ChessMove(myPosition, doubleForwardPosition, null));
                }
            }
        }

        //Diagonal Capture
        ChessPosition[] diagonalCaptures = {
                new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn() + 1),
                new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn() - 1)
        };

        for (ChessPosition diagonalPosition : diagonalCaptures) {
            if (isWithinBounds(diagonalPosition)) {
                ChessPiece targetPiece = board.getPiece(diagonalPosition);
                if (targetPiece != null && targetPiece.getTeamColor() != myPiece.getTeamColor()) {
                    promotionOrStep(myPosition, validMoves, myPiece, diagonalPosition);
                }
            }
            //Move forward one square if destination is empty
            // Do inside single move
            //Move forward two squares if it is the first move and both squares are empty
            //Capture Diagonally one Square if there is an opponents piece in that square
            //Store The piece in the promotion piece arg of ChessMove when adding it to validMoves if it has reached the end of the board

        }
        return validMoves;
    }

    private void promotionOrStep(ChessPosition myPosition, List<ChessMove> validMoves, ChessPiece myPiece, ChessPosition positionType) {
        if ((myPiece.getTeamColor() == ChessGame.TeamColor.WHITE && positionType.getRow() == 8) ||
                (myPiece.getTeamColor() == ChessGame.TeamColor.BLACK && positionType.getRow() == 1)) {
            validMoves.add(new ChessMove(myPosition, positionType, ChessPiece.PieceType.QUEEN));
            validMoves.add(new ChessMove(myPosition, positionType, ChessPiece.PieceType.ROOK));
            validMoves.add(new ChessMove(myPosition, positionType, ChessPiece.PieceType.BISHOP));
            validMoves.add(new ChessMove(myPosition, positionType, ChessPiece.PieceType.KNIGHT));
        } else {
            validMoves.add(new ChessMove(myPosition, positionType, null));
        }
    }
}
