package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.Menu;
import com.utp.cafeteria.entity.Pedido;
import com.utp.cafeteria.entity.Usuario;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acceso a la tabla {@code pedidos} usando JDBC puro.
 * Cada pedido se devuelve con su usuario, menu (si aplica) e items cargados.
 */
@Repository
public class PedidoRepository extends JdbcDao {

    private final ItemPedidoRepository itemPedidoRepository;

    public PedidoRepository(DataSource dataSource, ItemPedidoRepository itemPedidoRepository) {
        super(dataSource);
        this.itemPedidoRepository = itemPedidoRepository;
    }

    private static final String SELECT_JOIN =
            "SELECT pe.id AS pe_id, pe.estado, pe.hora_programada, pe.total, pe.observaciones, pe.voucher_url, " +
            "       pe.created_at AS pe_created_at, pe.updated_at AS pe_updated_at, " +
            "       u.id AS u_id, u.codigo AS u_codigo, u.email AS u_email, u.password AS u_password, " +
            "       u.nombre AS u_nombre, u.rol AS u_rol, u.activo AS u_activo, " +
            "       u.created_at AS u_created_at, u.updated_at AS u_updated_at, " +
            "       m.id AS m_id, m.fecha AS m_fecha, m.horario AS m_horario, m.activo AS m_activo, " +
            "       m.created_at AS m_created_at, m.updated_at AS m_updated_at " +
            "FROM pedidos pe " +
            "JOIN users u ON pe.usuario_id = u.id " +
            "LEFT JOIN menus m ON pe.menu_id = m.id ";

    public Optional<Pedido> findById(UUID id) {
        List<Pedido> result = run(SELECT_JOIN + "WHERE pe.id = ?", ps -> ps.setString(1, str(id)));
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public List<Pedido> findByUsuarioId(UUID usuarioId) {
        return run(SELECT_JOIN + "WHERE pe.usuario_id = ? ORDER BY pe.created_at DESC",
                ps -> ps.setString(1, str(usuarioId)));
    }

    public List<Pedido> findAll() {
        return run(SELECT_JOIN + "ORDER BY pe.created_at DESC", ps -> {});
    }

    public List<Pedido> findAllEnCola() {
        return run(SELECT_JOIN + "WHERE pe.estado NOT IN ('CANCELADO','ENTREGADO') ORDER BY pe.created_at ASC",
                ps -> {});
    }

    public List<Pedido> findByFechaRangoYEstado(LocalDateTime inicio, LocalDateTime fin) {
        return run(SELECT_JOIN + "WHERE pe.created_at >= ? AND pe.created_at < ? " +
                        "AND pe.estado <> 'CANCELADO' ORDER BY pe.created_at DESC",
                ps -> {
                    ps.setTimestamp(1, toTs(inicio));
                    ps.setTimestamp(2, toTs(fin));
                });
    }

    public Pedido save(Pedido pedido) {
        Connection con = null;
        try {
            con = getConnection();
            if (pedido.getId() == null) {
                pedido.setId(UUID.randomUUID());
                pedido.setCreatedAt(LocalDateTime.now());
                pedido.setUpdatedAt(LocalDateTime.now());
                String sql = "INSERT INTO pedidos (id, usuario_id, menu_id, estado, hora_programada, total, observaciones, voucher_url, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, str(pedido.getId()));
                    ps.setString(2, str(pedido.getUsuario().getId()));
                    ps.setString(3, pedido.getMenu() != null ? str(pedido.getMenu().getId()) : null);
                    ps.setString(4, pedido.getEstado().name());
                    ps.setTime(5, pedido.getHoraProgramada() != null ? Time.valueOf(pedido.getHoraProgramada()) : null);
                    ps.setBigDecimal(6, pedido.getTotal());
                    ps.setString(7, pedido.getObservaciones());
                    ps.setString(8, pedido.getVoucherUrl());
                    ps.setTimestamp(9, toTs(pedido.getCreatedAt()));
                    ps.setTimestamp(10, toTs(pedido.getUpdatedAt()));
                    ps.executeUpdate();
                }
                for (var item : pedido.getItems()) {
                    itemPedidoRepository.insert(con, item, pedido.getId());
                }
            } else {
                pedido.setUpdatedAt(LocalDateTime.now());
                String sql = "UPDATE pedidos SET menu_id=?, estado=?, hora_programada=?, total=?, observaciones=?, voucher_url=?, updated_at=? WHERE id=?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, pedido.getMenu() != null ? str(pedido.getMenu().getId()) : null);
                    ps.setString(2, pedido.getEstado().name());
                    ps.setTime(3, pedido.getHoraProgramada() != null ? Time.valueOf(pedido.getHoraProgramada()) : null);
                    ps.setBigDecimal(4, pedido.getTotal());
                    ps.setString(5, pedido.getObservaciones());
                    ps.setString(6, pedido.getVoucherUrl());
                    ps.setTimestamp(7, toTs(pedido.getUpdatedAt()));
                    ps.setString(8, str(pedido.getId()));
                    ps.executeUpdate();
                }
            }
            return pedido;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar pedido", e);
        } finally {
            release(con);
        }
    }

    /* ── Soporte ── */

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Pedido> run(String sql, Binder binder) {
        List<Pedido> pedidos = new ArrayList<>();
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                binder.bind(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) pedidos.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar pedidos", e);
        } finally {
            release(con);
        }
        // Cargar items de cada pedido (conexion ya liberada arriba)
        for (Pedido p : pedidos) {
            p.setItems(itemPedidoRepository.findByPedidoId(p.getId()));
        }
        return pedidos;
    }

    private static Pedido map(ResultSet rs) throws SQLException {
        Usuario usuario = Usuario.builder()
                .id(uuid(rs.getString("u_id")))
                .codigo(rs.getString("u_codigo"))
                .email(rs.getString("u_email"))
                .password(rs.getString("u_password"))
                .nombre(rs.getString("u_nombre"))
                .rol(Usuario.Rol.valueOf(rs.getString("u_rol")))
                .activo(rs.getBoolean("u_activo"))
                .createdAt(toLdt(rs.getTimestamp("u_created_at")))
                .updatedAt(toLdt(rs.getTimestamp("u_updated_at")))
                .build();

        Menu menu = null;
        String menuId = rs.getString("m_id");
        if (menuId != null) {
            Date fecha = rs.getDate("m_fecha");
            menu = Menu.builder()
                    .id(uuid(menuId))
                    .fecha(fecha != null ? fecha.toLocalDate() : null)
                    .horario(rs.getString("m_horario") != null ? Menu.Horario.valueOf(rs.getString("m_horario")) : null)
                    .activo(rs.getBoolean("m_activo"))
                    .createdAt(toLdt(rs.getTimestamp("m_created_at")))
                    .updatedAt(toLdt(rs.getTimestamp("m_updated_at")))
                    .build();
        }

        Time hora = rs.getTime("hora_programada");
        return Pedido.builder()
                .id(uuid(rs.getString("pe_id")))
                .usuario(usuario)
                .menu(menu)
                .estado(Pedido.Estado.valueOf(rs.getString("estado")))
                .horaProgramada(hora != null ? hora.toLocalTime() : null)
                .total(rs.getBigDecimal("total"))
                .observaciones(rs.getString("observaciones"))
                .voucherUrl(rs.getString("voucher_url"))
                .createdAt(toLdt(rs.getTimestamp("pe_created_at")))
                .updatedAt(toLdt(rs.getTimestamp("pe_updated_at")))
                .build();
    }
}
