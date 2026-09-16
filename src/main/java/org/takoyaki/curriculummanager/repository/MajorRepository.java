package org.takoyaki.curriculummanager.repository;

import org.takoyaki.curriculummanager.database.DatabaseManager;
import org.takoyaki.curriculummanager.model.Major;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 学科テーブルを操作するRepository。
 *
 * <p>
 * {@code majors} テーブルと {@link Major} モデルの
 * データ変換を担当します。
 * </p>
 */
public class MajorRepository {

    /**
     * 学科を新規登録します。
     *
     * @param major 登録する学科
     */
    public void save(Major major) {

        String sql = """
                INSERT INTO majors (
                    department_id,
                    name
                )
                VALUES (?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, major.getDepartmentId());

            statement.setString(2, major.getName());

            statement.executeUpdate();

            /*
             * 自動採番されたIDを取得する。
             */
            try (ResultSet resultSet = statement.getGeneratedKeys()) {

                if (resultSet.next()) {
                    major.setId(resultSet.getInt(1));
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException("学科の登録に失敗しました。", e);
        }
    }

    /**
     * IDから学科を取得します。
     *
     * @param id 学科ID
     * @return 学科。存在しない場合はnull
     */
    public Major findById(Integer id) {

        String sql = """
                SELECT
                    id,
                    department_id,
                    name
                FROM majors
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException("学科の取得に失敗しました。", e);
        }

        return null;
    }

    /**
     * すべての学科を取得します。
     *
     * @return 学科一覧
     */
    public List<Major> findAll() {

        String sql = """
                SELECT
                    id,
                    department_id,
                    name
                FROM majors
                ORDER BY department_id, id
                """;

        List<Major> majors = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                majors.add(mapRow(resultSet));
            }

        } catch (SQLException e) {

            throw new RuntimeException("学科一覧の取得に失敗しました。", e);
        }

        return majors;
    }

    /**
     * 指定された学部に所属する学科を取得します。
     *
     * @param departmentId 学部ID
     * @return 学科一覧
     */
    public List<Major> findByDepartmentId(Integer departmentId) {

        String sql = """
                SELECT
                    id,
                    department_id,
                    name
                FROM majors
                WHERE department_id = ?
                ORDER BY id
                """;

        List<Major> majors = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, departmentId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    majors.add(mapRow(resultSet));
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException("学部に所属する学科の取得に失敗しました。", e);
        }

        return majors;
    }

    /**
     * 学科情報を更新します。
     *
     * @param major 更新する学科
     */
    public void update(Major major) {

        String sql = """
                UPDATE majors
                SET
                    department_id = ?,
                    name = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, major.getDepartmentId());

            statement.setString(2, major.getName());

            statement.setInt(3, major.getId());

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException("学科の更新に失敗しました。", e);
        }
    }

    /**
     * IDを指定して学科を削除します。
     *
     * @param id 削除する学科ID
     */
    public void deleteById(Integer id) {

        String sql = """
                DELETE FROM majors
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException("学科の削除に失敗しました。", e);
        }
    }

    /**
     * ResultSetの1行をMajorへ変換します。
     */
    private Major mapRow(ResultSet resultSet) throws SQLException {

        return new Major(resultSet.getInt("id"), resultSet.getInt("department_id"), resultSet.getString("name"));
    }
}
