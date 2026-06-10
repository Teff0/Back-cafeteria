package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.Menu;
import com.utp.cafeteria.entity.Producto;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Acceso a la tabla {@code menus} (y la tabla puente {@code menu_productos}) con JDBC puro.
 */
@Repository
public class MenuRepository extends JdbcDao {

    public MenuRepository(DataSource dataSource) {
        super(dataSource);
    }

    private static final String COLS = "id, fecha, horario, activo, created_at, updated_at";

    private static final String PRODUCTOS_COLS =
            "p.id AS id, p.nombre AS nombre, p.descripcion AS descripcion, p.precio AS precio, " +
            "p.disponible AS disponible, p.imagen_url AS imagen_url, " +
            "p.stock AS stock, p.created_at AS created_at, p.updated_at AS updated_at";

    public Optional<Menu> findById(UUID id) {
        List<Menu> r = query("SELECT " + COLS + " FROM menus WHERE id = ?", ps -> ps.setString(1, str(id)));
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    public List<Menu> findByActivoTrue() {
        return query("SELECT " + COLS + " FROM menus WHERE activo = TRUE ORDER BY fecha DESC", ps -> {});
    }

    public List<Menu> findByFechaAndActivoTrueOrderByHorario(LocalDate fecha) {
        return query("SELECT " + COLS + " FROM menus WHERE fecha = ? AND activo = TRUE ORDER BY horario",
                ps -> ps.setDate(1, Date.valueOf(fecha)));
    }

    public Optional<Menu> findByFechaAndHorarioAndActivoTrue(LocalDate fecha, Menu.Horario horario) {
        List<Menu> r = query("SELECT " + COLS + " FROM menus WHERE fecha = ? AND horario = ? AND activo = TRUE",
                ps -> {
                    ps.setDate(1, Date.valueOf(fecha));
                    ps.setString(2, horario.name());
                });
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    public Menu save(Menu menu) {
        Connection con = null;
        try {
            con = getConnection();
            if (menu.getId() == null) {
                menu.setId(UUID.randomUUID());
                menu.setCreatedAt(LocalDateTime.now());
                menu.setUpdatedAt(LocalDateTime.now());
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO menus (" + COLS + ") VALUES (?,?,?,?,?,?)")) {
                    ps.setString(1, str(menu.getId()));
                    ps.setDate(2, Date.valueOf(menu.getFecha()));
                    ps.setString(3, menu.getHorario().name());
                    ps.setBoolean(4, Boolean.TRUE.equals(menu.getActivo()));
                    ps.setTimestamp(5, toTs(menu.getCreatedAt()));
                    ps.setTimestamp(6, toTs(menu.getUpdatedAt()));
                    ps.executeUpdate();
                }
            } else {
                menu.setUpdatedAt(LocalDateTime.now());
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE menus SET fecha=?, horario=?, activo=?, updated_at=? WHERE id=?")) {
                    ps.setDate(1, Date.valueOf(menu.getFecha()));
                    ps.setString(2, menu.getHorario().name());
                    ps.setBoolean(3, Boolean.TRUE.equals(menu.getActivo()));
                    ps.setTimestamp(4, toTs(menu.getUpdatedAt()));
                    ps.setString(5, str(menu.getId()));
                    ps.executeUpdate();
                }
            }
            syncProductos(con, menu);
            return menu;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar menu", e);
        } finally {
            release(con);
        }
    }

    private void syncProductos(Connection con, Menu menu) throws SQLException {
        try (PreparedStatement del = con.prepareStatement("DELETE FROM menu_productos WHERE menu_id = ?")) {
            del.setString(1, str(menu.getId()));
            del.executeUpdate();
        }
        if (menu.getProductos() == null || menu.getProductos().isEmpty()) return;
        try (PreparedStatement ins = con.prepareStatement(
                "INSERT INTO menu_productos (menu_id, producto_id) VALUES (?,?)")) {
            for (Producto p : menu.getProductos()) {
                ins.setString(1, str(menu.getId()));
                ins.setString(2, str(p.getId()));
                ins.addBatch();
            }
            ins.executeBatch();
        }
    }

    /* ── Soporte ── */

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Menu> query(String sql, Binder binder) {
        List<Menu> menus = new ArrayList<>();
        Connection con = null;
        try {
            con = getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                binder.bind(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) menus.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar menus", e);
        } finally {
            release(con);
        }
        for (Menu m : menus) {
            m.setProductos(loadProductos(m.getId()));
        }
        return menus;
    }

    private Set<Producto> loadProductos(UUID menuId) {
        Set<Producto> productos = new LinkedHashSet<>();
        Connection con = null;
        try {
            con = getConnection();
            String sql = "SELECT " + PRODUCTOS_COLS + " FROM productos p " +
                    "JOIN menu_productos mp ON p.id = mp.producto_id WHERE mp.menu_id = ? ORDER BY p.nombre";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, str(menuId));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) productos.add(ProductoRepository.mapBasic(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar productos del menu", e);
        } finally {
            release(con);
        }
        return productos;
    }

    private static Menu map(ResultSet rs) throws SQLException {
        Date fecha = rs.getDate("fecha");
        return Menu.builder()
                .id(uuid(rs.getString("id")))
                .fecha(fecha != null ? fecha.toLocalDate() : null)
                .horario(Menu.Horario.valueOf(rs.getString("horario")))
                .activo(rs.getBoolean("activo"))
                .createdAt(toLdt(rs.getTimestamp("created_at")))
                .updatedAt(toLdt(rs.getTimestamp("updated_at")))
                .build();
    }
}
