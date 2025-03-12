package dataaccess;

import model.AuthData;
import java.sql.*;
import java.util.UUID;

public class MySQLAuthDAO {
    private static final MySQLAuthDAO INSTANCE = new MySQLAuthDAO();
    private MySQLAuthDAO() {};

    public static  MySQLAuthDAO getInstance(){
        return INSTANCE;
    }

    public AuthData createAuth(String username) throws DataAccessException {
        String sqlString = "INSERT INTO auth_tokens (token, username) VALUES (?, ?)";
        String token = UUID.randomUUID().toString();
        AuthData authData = new AuthData(token, username);

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlString)){

            stmt.setString(1, authData.authToken());
            stmt.setString(2, authData.username());
            stmt.executeUpdate();
        } catch (SQLException e){
            throw new DataAccessException("Error inserting authToken: " + e.getMessage());
        }
        return authData;
    }

    public AuthData getAuthToken(String token) throws DataAccessException {
        String sqlString = "SELECT token, username FROM auth_token WHERE token = ?";

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlString)){

            stmt.setString(1, token);

            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                return new AuthData(rs.getString("token"), rs.getString("username"));
            } else {
                throw new DataAccessException("unauthorized");
            }
        } catch (SQLException e){
            throw new DataAccessException("Error retrieving authToken: " + e.getMessage());
        }
    }

    public void deleteAuth(String token) throws DataAccessException {
        String sql = "DELETE FROM auth_tokens WHERE token = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new DataAccessException("Auth token not found");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error deleting auth token: " + e.getMessage());
        }
    }
}
