package repository;

import model.Drawing;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DrawingRepository – DAO / Repository Pattern.
 * Single Responsibility: CRUD for the drawings table only.
 */
public class DrawingRepository implements IDrawingRepository {
    private final Connection conn;

    public DrawingRepository() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public Drawing save(Drawing drawing) throws SQLException {
        String sql = "INSERT INTO drawings(name, created_at) VALUES(?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, drawing.getName());
            ps.setString(2, LocalDateTime.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) drawing.setId(keys.getInt(1));
            }
        }
        return drawing;
    }

    public List<Drawing> findAll() throws SQLException {
        List<Drawing> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name, created_at FROM drawings ORDER BY id DESC")) {
            while (rs.next()) {
                Drawing d = new Drawing(rs.getString("name"));
                d.setId(rs.getInt("id"));
                d.setCreatedAt(rs.getString("created_at"));
                list.add(d);
            }
        }
        return list;
    }

    public void deleteById(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM drawings WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
