package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.Categoria;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acceso a la tabla {@code categorias} usando JDBC puro.
 */
@Repository
public class CategoriaRepository extends JdbcDao {

    public CategoriaRepository(DataSource dataSource) {
        super(dataSource);
    }

    private static final String COLS = "id, nombre, descripcion, activo, created_at, updated_at";

    public List<Categoria> findAll() {
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT " + COLS + " FROM categorias ORDER BY nombre")) {
                List<Categoria> list = new ArrayList<>();
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(map(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar categorias", e);
        } finally {
            release(con);
        }
    }

    public Optional<Categoria> findById(UUID id) {
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT " + COLS + " FROM categorias WHERE id = ?")) {
                ps.setString(1, str(id));
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(map(rs)) : Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar categoria", e);
        } finally {
            release(con);
        }
    }

    public boolean existsById(UUID id) {
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM categorias WHERE id = ?")) {
                ps.setString(1, str(id));
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar categoria", e);
        } finally {
            release(con);
        }
    }

    public void deleteById(UUID id) {
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM categorias WHERE id = ?")) {
                ps.setString(1, str(id));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar categoria", e);
        } finally {
            release(con);
        }
    }

    public Categoria save(Categoria c) {
        Connection con = null;
        try {
            con = getConnection();
            if (c.getId() == null) {
                c.setId(UUID.randomUUID());
                c.setCreatedAt(LocalDateTime.now());
                c.setUpdatedAt(LocalDateTime.now());
                if (c.getActivo() == null) c.setActivo(true);
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO categorias (" + COLS + ") VALUES (?,?,?,?,?,?)")) {
                    ps.setString(1, str(c.getId()));
                    ps.setString(2, c.getNombre());
                    ps.setString(3, c.getDescripcion());
                    ps.setBoolean(4, Boolean.TRUE.equals(c.getActivo()));
                    ps.setTimestamp(5, toTs(c.getCreatedAt()));
                    ps.setTimestamp(6, toTs(c.getUpdatedAt()));
                    ps.executeUpdate();
                }
            } else {
                c.setUpdatedAt(LocalDateTime.now());
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE categorias SET nombre=?, descripcion=?, activo=?, updated_at=? WHERE id=?")) {
                    ps.setString(1, c.getNombre());
                    ps.setString(2, c.getDescripcion());
                    ps.setBoolean(3, Boolean.TRUE.equals(c.getActivo()));
                    ps.setTimestamp(4, toTs(c.getUpdatedAt()));
                    ps.setString(5, str(c.getId()));
                    ps.executeUpdate();
                }
            }
            return c;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar categoria", e);
        } finally {
            release(con);
        }
    }

    static Categoria map(ResultSet rs) throws SQLException {
        return Categoria.builder()
                .id(uuid(rs.getString("id")))
                .nombre(rs.getString("nombre"))
                .descripcion(rs.getString("descripcion"))
                .activo(rs.getBoolean("activo"))
                .createdAt(toLdt(rs.getTimestamp("created_at")))
                .updatedAt(toLdt(rs.getTimestamp("updated_at")))
                .build();
    }
}
