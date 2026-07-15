package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.Categoria;
import com.utp.cafeteria.entity.Producto;
import com.utp.cafeteria.entity.Subcategoria;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acceso a la tabla {@code productos} usando JDBC puro.
 * Las lecturas traen la categoria y subcategoria (nombre) via JOIN.
 */
@Repository
public class ProductoRepository extends JdbcDao {

    public ProductoRepository(DataSource dataSource) {
        super(dataSource);
    }

    private static final String SELECT_JOIN =
            "SELECT p.id AS id, p.nombre AS nombre, p.descripcion AS descripcion, p.precio AS precio, " +
            "       p.categoria_id AS categoria_id, p.subcategoria_id AS subcategoria_id, " +
            "       p.disponible AS disponible, p.imagen_url AS imagen_url, p.stock AS stock, " +
            "       p.created_at AS created_at, p.updated_at AS updated_at, " +
            "       c.nombre AS cat_nombre, s.nombre AS sub_nombre " +
            "FROM productos p " +
            "LEFT JOIN categorias c    ON p.categoria_id    = c.id " +
            "LEFT JOIN subcategorias s ON p.subcategoria_id = s.id ";

    public List<Producto> findAll() {
        return query(SELECT_JOIN + "ORDER BY p.nombre", ps -> {});
    }

    public List<Producto> findByDisponibleTrue() {
        return query(SELECT_JOIN + "WHERE p.disponible = TRUE ORDER BY p.nombre", ps -> {});
    }

    public Optional<Producto> findById(UUID id) {
        List<Producto> r = query(SELECT_JOIN + "WHERE p.id = ?", ps -> ps.setString(1, str(id)));
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    public List<Producto> findAllById(Collection<UUID> ids) {
        List<Producto> result = new ArrayList<>();
        for (UUID id : ids) {
            findById(id).ifPresent(result::add);
        }
        return result;
    }

    public boolean existsById(UUID id) {
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM productos WHERE id = ?")) {
                ps.setString(1, str(id));
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar producto", e);
        } finally {
            release(con);
        }
    }

    public void deleteById(UUID id) {
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM productos WHERE id = ?")) {
                ps.setString(1, str(id));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar producto", e);
        } finally {
            release(con);
        }
    }

    public Producto save(Producto p) {
        Connection con = null;
        try {
            con = getConnection();
            String catId = p.getCategoria() != null ? str(p.getCategoria().getId()) : null;
            String subId = p.getSubcategoria() != null ? str(p.getSubcategoria().getId()) : null;
            if (p.getId() == null) {
                p.setId(UUID.randomUUID());
                p.setCreatedAt(LocalDateTime.now());
                p.setUpdatedAt(LocalDateTime.now());
                String sql = "INSERT INTO productos (id, nombre, descripcion, precio, categoria_id, subcategoria_id, disponible, imagen_url, stock, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, str(p.getId()));
                    ps.setString(2, p.getNombre());
                    ps.setString(3, p.getDescripcion());
                    ps.setBigDecimal(4, p.getPrecio());
                    ps.setString(5, catId);
                    ps.setString(6, subId);
                    ps.setBoolean(7, Boolean.TRUE.equals(p.getDisponible()));
                    ps.setString(8, p.getImagenUrl());
                    ps.setInt(9, p.getStock() != null ? p.getStock() : 0);
                    ps.setTimestamp(10, toTs(p.getCreatedAt()));
                    ps.setTimestamp(11, toTs(p.getUpdatedAt()));
                    ps.executeUpdate();
                }
            } else {
                p.setUpdatedAt(LocalDateTime.now());
                String sql = "UPDATE productos SET nombre=?, descripcion=?, precio=?, categoria_id=?, subcategoria_id=?, disponible=?, imagen_url=?, stock=?, updated_at=? WHERE id=?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, p.getNombre());
                    ps.setString(2, p.getDescripcion());
                    ps.setBigDecimal(3, p.getPrecio());
                    ps.setString(4, catId);
                    ps.setString(5, subId);
                    ps.setBoolean(6, Boolean.TRUE.equals(p.getDisponible()));
                    ps.setString(7, p.getImagenUrl());
                    ps.setInt(8, p.getStock() != null ? p.getStock() : 0);
                    ps.setTimestamp(9, toTs(p.getUpdatedAt()));
                    ps.setString(10, str(p.getId()));
                    ps.executeUpdate();
                }
            }
            return p;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar producto", e);
        } finally {
            release(con);
        }
    }

    /* ── Soporte ── */

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Producto> query(String sql, Binder binder) {
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                binder.bind(ps);
                List<Producto> list = new ArrayList<>();
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(map(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar productos", e);
        } finally {
            release(con);
        }
    }

    /** Mapea un producto con su categoria y subcategoria (requiere los alias del SELECT_JOIN). */
    static Producto map(ResultSet rs) throws SQLException {
        Categoria categoria = null;
        String catId = rs.getString("categoria_id");
        if (catId != null) {
            categoria = Categoria.builder()
                    .id(uuid(catId))
                    .nombre(rs.getString("cat_nombre"))
                    .build();
        }
        Subcategoria subcategoria = null;
        String subId = rs.getString("subcategoria_id");
        if (subId != null) {
            subcategoria = Subcategoria.builder()
                    .id(uuid(subId))
                    .categoriaId(catId != null ? uuid(catId) : null)
                    .nombre(rs.getString("sub_nombre"))
                    .build();
        }
        return Producto.builder()
                .id(uuid(rs.getString("id")))
                .nombre(rs.getString("nombre"))
                .descripcion(rs.getString("descripcion"))
                .precio(rs.getBigDecimal("precio"))
                .categoria(categoria)
                .subcategoria(subcategoria)
                .disponible(rs.getBoolean("disponible"))
                .imagenUrl(rs.getString("imagen_url"))
                .stock(rs.getInt("stock"))
                .createdAt(toLdt(rs.getTimestamp("created_at")))
                .updatedAt(toLdt(rs.getTimestamp("updated_at")))
                .build();
    }

    /** Mapea solo los campos base del producto (sin categoria). Para usos donde no hay JOIN. */
    static Producto mapBasic(ResultSet rs) throws SQLException {
        return Producto.builder()
                .id(uuid(rs.getString("id")))
                .nombre(rs.getString("nombre"))
                .descripcion(rs.getString("descripcion"))
                .precio(rs.getBigDecimal("precio"))
                .disponible(rs.getBoolean("disponible"))
                .imagenUrl(rs.getString("imagen_url"))
                .stock(rs.getInt("stock"))
                .createdAt(toLdt(rs.getTimestamp("created_at")))
                .updatedAt(toLdt(rs.getTimestamp("updated_at")))
                .build();
    }
}
