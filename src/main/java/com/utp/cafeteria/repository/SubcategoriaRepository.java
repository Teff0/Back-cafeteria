package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.Subcategoria;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acceso a la tabla {@code subcategorias} usando JDBC puro.
 */
@Repository
public class SubcategoriaRepository extends JdbcDao {

    public SubcategoriaRepository(DataSource dataSource) {
        super(dataSource);
    }

    private static final String COLS = "id, categoria_id, nombre, descripcion, created_at, updated_at";

    public List<Subcategoria> findAll() {
        return query("SELECT " + COLS + " FROM subcategorias ORDER BY nombre", ps -> {});
    }

    public List<Subcategoria> findByCategoriaId(UUID categoriaId) {
        return query("SELECT " + COLS + " FROM subcategorias WHERE categoria_id = ? ORDER BY nombre",
                ps -> ps.setString(1, str(categoriaId)));
    }

    public Optional<Subcategoria> findById(UUID id) {
        List<Subcategoria> r = query("SELECT " + COLS + " FROM subcategorias WHERE id = ?",
                ps -> ps.setString(1, str(id)));
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    public boolean existsById(UUID id) {
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM subcategorias WHERE id = ?")) {
                ps.setString(1, str(id));
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar subcategoria", e);
        } finally {
            release(con);
        }
    }

    public void deleteById(UUID id) {
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM subcategorias WHERE id = ?")) {
                ps.setString(1, str(id));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar subcategoria", e);
        } finally {
            release(con);
        }
    }

    public Subcategoria save(Subcategoria s) {
        Connection con = null;
        try {
            con = getConnection();
            if (s.getId() == null) {
                s.setId(UUID.randomUUID());
                s.setCreatedAt(LocalDateTime.now());
                s.setUpdatedAt(LocalDateTime.now());
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO subcategorias (" + COLS + ") VALUES (?,?,?,?,?,?)")) {
                    ps.setString(1, str(s.getId()));
                    ps.setString(2, str(s.getCategoriaId()));
                    ps.setString(3, s.getNombre());
                    ps.setString(4, s.getDescripcion());
                    ps.setTimestamp(5, toTs(s.getCreatedAt()));
                    ps.setTimestamp(6, toTs(s.getUpdatedAt()));
                    ps.executeUpdate();
                }
            } else {
                s.setUpdatedAt(LocalDateTime.now());
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE subcategorias SET categoria_id=?, nombre=?, descripcion=?, updated_at=? WHERE id=?")) {
                    ps.setString(1, str(s.getCategoriaId()));
                    ps.setString(2, s.getNombre());
                    ps.setString(3, s.getDescripcion());
                    ps.setTimestamp(4, toTs(s.getUpdatedAt()));
                    ps.setString(5, str(s.getId()));
                    ps.executeUpdate();
                }
            }
            return s;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar subcategoria", e);
        } finally {
            release(con);
        }
    }

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Subcategoria> query(String sql, Binder binder) {
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                binder.bind(ps);
                List<Subcategoria> list = new ArrayList<>();
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(map(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar subcategorias", e);
        } finally {
            release(con);
        }
    }

    static Subcategoria map(ResultSet rs) throws SQLException {
        return Subcategoria.builder()
                .id(uuid(rs.getString("id")))
                .categoriaId(uuid(rs.getString("categoria_id")))
                .nombre(rs.getString("nombre"))
                .descripcion(rs.getString("descripcion"))
                .createdAt(toLdt(rs.getTimestamp("created_at")))
                .updatedAt(toLdt(rs.getTimestamp("updated_at")))
                .build();
    }
}
