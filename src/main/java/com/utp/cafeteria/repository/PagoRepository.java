package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.Pago;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Acceso a la tabla {@code pagos} usando JDBC puro.
 */
@Repository
public class PagoRepository extends JdbcDao {

    private final PedidoRepository pedidoRepository;

    public PagoRepository(DataSource dataSource, PedidoRepository pedidoRepository) {
        super(dataSource);
        this.pedidoRepository = pedidoRepository;
    }

    private static final String COLS =
            "id, pedido_id, monto, metodo_pago, estado, codigo_transaccion, fecha_pago, created_at, updated_at";

    public Optional<Pago> findByPedidoId(UUID pedidoId) {
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT " + COLS + " FROM pagos WHERE pedido_id = ?")) {
                ps.setString(1, str(pedidoId));
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    Pago pago = map(rs);
                    // Adjuntar el pedido (con usuario) que el servicio necesita
                    pedidoRepository.findById(uuid(rs.getString("pedido_id"))).ifPresent(pago::setPedido);
                    return Optional.of(pago);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar pago", e);
        } finally {
            release(con);
        }
    }

    public boolean existsByPedidoId(UUID pedidoId) {
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM pagos WHERE pedido_id = ?")) {
                ps.setString(1, str(pedidoId));
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar pago", e);
        } finally {
            release(con);
        }
    }

    public Pago save(Pago pago) {
        Connection con = null;
        try {
            con = getConnection();
            if (pago.getId() == null) {
                pago.setId(UUID.randomUUID());
                pago.setCreatedAt(LocalDateTime.now());
                pago.setUpdatedAt(LocalDateTime.now());
                String sql = "INSERT INTO pagos (" + COLS + ") VALUES (?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, str(pago.getId()));
                    ps.setString(2, str(pago.getPedido().getId()));
                    ps.setBigDecimal(3, pago.getMonto());
                    ps.setString(4, pago.getMetodoPago().name());
                    ps.setString(5, pago.getEstado().name());
                    ps.setString(6, pago.getCodigoTransaccion());
                    ps.setTimestamp(7, toTs(pago.getFechaPago()));
                    ps.setTimestamp(8, toTs(pago.getCreatedAt()));
                    ps.setTimestamp(9, toTs(pago.getUpdatedAt()));
                    ps.executeUpdate();
                }
            } else {
                pago.setUpdatedAt(LocalDateTime.now());
                String sql = "UPDATE pagos SET monto=?, metodo_pago=?, estado=?, codigo_transaccion=?, fecha_pago=?, updated_at=? WHERE id=?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setBigDecimal(1, pago.getMonto());
                    ps.setString(2, pago.getMetodoPago().name());
                    ps.setString(3, pago.getEstado().name());
                    ps.setString(4, pago.getCodigoTransaccion());
                    ps.setTimestamp(5, toTs(pago.getFechaPago()));
                    ps.setTimestamp(6, toTs(pago.getUpdatedAt()));
                    ps.setString(7, str(pago.getId()));
                    ps.executeUpdate();
                }
            }
            return pago;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar pago", e);
        } finally {
            release(con);
        }
    }

    static Pago map(ResultSet rs) throws SQLException {
        return Pago.builder()
                .id(uuid(rs.getString("id")))
                .monto(rs.getBigDecimal("monto"))
                .metodoPago(Pago.MetodoPago.valueOf(rs.getString("metodo_pago")))
                .estado(Pago.EstadoPago.valueOf(rs.getString("estado")))
                .codigoTransaccion(rs.getString("codigo_transaccion"))
                .fechaPago(toLdt(rs.getTimestamp("fecha_pago")))
                .createdAt(toLdt(rs.getTimestamp("created_at")))
                .updatedAt(toLdt(rs.getTimestamp("updated_at")))
                .build();
    }
}
