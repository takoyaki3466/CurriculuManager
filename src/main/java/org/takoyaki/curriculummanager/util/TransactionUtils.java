package org.takoyaki.curriculummanager.util;

import org.takoyaki.curriculummanager.database.DatabaseManager;
import java.sql.Connection;
import java.sql.SQLException;

public final class TransactionUtils {
    private TransactionUtils() {
    }

    public static <R> R execute(TransactionOperation<R> operation) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                R result = operation.execute(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @FunctionalInterface
    public interface TransactionOperation<R> {
        R execute(Connection connection) throws SQLException;
    }
}
