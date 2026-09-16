package org.takoyaki.curriculummanager.repository;

import org.takoyaki.curriculummanager.database.DatabaseManager;
import org.takoyaki.curriculummanager.model.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Courseのデータベース操作を担当するRepositoryです。
 * <p>
 * このクラスでは、
 * <p>
 * ・科目の追加
 * ・科目の取得
 * ・科目コードによる検索
 * ・カテゴリーによる検索
 * ・科目の更新
 * ・科目の削除
 * <p>
 * など、coursesテーブルに対する操作を行います。
 * <p>
 * 「必修」「選択」などの情報は
 * CurriculumCourseが担当するため、
 * このRepositoryでは扱いません。
 */
public class CourseRepository {

    /**
     * 科目を新規登録します。
     * <p>
     * 登録後、データベースが発行したIDを
     * Courseオブジェクトへ設定します。
     *
     * @param course 登録する科目
     * @return IDが設定された科目
     */
    public Course save(Course course) throws SQLException {

        String sql = """
                INSERT INTO courses (
                    course_code,
                    name,
                    credits,
                    category_id,
                    description
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            /*
             * 科目コード。
             *
             * NULLも許可されているため、
             * nullの場合はSQL NULLとして登録します。
             */
            if (course.getCourseCode() == null) {
                statement.setNull(1, java.sql.Types.VARCHAR);
            } else {
                statement.setString(1, course.getCourseCode());
            }

            /*
             * 科目名
             */
            statement.setString(2, course.getName());

            /*
             * 単位数
             */
            statement.setDouble(3, course.getCredits());

            /*
             * カテゴリーID。
             *
             * カテゴリー未設定の場合はNULLにします。
             */
            if (course.getCategoryId() == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, course.getCategoryId());
            }

            /*
             * 説明。
             *
             * descriptionもNULLを許可しています。
             */
            if (course.getDescription() == null) {
                statement.setNull(5, java.sql.Types.VARCHAR);
            } else {
                statement.setString(5, course.getDescription());
            }

            statement.executeUpdate();

            /*
             * INSERT時に自動生成されたIDを取得します。
             */
            try (ResultSet resultSet = statement.getGeneratedKeys()) {

                if (resultSet.next()) {
                    course.setId(resultSet.getInt(1));
                }
            }
        }

        return course;
    }

    /**
     * IDを指定して科目を1件取得します。
     *
     * @param id 科目ID
     * @return 該当する科目。
     * 存在しない場合はnull。
     */
    public Course findById(Integer id) throws SQLException {

        String sql = """
                SELECT
                    id,
                    course_code,
                    name,
                    credits,
                    category_id,
                    description
                FROM courses
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
     * すべての科目を取得します。
     * <p>
     * 科目名の順番で並べます。
     *
     * @return すべての科目
     */
    public List<Course> findAll() throws SQLException {

        String sql = """
                SELECT
                    id,
                    course_code,
                    name,
                    credits,
                    category_id,
                    description
                FROM courses
                ORDER BY name, id
                """;

        List<Course> courses = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                courses.add(mapRow(resultSet));
            }
        }

        return courses;
    }

    /**
     * 科目コードを指定して科目を取得します。
     * <p>
     * course_codeはデータベース上で
     * UNIQUEになっているため、
     * 同じコードの科目は存在しません。
     *
     * @param courseCode 科目コード
     * @return 該当する科目。
     * 存在しない場合はnull。
     */
    public Course findByCourseCode(String courseCode) throws SQLException {

        String sql = """
                SELECT
                    id,
                    course_code,
                    name,
                    credits,
                    category_id,
                    description
                FROM courses
                WHERE course_code = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, courseCode);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    /**
     * 指定したカテゴリーに所属する科目を取得します。
     * <p>
     * 例えば、
     * <p>
     * 「数学」カテゴリーに所属する科目
     * <p>
     * を一覧表示するときなどに使用します。
     *
     * @param categoryId カテゴリーID
     * @return 科目一覧
     */
    public List<Course> findByCategoryId(Integer categoryId) throws SQLException {

        String sql = """
                SELECT
                    id,
                    course_code,
                    name,
                    credits,
                    category_id,
                    description
                FROM courses
                WHERE category_id = ?
                ORDER BY name, id
                """;

        List<Course> courses = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    courses.add(mapRow(resultSet));
                }
            }
        }

        return courses;
    }

    /**
     * 科目を更新します。
     *
     * @param course 更新する科目
     */
    public void update(Course course) throws SQLException {

        String sql = """
                UPDATE courses
                SET
                    course_code = ?,
                    name = ?,
                    credits = ?,
                    category_id = ?,
                    description = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            /*
             * 科目コード
             */
            if (course.getCourseCode() == null) {
                statement.setNull(1, java.sql.Types.VARCHAR);
            } else {
                statement.setString(1, course.getCourseCode());
            }

            /*
             * 科目名
             */
            statement.setString(2, course.getName());

            /*
             * 単位数
             */
            statement.setDouble(3, course.getCredits());

            /*
             * カテゴリー
             */
            if (course.getCategoryId() == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, course.getCategoryId());
            }

            /*
             * 説明
             */
            if (course.getDescription() == null) {
                statement.setNull(5, java.sql.Types.VARCHAR);
            } else {
                statement.setString(5, course.getDescription());
            }

            /*
             * 更新対象のID
             */
            statement.setInt(6, course.getId());

            statement.executeUpdate();
        }
    }

    /**
     * IDを指定して科目を削除します。
     * <p>
     * coursesテーブル側の
     * ON DELETE CASCADEにより、
     * この科目を参照している
     * CurriculumCourseやEnrollmentも
     * データベース設定に従って削除されます。
     *
     * @param id 削除する科目ID
     */
    public void deleteById(Integer id) throws SQLException {

        String sql = """
                DELETE FROM courses
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            statement.executeUpdate();
        }
    }

    /**
     * ResultSetの1行をCourseオブジェクトへ変換します。
     * <p>
     * category_idはNULLになる可能性があるため、
     * wasNull()を使用してNULLを判定します。
     */
    private Course mapRow(ResultSet resultSet) throws SQLException {

        Integer id = resultSet.getInt("id");

        String courseCode = resultSet.getString("course_code");

        String name = resultSet.getString("name");

        double credits = resultSet.getDouble("credits");

        /*
         * category_idはNULL可能です。
         */
        int categoryIdValue = resultSet.getInt("category_id");

        Integer categoryId;

        if (resultSet.wasNull()) {
            categoryId = null;
        } else {
            categoryId = categoryIdValue;
        }

        String description = resultSet.getString("description");

        return new Course(id, courseCode, name, credits, categoryId, description);
    }
}