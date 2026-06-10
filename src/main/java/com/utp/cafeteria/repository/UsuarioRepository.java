package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.Usuario;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Acceso a la tabla {@code users} usando JDBC puro.
 */
@Repository
public class UsuarioRepository extends JdbcDao {

    public UsuarioRepository(DataSource dataSource) {
        super(dataSource);
    }

    private static final String COLS =
            "id, codigo, email, password, nombre, rol, activo, created_at, updated_at";

    public Optional<Usuario> findById(UUID id) {
        return queryOne("SELECT " + COLS + " FROM users WHERE id = ?", str(id));
    }

    public Optional<Usuario> findByEmail(String email) {
        return queryOne("SELECT " + COLS + " FROM users WHERE email = ?", email);
    }

    public Optional<Usuario> findByCodigo(String codigo) {
        return queryOne("SELECT " + COLS + " FROM users WHERE codigo = ?", codigo);
    }

    public Usuario save(Usuario u) {
        Connection con = null;
        try {
            con = getConnection();
            if (u.getId() == null) {
                u.setId(UUID.randomUUID());
                u.setCreatedAt(LocalDateTime.now());
                u.setUpdatedAt(LocalDateTime.now());
                String sql = "INSERT INTO users (" + COLS + ") VALUES (?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, str(u.getId()));
                    ps.setString(2, u.getCodigo());
                    ps.setString(3, u.getEmail());
                    ps.setString(4, u.getPassword());
                    ps.setString(5, u.getNombre());
                    ps.setString(6, u.getRol().name());
                    ps.setBoolean(7, Boolean.TRUE.equals(u.getActivo()));
                    ps.setTimestamp(8, toTs(u.getCreatedAt()));
                    ps.setTimestamp(9, toTs(u.getUpdatedAt()));
                    ps.executeUpdate();
                }
            } else {
                u.setUpdatedAt(LocalDateTime.now());
                String sql = "UPDATE users SET codigo=?, email=?, password=?, nombre=?, rol=?, activo=?, updated_at=? WHERE id=?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, u.getCodigo());
                    ps.setString(2, u.getEmail());
                    ps.setString(3, u.getPassword());
                    ps.setString(4, u.getNombre());
                    ps.setString(5, u.getRol().name());
                    ps.setBoolean(6, Boolean.TRUE.equals(u.getActivo()));
                    ps.setTimestamp(7, toTs(u.getUpdatedAt()));
                    ps.setString(8, str(u.getId()));
                    ps.executeUpdate();
                }
            }
            return u;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar usuario", e);
        } finally {
            release(con);
        }
    }

    /* ── Mapeo ── */

    private Optional<Usuario> queryOne(String sql, String param) {
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, param);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(map(rs));
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar usuario", e);
        } finally {
            release(con);
        }
    }

    static Usuario map(ResultSet rs) throws SQLException {
        return Usuario.builder()
                .id(uuid(rs.getString("id")))
                .codigo(rs.getString("codigo"))
                .email(rs.getString("email"))
                .password(rs.getString("password"))
                .nombre(rs.getString("nombre"))
                .rol(Usuario.Rol.valueOf(rs.getString("rol")))
                .activo(rs.getBoolean("activo"))
                .createdAt(toLdt(rs.getTimestamp("created_at")))
                .updatedAt(toLdt(rs.getTimestamp("updated_at")))
                .build();
    }
}
