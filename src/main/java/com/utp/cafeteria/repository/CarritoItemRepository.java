package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.CarritoItem;
import com.utp.cafeteria.entity.Producto;
import com.utp.cafeteria.entity.Usuario;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acceso a la tabla {@code carrito_items} usando JDBC puro.
 * Cada item se devuelve con su {@link Producto} completo y el usuario (id).
 */
@Repository
public class CarritoItemRepository extends JdbcDao {

    public CarritoItemRepository(DataSource dataSource) {
        super(dataSource);
    }

    private static final String SELECT_JOIN =
            "SELECT ci.id AS ci_id, ci.usuario_id, ci.cantidad, " +
            "       p.id AS p_id, p.nombre AS p_nombre, p.descripcion AS p_descripcion, " +
            "       p.precio AS p_precio, p.disponible AS p_disponible, " +
            "       p.imagen_url AS p_imagen_url, p.stock AS p_stock, " +
            "       p.created_at AS p_created_at, p.updated_at AS p_updated_at " +
            "FROM carrito_items ci JOIN productos p ON ci.producto_id = p.id ";

    public List<CarritoItem> findByUsuarioId(UUID usuarioId) {
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement(SELECT_JOIN + "WHERE ci.usuario_id = ?")) {
                ps.setString(1, str(usuarioId));
                return collect(ps);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar carrito", e);
        } finally {
            release(con);
        }
    }

    public Optional<CarritoItem> findByUsuarioIdAndProductoId(UUID usuarioId, UUID productoId) {
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement(SELECT_JOIN + "WHERE ci.usuario_id = ? AND ci.producto_id = ?")) {
                ps.setString(1, str(usuarioId));
                ps.setString(2, str(productoId));
                List<CarritoItem> r = collect(ps);
                return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar carrito", e);
        } finally {
            release(con);
        }
    }

    public Optional<CarritoItem> findById(UUID id) {
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement(SELECT_JOIN + "WHERE ci.id = ?")) {
                ps.setString(1, str(id));
                List<CarritoItem> r = collect(ps);
                return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar item de carrito", e);
        } finally {
            release(con);
        }
    }

    public CarritoItem save(CarritoItem item) {
        Connection con = null;
        try {
            con = getConnection();
            if (item.getId() == null) {
                item.setId(UUID.randomUUID());
                String sql = "INSERT INTO carrito_items (id, usuario_id, producto_id, cantidad) VALUES (?,?,?,?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, str(item.getId()));
                    ps.setString(2, str(item.getUsuario().getId()));
                    ps.setString(3, str(item.getProducto().getId()));
                    ps.setInt(4, item.getCantidad());
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = con.prepareStatement("UPDATE carrito_items SET cantidad=? WHERE id=?")) {
                    ps.setInt(1, item.getCantidad());
                    ps.setString(2, str(item.getId()));
                    ps.executeUpdate();
                }
            }
            return item;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar item de carrito", e);
        } finally {
            release(con);
        }
    }

    public void delete(CarritoItem item) {
        deleteById(item.getId());
    }

    public void deleteAll(Collection<CarritoItem> items) {
        for (CarritoItem item : items) {
            deleteById(item.getId());
        }
    }

    private void deleteById(UUID id) {
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM carrito_items WHERE id = ?")) {
                ps.setString(1, str(id));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar item de carrito", e);
        } finally {
            release(con);
        }
    }

    private List<CarritoItem> collect(PreparedStatement ps) throws SQLException {
        List<CarritoItem> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    static CarritoItem map(ResultSet rs) throws SQLException {
        Producto producto = Producto.builder()
                .id(uuid(rs.getString("p_id")))
                .nombre(rs.getString("p_nombre"))
                .descripcion(rs.getString("p_descripcion"))
                .precio(rs.getBigDecimal("p_precio"))
                .disponible(rs.getBoolean("p_disponible"))
                .imagenUrl(rs.getString("p_imagen_url"))
                .stock(rs.getInt("p_stock"))
                .createdAt(toLdt(rs.getTimestamp("p_created_at")))
                .updatedAt(toLdt(rs.getTimestamp("p_updated_at")))
                .build();

        Usuario usuario = Usuario.builder()
                .id(uuid(rs.getString("usuario_id")))
                .build();

        return CarritoItem.builder()
                .id(uuid(rs.getString("ci_id")))
                .usuario(usuario)
                .producto(producto)
                .cantidad(rs.getInt("cantidad"))
                .build();
    }
}
