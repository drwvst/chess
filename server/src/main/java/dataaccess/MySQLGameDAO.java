package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLGameDAO {
    private static final MySQLGameDAO INSTANCE = new MySQLGameDAO();
    private static final Gson GSON = new GsonBuilder().create();

    private MySQLGameDAO() {}

    public static MySQLGameDAO getInstance() {
        return INSTANCE;
    }

    public List<GameData> listGames() throws DataAccessException {
        List<GameData> games = new ArrayList<>();
        String sql = "SELECT * FROM games";

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
        String sql = "INSERT INTO games (game_name, chess_game) VALUES (?, ?)";
        ChessGame newGame = new ChessGame();
        String gameStateJson = GSON.toJson(newGame);
        int gameID = -1;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, gameName);
            stmt.setString(2, gameStateJson);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    gameID = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game: " + e.getMessage());
        }
        return new GameData(gameID, null, null, gameName, newGame);
    }


    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT * FROM games WHERE game_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, gameID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractGame(rs);
                } else {
                    throw new DataAccessException("bad request");
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

    private GameData extractGame(ResultSet rs) throws SQLException {
        int gameID = rs.getInt("game_id");
        String whitePlayer = rs.getString("white_player");
        String blackPlayer = rs.getString("black_player");
        String gameName = rs.getString("game_name");
        String gameStateJson = rs.getString("chess_game");

        ChessGame gameState = GSON.fromJson(gameStateJson, ChessGame.class);
        return new GameData(gameID, whitePlayer, blackPlayer, gameName, gameState);
    }
}