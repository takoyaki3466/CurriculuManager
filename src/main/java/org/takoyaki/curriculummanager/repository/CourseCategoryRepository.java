package org.takoyaki.curriculummanager.repository;

import org.takoyaki.curriculummanager.database.DatabaseManager;
import org.takoyaki.curriculummanager.model.CourseCategory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * CourseCategoryのデータベース操作を担当するRepositoryです。
 * <p>
 * 科目カテゴリーは階層構造を持つため、
 * 親カテゴリー・子カテゴリーを扱う処理も
 * このRepositoryで担当します。
 * <p>
 * 例:
 * <p>
 * 一般教養
 * ├─ グループ1
 * ├─ グループ2
 * ├─ グループ3
 * └─ グループ4
 */
public class CourseCategoryRepository {

    /**
     * カテゴリーを新規登録します。
     *
     * @param category 登録するカテゴリー
     * @return IDが設定されたカテゴリー
     */
    public CourseCategory save(CourseCategory category) throws SQLException {

        String sql = """
                INSERT INTO course_categories (
                    curriculum_id,
                    parent_id,
                    name,
                    display_order
                )
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            /*
             * curriculum_id
             */
            statement.setInt(1, category.getCurriculumId());

            /*
             * parent_idはNULLになる可能性があります。
             *
             * 親カテゴリーがない場合は、
             * parent_id = NULLになります。
             */
            if (category.getParentId() == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, category.getParentId());
            }

            /*
             * カテゴリー名
             */
            statement.setString(3, category.getName());

            /*
             * 表示順
             */
            statement.setInt(4, category.getDisplayOrder());

            statement.executeUpdate();

            /*
             * 自動生成されたIDを取得します。
             */
            try (ResultSet resultSet = statement.getGeneratedKeys()) {

                if (resultSet.next()) {
                    category.setId(resultSet.getInt(1));
                }
            }
        }

        return category;
    }

    /**
     * IDを指定してカテゴリーを1件取得します。
     *
     * @param id カテゴリーID
     * @return 該当するカテゴリー。
     * 存在しない場合はnull。
     */
    public CourseCategory findById(Integer id) throws SQLException {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    parent_id,
                    name,
                    display_order
                FROM course_categories
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
        }

        return null;
    }

    /**
     * すべてのカテゴリーを取得します。
     * <p>
     * カリキュラムID、親ID、表示順の順で並べます。
     *
     * @return すべてのカテゴリー
     */
    public List<CourseCategory> findAll() throws SQLException {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    parent_id,
                    name,
                    display_order
                FROM course_categories
                ORDER BY
                    curriculum_id,
                    parent_id,
                    display_order,
                    id
                """;

        List<CourseCategory> categories = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                categories.add(mapRow(resultSet));
            }
        }

        return categories;
    }

    /**
     * 指定したカリキュラムに属する
     * すべてのカテゴリーを取得します。
     * <p>
     * 親カテゴリー・子カテゴリーの両方が取得されます。
     *
     * @param curriculumId カリキュラムID
     * @return カテゴリー一覧
     */
    public List<CourseCategory> findByCurriculumId(Integer curriculumId) throws SQLException {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    parent_id,
                    name,
                    display_order
                FROM course_categories
                WHERE curriculum_id = ?
                ORDER BY
                    parent_id,
                    display_order,
                    id
                """;

        List<CourseCategory> categories = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, curriculumId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    categories.add(mapRow(resultSet));
                }
            }
        }

        return categories;
    }

    /**
     * 指定した親カテゴリーの
     * 直接の子カテゴリーを取得します。
     * <p>
     * 例えば、
     * <p>
     * 一般教養
     * ├─ グループ1
     * ├─ グループ2
     * └─ グループ3
     * <p>
     * の「グループ1～3」を取得する場合に使用します。
     *
     * @param parentId 親カテゴリーID
     * @return 子カテゴリー一覧
     */
    public List<CourseCategory> findByParentId(Integer parentId) throws SQLException {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    parent_id,
                    name,
                    display_order
                FROM course_categories
                WHERE parent_id = ?
                ORDER BY
                    display_order,
                    id
                """;

        List<CourseCategory> categories = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, parentId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    categories.add(mapRow(resultSet));
                }
            }
        }

        return categories;
    }

    /**
     * 親カテゴリーを取得します。
     * <p>
     * parent_idがNULLのカテゴリー、
     * つまり最上位カテゴリーを取得します。
     *
     * @param curriculumId カリキュラムID
     * @return 最上位カテゴリー一覧
     */
    public List<CourseCategory> findRootCategories(Integer curriculumId) throws SQLException {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    parent_id,
                    name,
                    display_order
                FROM course_categories
                WHERE curriculum_id = ?
                  AND parent_id IS NULL
                ORDER BY
                    display_order,
                    id
                """;

        List<CourseCategory> categories = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, curriculumId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    categories.add(mapRow(resultSet));
                }
            }
        }

        return categories;
    }

    /**
     * カテゴリーを更新します。
     *
     * @param category 更新するカテゴリー
     */
    public void update(CourseCategory category) throws SQLException {

        String sql = """
                UPDATE course_categories
                SET
                    curriculum_id = ?,
                    parent_id = ?,
                    name = ?,
                    display_order = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, category.getCurriculumId());

            /*
             * 親カテゴリーがない場合はNULLを設定します。
             */
            if (category.getParentId() == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, category.getParentId());
            }

            statement.setString(3, category.getName());

            statement.setInt(4, category.getDisplayOrder());

            statement.setInt(5, category.getId());

            statement.executeUpdate();
        }
    }

    /**
     * IDを指定してカテゴリーを削除します。
     * <p>
     * データベース側の
     * <p>
     * ON DELETE CASCADE
     * <p>
     * により、このカテゴリーを親としている
     * 子カテゴリーも削除されます。
     *
     * @param id 削除するカテゴリーID
     */
    public void deleteById(Integer id) throws SQLException {

        String sql = """
                DELETE FROM course_categories
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            statement.executeUpdate();
        }
    }

    /**
     * ResultSetの1行を
     * CourseCategoryオブジェクトへ変換します。
     */
    private CourseCategory mapRow(ResultSet resultSet) throws SQLException {

        Integer id = resultSet.getInt("id");

        Integer curriculumId = resultSet.getInt("curriculum_id");

        /*
         * parent_idはNULLになる可能性があります。
         *
         * getInt()だけでは、
         * NULLの場合も0として返ってしまいます。
         *
         * そのためwasNull()を使用して
         * NULLを正しく判定します。
         */
        int parentIdValue = resultSet.getInt("parent_id");

        Integer parentId;

        if (resultSet.wasNull()) {
            parentId = null;
        } else {
            parentId = parentIdValue;
        }

        String name = resultSet.getString("name");

        int displayOrder = resultSet.getInt("display_order");

        return new CourseCategory(id, curriculumId, parentId, name, displayOrder);
    }
}