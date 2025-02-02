package chess;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    /// NOTES FOR PHASE
    //tryMove and UndoMove Methods
    //Make a bunch of private variables here
    //First implement isInCheck

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

        throw new RuntimeException("Not implemented");
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
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
        //Make a printBoard function for debugging
        //instead of doing a move and undo move logic to test if the move could be made, make a temporary board called
            //testBoard where you make the move on that board and test if the king is still in check on that temp board
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
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
        return board;
    }

    public void printBoard(ChessBoard board) {
        for (int row = 8; row >= 1; row--) {
            System.out.print("|");
            for (int col = 1; col <= 8; col++) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece != null) {
                    char pieceChar = getPieceChar(piece);
                    System.out.print(pieceChar);
                } else {
                    System.out.print(" ");
                }
                System.out.print("|");
            }
            System.out.println();
        }
    }

    // Helper method to determine the correct character for a piece
    private char getPieceChar(ChessPiece piece) {
        char pieceChar;
        switch (piece.getPieceType()) {
            case KING -> pieceChar = 'k';
            case QUEEN -> pieceChar = 'q';
            case ROOK -> pieceChar = 'r';
            case BISHOP -> pieceChar = 'b';
            case KNIGHT -> pieceChar = 'n';
            case PAWN -> pieceChar = 'p';
            default -> pieceChar = ' ';
        }
        // Convert to uppercase if it's a white piece
        return piece.getTeamColor() == TeamColor.WHITE ? Character.toUpperCase(pieceChar) : pieceChar;
    }

}
