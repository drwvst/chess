package dataaccess;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static final String DATABASE_NAME;
    private static final String USER;
    private static final String PASSWORD;
    private static final String CONNECTION_URL;

    /*
     * Load the database information for the db.properties file.
     */
    static {
        try {
            try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
                if (propStream == null) {
                    throw new Exception("Unable to load db.properties");
                }
                Properties props = new Properties();
                props.load(propStream);
                DATABASE_NAME = props.getProperty("db.name");
                USER = props.getProperty("db.user");
                PASSWORD = props.getProperty("db.password");

                var host = props.getProperty("db.host");
                var port = Integer.parseInt(props.getProperty("db.port"));
                CONNECTION_URL = String.format("jdbc:mysql://%s:%d", host, port);
            }
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties. " + ex.getMessage());
        }
    }

    /**
     * Creates the database if it does not already exist.
     */
    static void createDatabase() throws DataAccessException {
        try {
            var statement = "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME;
            var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DbInfo.getConnection(databaseName)) {
     * // execute SQL statements.
     * }
     * </code>
     */
    static Connection getConnection() throws DataAccessException {
        try {
            var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
            conn.setCatalog(DATABASE_NAME);
            return conn;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public static void initializeDatabase() {
        try {
            //clear();
            createDatabase();
            createTables();
        } catch (DataAccessException e) {
            throw new RuntimeException("Database initialization failed: " + e.getMessage(), e);
        }
    }

    public static void createTables(){
        try(Connection conn = getConnection();
            Statement stmt = conn.createStatement()){

            //Users Table initialization
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(50) PRIMARY KEY,
                    password_hash VARCHAR(255) NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL
                )
            """);

            //AuthTokens table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS auth_tokens(
                    token VARCHAR(100) PRIMARY KEY,
                    username VARCHAR(50) NOT NULL,
                    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
                )
            """);

            //Games Table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS games (
                    game_id INT AUTO_INCREMENT PRIMARY KEY,
                    white_player VARCHAR(50),
                    black_player VARCHAR(50),
                    game_name VARCHAR(50),
                    chess_game TEXT NOT NULL,
                    FOREIGN KEY (white_player) REFERENCES users(username),
                    FOREIGN KEY (black_player) REFERENCES users(username)
                )
            """);

        } catch (SQLException | DataAccessException e){
            throw new RuntimeException("Error creating tables: " + e.getMessage());
        }
    }

    public static void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Disable foreign key checks to avoid constraint violations
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");

            // Clear tables in the correct order
            stmt.executeUpdate("DELETE FROM auth_tokens");
            stmt.executeUpdate("DELETE FROM games");
            stmt.executeUpdate("DELETE FROM users");

            // Reset auto-increment values if needed
            stmt.executeUpdate("ALTER TABLE users AUTO_INCREMENT = 1");
            stmt.executeUpdate("ALTER TABLE games AUTO_INCREMENT = 1");

            // Re-enable foreign key checks
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");

        } catch (SQLException e) {
            throw new DataAccessException("Error clearing tables: " + e.getMessage());
        }
    }


}
