package ui;

import chess.*;
import model.GameData;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.EMPTY;

class ChessBoardDrawer {
    public static String displayBoard(ChessBoard chessBoard, String playerColor) {
        StringBuilder boardString = new StringBuilder();
        ChessBoard board = chessBoard;

        boolean isWhitePlayer = playerColor.equalsIgnoreCase("white");

        boardString.append("\n").append(SET_BG_COLOR_GREY).append("   ");

        playerColorCoordinateSet(boardString, isWhitePlayer);

        for (int row = 1; row <= 8; row++) {
            int boardRow = isWhitePlayer ? (9 - row) : row;

            boardString.append(SET_BG_COLOR_GREY).append(SET_TEXT_COLOR_BLACK).append(" ").append(boardRow).append(" ");

            for (int col = 1; col <= 8; col++) {
                int boardCol = isWhitePlayer ? col : (9 - col);

                ChessPiece piece = board.getPiece(new ChessPosition(boardRow, boardCol));

                if ((boardRow + boardCol) % 2 == 0) {
                    boardString.append(SET_BG_COLOR_DARK_BROWN);
                } else {
                    boardString.append(SET_BG_COLOR_LIGHT_BROWN);
                }

                if (piece != null) {
                    String pieceChar = getPieceChar(piece, playerColor);
                    boardString.append(pieceChar);
                } else {
                    boardString.append(EMPTY);
                }
                boardString.append(RESET_BG_COLOR);
            }

            boardString.append(SET_BG_COLOR_GREY)
                    .append(SET_TEXT_COLOR_BLACK).append(" ").append(boardRow).append(" ").append(RESET_BG_COLOR)
                    .append("\n");
        }

        boardString.append(SET_BG_COLOR_GREY).append("   ");

        playerColorCoordinateSet(boardString, isWhitePlayer);

        boardString.append(RESET_BG_COLOR);
        boardString.append(RESET_TEXT_COLOR);
        boardString.append("\n");
        return boardString.toString();
    }

    public static String drawBoardWithHighlights(ChessBoard board, ChessGame.TeamColor perspective, java.util.Collection<ChessMove> validMoves) {
        // TODO: Implement board drawing with highlighted valid move squares
        StringBuilder highlights = new StringBuilder(" Valid Moves:");
        for (ChessMove move : validMoves) {
            highlights.append(" ").append(move.getEndPosition());
        }
        return "\n --- Chess Board Placeholder (Perspective: " + perspective + ") --- \n" + board.toString() + highlights.toString() + "\n --- End Board --- \n";
    }


    //helpers
    private static void playerColorCoordinateSet(StringBuilder boardString, boolean isWhitePlayer) {
        if (isWhitePlayer) {
            for (char c = 'a'; c <= 'h'; c++) {
                boardString.append(SET_TEXT_COLOR_BLACK).append(" ").append(c).append("\u2003");
            }
        } else {
            for (char c = 'h'; c >= 'a'; c--) {
                boardString.append(SET_TEXT_COLOR_BLACK).append(" ").append(c).append("\u2003");
            }
        }
        boardString.append("   ").append(RESET_BG_COLOR).append("\n");
    }

    private static String getPieceChar(ChessPiece piece, String playerColor) {
        boolean isBlackPiece = piece.getTeamColor() == ChessGame.TeamColor.BLACK;

        return switch (piece.getPieceType()) {
            case KING -> isBlackPiece ? SET_TEXT_COLOR_BLACK + BLACK_KING : SET_TEXT_COLOR_WHITE + BLACK_KING;
            case QUEEN -> isBlackPiece ? SET_TEXT_COLOR_BLACK + BLACK_QUEEN : SET_TEXT_COLOR_WHITE + BLACK_QUEEN;
            case ROOK -> isBlackPiece ? SET_TEXT_COLOR_BLACK + BLACK_ROOK : SET_TEXT_COLOR_WHITE + BLACK_ROOK;
            case BISHOP -> isBlackPiece ? SET_TEXT_COLOR_BLACK + BLACK_BISHOP : SET_TEXT_COLOR_WHITE + BLACK_BISHOP;
            case KNIGHT -> isBlackPiece ? SET_TEXT_COLOR_BLACK + BLACK_KNIGHT : SET_TEXT_COLOR_WHITE + BLACK_KNIGHT;
            case PAWN -> isBlackPiece ? SET_TEXT_COLOR_BLACK + BLACK_PAWN : SET_TEXT_COLOR_WHITE + BLACK_PAWN;
            default -> EMPTY;
        };
    }
}