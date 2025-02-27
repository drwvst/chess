package chess;

import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    /// NOTES FOR PHASE

    //Keep track of who's turn
    /// WHEN FIGURING OUT A METHOD, LOOK AT THE PARAMETERS AND MAKE A NOTES LIST OF THE METHODS OF THE PARAMETER TYPES
    private TeamColor currentTurn;
    private ChessBoard board = new ChessBoard();

    public ChessGame() {
        board.resetBoard();
        this.currentTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */

    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        //Checks Check, Checkmate, Stalemate
        //needs to filter out the moves that it cant move to
        ChessPiece piece = board.getPiece(startPosition);
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> legalMoves = new ArrayList<>();

        ChessBoard testBoard = null;
        for(ChessMove move : moves) {
            testBoard = new ChessBoard(board);
            testBoard.addPiece(move.getEndPosition(), piece);
            testBoard.addPiece(move.getStartPosition(), null);

            if(!isInCheck(piece.getTeamColor(), testBoard)) {
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        //if the move is not in valid moves
            //throw invalid Move exception
        if(board.getPiece(move.getStartPosition()) != null) {
            ChessPiece piece = board.getPiece(move.getStartPosition());
            ChessGame.TeamColor team = piece.getTeamColor();
            if(getTeamTurn() != team || !validMoves(move.getStartPosition()).contains(move)){
                throw new InvalidMoveException();
            }
            if(move.getPromotionPiece() != null) {
                ChessPiece promotionPiece = new ChessPiece(team, move.getPromotionPiece());
                board.addPiece(move.getEndPosition(), promotionPiece);
            } else {
                board.addPiece(move.getEndPosition(), piece);
            }
            board.addPiece(move.getStartPosition(), null);
            if(team == TeamColor.WHITE){
                setTeamTurn(TeamColor.BLACK);
            } else {
                setTeamTurn(TeamColor.WHITE);
            }
        } else {
            throw new InvalidMoveException();
        }

        //Updates the board to have the right piece in the right place based on the selected move
        //Updates Pieces for pawn promotions

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        //finding the kings Position
        //printBoard(board);
        ChessPosition kingPosition = findKing(teamColor);

        //Check if the king is Attacked
        //iterate through all opponent pieces
        //For each opponent piece, get all of its valid moves using pieceMoves(board, myPosition)
        //If any of these moves can reach the kings position, the king is in check
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() != teamColor){ //opponent pieces
                    for(ChessMove move : piece.pieceMoves(board, position)){
                        if (move.getEndPosition().equals(kingPosition)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    //Override for Checkmate Testing
    public boolean isInCheck(TeamColor teamColor, ChessBoard testBoard) {
        //finding the kings Position
        ChessPosition kingPosition = null;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = testBoard.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor
                        && piece.getPieceType() == ChessPiece.PieceType.KING){
                    kingPosition = position;
                }
            }
        }

        //Check if the king is Attacked
        //iterate through all opponent pieces
        //For each opponent piece, get all of its valid moves using pieceMoves(board, myPosition)
        //If any of these moves can reach the kings position, the king is in check
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = testBoard.getPiece(position);

                if (piece != null && piece.getTeamColor() != teamColor){ //opponent pieces
                    for(ChessMove move : piece.pieceMoves(testBoard, position)){
                        if (move.getEndPosition().equals(kingPosition)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */

    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        ChessPosition kingPosition = findKing(teamColor);
        ChessPiece kingPiece = board.getPiece(kingPosition);

        if (canKingEscape(kingPiece, kingPosition, teamColor)) {return false;}

        List<ChessMove> threatPieces = findThreatPieces(teamColor, kingPosition);

        return !canBlockOrCaptureThreat(threatPieces, teamColor);
    }

    private boolean canKingEscape(ChessPiece kingPiece, ChessPosition kingPosition, TeamColor teamColor) {
        for (ChessMove move : kingPiece.pieceMoves(board, kingPosition)) {
            ChessBoard testBoard = new ChessBoard(board);
            performMove(testBoard, move, teamColor);
            if (!isInCheck(teamColor, testBoard)) {
                return true;
            }
        }
        return false;
    }

    private List<ChessMove> findThreatPieces(TeamColor teamColor, ChessPosition kingPosition) {
        List<ChessMove> threatPieces = new ArrayList<>();

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() != teamColor) {
                    for (ChessMove validMove : piece.pieceMoves(board, position)) {
                        if (validMove.getEndPosition().equals(kingPosition)) {
                            threatPieces.add(validMove);
                        }
                    }
                }
            }
        }
        return threatPieces;
    }

    private boolean canBlockOrCaptureThreat(List<ChessMove> threatPieces, TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    for (ChessMove validMove : piece.pieceMoves(board, position)) {
                        ChessBoard testBoard = new ChessBoard(board);
                        performMove(testBoard, validMove, teamColor);

                        if (!isInCheck(teamColor, testBoard)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void performMove(ChessBoard board, ChessMove move, TeamColor teamColor) {
        ChessPosition newPosition = move.getEndPosition();
        ChessPiece piece = board.getPiece(move.getStartPosition());

        board.addPiece(newPosition, piece);
        board.addPiece(move.getStartPosition(), null);
    }



    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    for (ChessMove move : piece.pieceMoves(board, position)) {
                        ChessBoard testBoard = new ChessBoard(board);
                        testBoard.addPiece(move.getEndPosition(), piece);
                        testBoard.addPiece(position, null);

                        if (!isInCheck(teamColor, testBoard)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
       this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        setBoard(board);
        //printBoard(board);
        return board;
    }

    public ChessPosition findKing(TeamColor teamColor) {
        //finding the kings Position
        //printBoard(board);
        ChessPosition kingPosition = null;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor
                        && piece.getPieceType() == ChessPiece.PieceType.KING){
                    kingPosition = position;
                }
            }
        }
        return kingPosition;
    }
}
