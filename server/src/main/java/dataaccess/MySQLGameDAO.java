package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.GameData;
import model.GameStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLGameDAO {
    private static final MySQLGameDAO INSTANCE = new MySQLGameDAO();
    private static final Gson GSON = new GsonBuilder().create();

    public MySQLGameDAO() {}

    public static MySQLGameDAO getInstance() {
        return INSTANCE;
    }

    public List<GameData> listGames() throws DataAccessException {
        List<GameData> games = new ArrayList<>();
        String sql = "SELECT game_id, white_player, black_player, game_name, chess_game, status FROM games";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                games.add(extractGame(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game list: " + e.getMessage());
        }
        return games;
    }

    public GameData createGame(String gameName) throws DataAccessException {
        String checkSql = "SELECT COUNT(*) FROM games WHERE game_name = ?";
        String insertSql = "INSERT INTO games (game_name, chess_game, status) VALUES (?, ?, ?)"; // <-- Added status
        ChessGame newGame = new ChessGame();
        newGame.getBoard().resetBoard();
        String gameStateJson = GSON.toJson(newGame);
        int gameID = -1;
        GameStatus initialStatus = GameStatus.ACTIVE;


        try (Connection conn = DatabaseManager.getConnection()) {
            //Checking if game name already exists
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, gameName);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new DataAccessException("Game with name '" + gameName + "' already exists.");
                    }
                }
            }


            //Insert new game if name not already taken
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, gameName);
                insertStmt.setString(2, gameStateJson);
                insertStmt.setString(3, initialStatus.name());
                insertStmt.executeUpdate();

                try (ResultSet rs = insertStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        gameID = rs.getInt(1);
                    } else {
                        throw new SQLException("Creating game failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game: " + e.getMessage());
        }

        return new GameData(gameID, null, null, gameName, newGame, initialStatus);
    }



    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT game_id, white_player, black_player, game_name, chess_game," +
                " status FROM games WHERE game_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, gameID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractGame(rs);
                } else {
                    throw new DataAccessException("Error: Game with ID " + gameID + " not found.");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game: " + e.getMessage());
        }
    }

    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE games SET white_player = ?, black_player = ?, chess_game = ? WHERE game_id = ?";
        String gameStateJson = GSON.toJson(game.game());

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, gameStateJson);
            stmt.setInt(4, game.gameID());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("Game update failed: No game found with ID " + game.gameID());
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage());
        }
    }

    public void updateGameStatus(int gameID, GameStatus status) throws DataAccessException {
        String sql = "UPDATE games SET status = ? WHERE game_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, gameID);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("Game status update failed: No game found with ID " + gameID);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game status: " + e.getMessage());
        }
    }

    private GameData extractGame(ResultSet rs) throws SQLException {
        int gameID = rs.getInt("game_id");
        String whitePlayer = rs.getString("white_player");
        String blackPlayer = rs.getString("black_player");
        String gameName = rs.getString("game_name");
        String gameStateJson = rs.getString("chess_game");
        String statusString = rs.getString("status");

        ChessGame gameState = GSON.fromJson(gameStateJson, ChessGame.class);
        GameStatus status = GameStatus.valueOf(statusString.toUpperCase());

        return new GameData(gameID, whitePlayer, blackPlayer, gameName, gameState, status);
    }
}