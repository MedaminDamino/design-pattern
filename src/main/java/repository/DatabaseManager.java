package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseManager – Singleton.
 * Single Responsibility: manages the MySQL connection and schema creation.
 */
public class DatabaseManager {
    private static final String DB_URL_BASE = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "drawing_app";
    private static final String DB_PARAMS = "?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "";

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Connect to base to create database
            try (Connection baseConn = DriverManager.getConnection(DB_URL_BASE + DB_PARAMS, USER, PASS);
                 Statement st = baseConn.createStatement()) {
                st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            }

            // Connect to actual database
            connection = DriverManager.getConnection(DB_URL_BASE + DB_NAME + DB_PARAMS, USER, PASS);
            createTables();
        } catch (Exception e) {
            throw new RuntimeException("Cannot connect to MySQL database: " + e.getMessage(), e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    public Connection getConnection() { return connection; }

    private void createTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS drawings (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    created_at VARCHAR(255) NOT NULL
                )""");
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS shapes (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    drawing_id INT NOT NULL,
                    type VARCHAR(50) NOT NULL,
                    start_x DOUBLE, start_y DOUBLE,
                    end_x DOUBLE, end_y DOUBLE,
                    width DOUBLE, height DOUBLE, radius DOUBLE,
                    stroke_color VARCHAR(50), fill_color VARCHAR(50),
                    FOREIGN KEY(drawing_id) REFERENCES drawings(id) ON DELETE CASCADE
                )""");
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS graph_nodes (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    drawing_id INT NOT NULL,
                    node_id INT NOT NULL,
                    x DOUBLE, y DOUBLE,
                    stroke_color VARCHAR(50), fill_color VARCHAR(50),
                    FOREIGN KEY(drawing_id) REFERENCES drawings(id) ON DELETE CASCADE
                )""");
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS graph_edges (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    drawing_id INT NOT NULL,
                    start_x DOUBLE, start_y DOUBLE,
                    end_x DOUBLE, end_y DOUBLE,
                    weight DOUBLE,
                    stroke_color VARCHAR(50), fill_color VARCHAR(50),
                    FOREIGN KEY(drawing_id) REFERENCES drawings(id) ON DELETE CASCADE
                )""");
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS logs (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    action VARCHAR(100) NOT NULL,
                    details TEXT,
                    created_at VARCHAR(255) NOT NULL
                )""");
        }
    }
}
