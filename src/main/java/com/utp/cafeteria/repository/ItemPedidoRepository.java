package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.ItemPedido;
import com.utp.cafeteria.entity.Producto;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Acceso a la tabla {@code items_pedido} usando JDBC puro.
 * Cada item se devuelve con su {@link Producto} completo (JOIN con productos).
 */
@Repository
public class ItemPedidoRepository extends JdbcDao {

    public ItemPedidoRepository(DataSource dataSource) {
        super(dataSource);
    }

    private static final String SELECT_JOIN =
            "SELECT ip.id AS ip_id, ip.pedido_id, ip.producto_id, ip.cantidad, " +
            "       ip.precio_unitario, ip.subtotal, ip.created_at AS ip_created_at, " +
            "       p.id AS p_id, p.nombre AS p_nombre, p.descripcion AS p_descripcion, " +
            "       p.precio AS p_precio, p.disponible AS p_disponible, " +
            "       p.imagen_url AS p_imagen_url, p.stock AS p_stock, " +
            "       p.created_at AS p_created_at, p.updated_at AS p_updated_at " +
            "FROM items_pedido ip JOIN productos p ON ip.producto_id = p.id ";

    public List<ItemPedido> findByPedidoId(UUID pedidoId) {
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement(SELECT_JOIN + "WHERE ip.pedido_id = ?")) {
                ps.setString(1, str(pedidoId));
                return collect(ps);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar items del pedido", e);
        } finally {
            release(con);
        }
    }

    public List<ItemPedido> findAll() {
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement(SELECT_JOIN)) {
                return collect(ps);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar items", e);
        } finally {
            release(con);
        }
    }

    /** Inserta un item. Usado por {@link PedidoRepository} al guardar un pedido nuevo. */
    public void insert(Connection con, ItemPedido item, UUID pedidoId) throws SQLException {
        if (item.getId() == null) item.setId(UUID.randomUUID());
        if (item.getCreatedAt() == null) item.setCreatedAt(LocalDateTime.now());
        String sql = "INSERT INTO items_pedido (id, pedido_id, producto_id, cantidad, precio_unitario, subtotal, created_at) " +
                "VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, str(item.getId()));
            ps.setString(2, str(pedidoId));
            ps.setString(3, str(item.getProducto().getId()));
            ps.setInt(4, item.getCantidad());
            ps.setBigDecimal(5, item.getPrecioUnitario());
            ps.setBigDecimal(6, item.getSubtotal());
            ps.setTimestamp(7, toTs(item.getCreatedAt()));
            ps.executeUpdate();
        }
    }

    private List<ItemPedido> collect(PreparedStatement ps) throws SQLException {
        List<ItemPedido> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    static ItemPedido map(ResultSet rs) throws SQLException {
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

        return ItemPedido.builder()
                .id(uuid(rs.getString("ip_id")))
                .producto(producto)
                .cantidad(rs.getInt("cantidad"))
                .precioUnitario(rs.getBigDecimal("precio_unitario"))
                .subtotal(rs.getBigDecimal("subtotal"))
                .createdAt(toLdt(rs.getTimestamp("ip_created_at")))
                .build();
    }
}
