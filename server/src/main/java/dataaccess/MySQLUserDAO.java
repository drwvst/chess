package dataaccess;

import model.UserData;

import java.sql.*;

public class MySQLUserDAO {
    private static final MySQLUserDAO INSTANCE = new MySQLUserDAO();

    private MySQLUserDAO() {};

    public static MySQLUserDAO getInstance(){
        return INSTANCE;
    }

    public void createUser(UserData user) throws DataAccessException{
        String checkUserSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        String insertUserSql = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkUserSql)) {
                checkStmt.setString(1, user.username());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new DataAccessException("Username already taken");
                    }
                }
            }

            //Inserts the new user if it doesn't already exist
            try (PreparedStatement insertStmt = conn.prepareStatement(insertUserSql)) {
                insertStmt.setString(1, user.username());
                insertStmt.setString(2, user.password());
                insertStmt.setString(3, user.email());
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting user: " + e.getMessage());
        }
    }

    public UserData getUser(String username) throws DataAccessException{
        String sqlString = "SELECT username, password_hash, email FROM users WHERE username = ?";
        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlString)){

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                return new UserData(rs.getString("username"), rs.getString("password_hash"),
                        rs.getString("email"));
            } else {
                throw new DataAccessException("unauthorized"); // User DNE so no access
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving user: " + e.getMessage());
        }
    }
}
