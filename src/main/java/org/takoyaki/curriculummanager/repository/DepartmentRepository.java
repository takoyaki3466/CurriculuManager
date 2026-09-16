package org.takoyaki.curriculummanager.repository;

import org.takoyaki.curriculummanager.database.DatabaseManager;
import org.takoyaki.curriculummanager.model.Department;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Departmentのデータベース操作を担当するRepository。
 * <p>
 * このクラスでは、
 * <p>
 * ・学科の追加
 * ・学科の取得
 * ・学科の更新
 * ・学科の削除
 * <p>
 * など、departmentsテーブルに対する操作を行います。
 * <p>
 * GUIやビジネスロジックはここには記述しません。
 */
public class DepartmentRepository {

    /**
     * 学科をデータベースに追加します。
     * <p>
     * INSERT後にSQLiteから発行されたIDを取得し、
     * Departmentオブジェクトに設定します。
     *
     * @param department 追加する学科
     * @return DBに登録された学科
     * @throws SQLException データベース操作に失敗した場合
     */
    public Department save(Department department) throws SQLException {

        String sql = """
                INSERT INTO departments (name)
                VALUES (?)
                """;

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, department.getName());

            statement.executeUpdate();

            /*
             * SQLiteが自動生成したIDを取得します。
             */
            try (ResultSet resultSet = statement.getGeneratedKeys()) {

                if (resultSet.next()) {
                    department.setId(resultSet.getInt(1));
                }
            }
        }

        return department;
    }

    /**
     * IDを指定して学科を1件取得します。
     *
     * @param id 学科ID
     * @return 該当する学科。存在しない場合はnull
     * @throws SQLException データベース操作に失敗した場合
     */
    public Department findById(Integer id) throws SQLException {

        String sql = """
                SELECT
                    id,
                    name
                FROM departments
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    /**
     * すべての学科を取得します。
     * <p>
     * ID順で取得します。
     *
     * @return 学科のリスト
     * @throws SQLException データベース操作に失敗した場合
     */
    public List<Department> findAll() throws SQLException {

        String sql = """
                SELECT
                    id,
                    name
                FROM departments
                ORDER BY id
                """;

        List<Department> departments = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                departments.add(mapRow(resultSet));
            }
        }

        return departments;
    }

    /**
     * 学科情報を更新します。
     *
     * @param department 更新する学科
     * @throws SQLException データベース操作に失敗した場合
     */
    public void update(Department department) throws SQLException {

        String sql = """
                UPDATE departments
                SET name = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, department.getName());

            statement.setInt(2, department.getId());

            statement.executeUpdate();
        }
    }

    /**
     * IDを指定して学科を削除します。
     * <p>
     * departmentsを削除すると、
     * 外部キーのON DELETE CASCADEにより
     * その学科に所属するCurriculumも削除されます。
     *
     * @param id 削除する学科ID
     * @throws SQLException データベース操作に失敗した場合
     */
    public void deleteById(Integer id) throws SQLException {

        String sql = """
                DELETE FROM departments
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            statement.executeUpdate();
        }
    }

    /**
     * ResultSetの1行をDepartmentオブジェクトに変換します。
     * <p>
     * SQLの結果をModelへ変換する処理を
     * ここにまとめることで、findByIdやfindAllの
     * コードが重複するのを防ぎます。
     *
     * @param resultSet SQLの検索結果
     * @return Departmentオブジェクト
     * @throws SQLException ResultSetの読み込みに失敗した場合
     */
    private Department mapRow(ResultSet resultSet) throws SQLException {

        Integer id = resultSet.getInt("id");
        String name = resultSet.getString("name");

        return new Department(id, name);
    }
}