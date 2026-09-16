package org.takoyaki.curriculummanager.repository;

import org.takoyaki.curriculummanager.database.DatabaseManager;
import org.takoyaki.curriculummanager.model.Curriculum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * カリキュラムテーブルを操作するRepository。
 *
 * <p>
 * {@code curricula} テーブルと
 * {@link Curriculum} モデルのデータ変換を担当します。
 * </p>
 */
public class CurriculumRepository {

    /**
     * カリキュラムを新規登録します。
     *
     * @param curriculum 登録するカリキュラム
     */
    public void save(Curriculum curriculum) {

        String sql = """
                INSERT INTO curricula (
                    major_id,
                    name,
                    start_year
                )
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, curriculum.getMajorId());

            statement.setString(2, curriculum.getName());

            statement.setInt(3, curriculum.getStartYear());

            statement.executeUpdate();

            /*
             * 自動採番されたIDを取得する。
             */
            try (ResultSet resultSet = statement.getGeneratedKeys()) {

                if (resultSet.next()) {

                    curriculum.setId(resultSet.getInt(1));
                }
            }

        } catch (SQLException e) {

            /*
             * SQLiteから返された実際のエラー内容を確認できるようにする。
             *
             * 特にUNIQUE制約違反の場合、
             * 「2026年度がすでに存在する」
             * などの原因を確認できます。
             */
            throw new RuntimeException("カリキュラムの登録に失敗しました。\n" + "学科ID: " + curriculum.getMajorId() + "\n" + "カリキュラム名: " + curriculum.getName() + "\n" + "開始年度: " + curriculum.getStartYear() + "\n" + "SQLiteエラー: " + e.getMessage(), e);
        }
    }


    /**
     * IDからカリキュラムを取得します。
     *
     * @param id カリキュラムID
     * @return カリキュラム。存在しない場合はnull
     */
    public Curriculum findById(Integer id) {

        String sql = """
                SELECT
                    id,
                    major_id,
                    name,
                    start_year
                FROM curricula
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

            throw new RuntimeException("カリキュラムの取得に失敗しました。", e);
        }

        return null;
    }

    /**
     * すべてのカリキュラムを取得します。
     *
     * @return カリキュラム一覧
     */
    public List<Curriculum> findAll() {

        String sql = """
                SELECT
                    id,
                    major_id,
                    name,
                    start_year
                FROM curricula
                ORDER BY major_id, start_year, id
                """;

        List<Curriculum> curricula = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                curricula.add(mapRow(resultSet));
            }

        } catch (SQLException e) {

            throw new RuntimeException("カリキュラム一覧の取得に失敗しました。", e);
        }

        return curricula;
    }

    /**
     * 指定された学科に所属するカリキュラムを取得します。
     *
     * @param majorId 学科ID
     * @return カリキュラム一覧
     */
    public List<Curriculum> findByMajorId(Integer majorId) {

        String sql = """
                SELECT
                    id,
                    major_id,
                    name,
                    start_year
                FROM curricula
                WHERE major_id = ?
                ORDER BY start_year, id
                """;

        List<Curriculum> curricula = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, majorId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    curricula.add(mapRow(resultSet));
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException("学科に所属するカリキュラムの取得に失敗しました。", e);
        }

        return curricula;
    }

    /**
     * カリキュラムを更新します。
     *
     * @param curriculum 更新するカリキュラム
     */
    public void update(Curriculum curriculum) {

        String sql = """
                UPDATE curricula
                SET
                    major_id = ?,
                    name = ?,
                    start_year = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, curriculum.getMajorId());

            statement.setString(2, curriculum.getName());

            statement.setInt(3, curriculum.getStartYear());

            statement.setInt(4, curriculum.getId());

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException("カリキュラムの更新に失敗しました。", e);
        }
    }

    /**
     * IDを指定してカリキュラムを削除します。
     *
     * @param id 削除するカリキュラムID
     */
    public void deleteById(Integer id) {

        String sql = """
                DELETE FROM curricula
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException("カリキュラムの削除に失敗しました。", e);
        }
    }

    /**
     * ResultSetの1行をCurriculumへ変換します。
     *
     * @param resultSet SQL検索結果
     * @return カリキュラム
     */
    private Curriculum mapRow(ResultSet resultSet) throws SQLException {

        return new Curriculum(resultSet.getInt("id"), resultSet.getInt("major_id"), resultSet.getString("name"), resultSet.getInt("start_year"));
    }
}
