package com.utp.cafeteria.repository;

import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Soporte comun para los DAO de JDBC puro.
 *
 * La conexion se obtiene mediante {@link DataSourceUtils} para que las
 * operaciones participen en la transaccion abierta por @Transactional
 * (si existe). Todo el SQL se escribe a mano con PreparedStatement/ResultSet:
 * NO se usa JPA, Hibernate ni Spring Data.
 */
public abstract class JdbcDao {

    protected final DataSource dataSource;

    protected JdbcDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Conexion ligada a la transaccion actual (o nueva del pool si no hay transaccion). */
    protected Connection getConnection() throws SQLException {
        return DataSourceUtils.getConnection(dataSource);
    }

    /** Devuelve la conexion al pool solo si no pertenece a una transaccion activa. */
    protected void release(Connection con) {
        DataSourceUtils.releaseConnection(con, dataSource);
    }

    /* ── Helpers de conversion de tipos ── */

    protected static UUID uuid(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    protected static String str(UUID value) {
        return value == null ? null : value.toString();
    }

    protected static LocalDateTime toLdt(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    protected static Timestamp toTs(LocalDateTime ldt) {
        return ldt == null ? null : Timestamp.valueOf(ldt);
    }
}
