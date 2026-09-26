package org.takoyaki.curriculummanager.repository.abstracts;

import org.takoyaki.curriculummanager.database.DatabaseManager;
import org.takoyaki.curriculummanager.repository.interfaces.CrudRepository;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractJdbcRepository<T> implements CrudRepository<T, Integer> {
    protected Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    protected <R> R executeInTransaction(TransactionOperation<R> operation) throws SQLException {
        try (Connection connection = getConnection()) {
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
    protected interface TransactionOperation<R> {
        R execute(Connection connection) throws SQLException;
    }
}
