package repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * LogRepository – DAO / Repository Pattern.
 * Single Responsibility: CRUD for the logs table only.
 */
public class LogRepository implements ILogRepository {
    private final Connection conn;

    public LogRepository() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public void save(String action, String details) {
        String sql = "INSERT INTO logs(action, details, created_at) VALUES(?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, action);
            ps.setString(2, details);
            ps.setString(3, LocalDateTime.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("LogRepository.save error: " + e.getMessage());
        }
    }

    public List<String> findAll() throws SQLException {
        List<String> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT created_at, action, details FROM logs ORDER BY id DESC LIMIT 100")) {
            while (rs.next())
                list.add("[" + rs.getString("created_at").substring(0, 19) +
                         "] " + rs.getString("action") + " – " + rs.getString("details"));
        }
        return list;
    }
}
