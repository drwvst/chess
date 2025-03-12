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
        String sqlString = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";

        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlString)){

            stmt.setString(1, user.username());
            stmt.setString(2, user.password());
            stmt.setString(3, user.email()); //MUST HASH PASSWORD LATER
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                throw new DataAccessException("Username already taken");
            }
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
