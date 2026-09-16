package org.takoyaki.curriculummanager.repository;

import org.takoyaki.curriculummanager.database.DatabaseManager;
import org.takoyaki.curriculummanager.model.CurriculumCourse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * CurriculumCourseのデータベース操作を担当するRepositoryです。
 * <p>
 * CurriculumとCourseの関係を管理します。
 * <p>
 * 例えば、
 * <p>
 * 2026年度カリキュラム
 * │
 * ├─ 数学       → 必修
 * ├─ プログラミング → 必修
 * ├─ 英語       → 選択必修
 * └─ 経済学     → 選択
 * <p>
 * のような情報を管理します。
 * <p>
 * 「必修」「選択」などの扱いはCourseではなく、
 * CurriculumCourseが持ちます。
 */
public class CurriculumCourseRepository {

    /**
     * カリキュラムと科目の関係を新規登録します。
     *
     * @param curriculumCourse 登録する関係
     * @return IDが設定されたCurriculumCourse
     */
    public CurriculumCourse save(CurriculumCourse curriculumCourse) throws SQLException {

        String sql = """
                INSERT INTO curriculum_courses (
                    curriculum_id,
                    course_id,
                    requirement_type
                )
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            /*
             * カリキュラムID
             */
            statement.setInt(1, curriculumCourse.getCurriculumId());

            /*
             * 科目ID
             */
            statement.setInt(2, curriculumCourse.getCourseId());

            /*
             * 必修・選択などの区分
             */
            statement.setString(3, curriculumCourse.getRequirementType());

            statement.executeUpdate();

            /*
             * 自動生成されたIDを取得します。
             */
            try (ResultSet resultSet = statement.getGeneratedKeys()) {

                if (resultSet.next()) {
                    curriculumCourse.setId(resultSet.getInt(1));
                }
            }
        }

        return curriculumCourse;
    }

    /**
     * IDを指定して1件取得します。
     *
     * @param id CurriculumCourseのID
     * @return 該当する関係。
     * 存在しない場合はnull。
     */
    public CurriculumCourse findById(Integer id) throws SQLException {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    requirement_type
                FROM curriculum_courses
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
     * すべてのCurriculumCourseを取得します。
     *
     * @return すべての関係
     */
    public List<CurriculumCourse> findAll() throws SQLException {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    requirement_type
                FROM curriculum_courses
                ORDER BY
                    curriculum_id,
                    course_id
                """;

        List<CurriculumCourse> curriculumCourses = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                curriculumCourses.add(mapRow(resultSet));
            }
        }

        return curriculumCourses;
    }

    /**
     * 指定したカリキュラムに登録されている
     * 科目の一覧を取得します。
     * <p>
     * 例えば、
     * <p>
     * 2026年度カリキュラム
     * ├─ 数学
     * ├─ 物理
     * ├─ プログラミング
     * └─ 情報処理
     * <p>
     * のような一覧を取得します。
     *
     * @param curriculumId カリキュラムID
     * @return カリキュラムに登録された科目一覧
     */
    public List<CurriculumCourse> findByCurriculumId(Integer curriculumId) throws SQLException {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    requirement_type
                FROM curriculum_courses
                WHERE curriculum_id = ?
                ORDER BY course_id
                """;

        List<CurriculumCourse> curriculumCourses = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, curriculumId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    curriculumCourses.add(mapRow(resultSet));
                }
            }
        }

        return curriculumCourses;
    }

    /**
     * 指定した科目が登録されている
     * カリキュラムの一覧を取得します。
     * <p>
     * 同じ科目が複数のカリキュラムで使用されている場合に
     * 利用できます。
     *
     * @param courseId 科目ID
     * @return 科目が登録されているカリキュラム一覧
     */
    public List<CurriculumCourse> findByCourseId(Integer courseId) throws SQLException {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    requirement_type
                FROM curriculum_courses
                WHERE course_id = ?
                ORDER BY curriculum_id
                """;

        List<CurriculumCourse> curriculumCourses = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, courseId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    curriculumCourses.add(mapRow(resultSet));
                }
            }
        }

        return curriculumCourses;
    }

    /**
     * 特定のカリキュラムと科目の組み合わせを取得します。
     * <p>
     * curriculum_coursesには
     * <p>
     * UNIQUE(curriculum_id, course_id)
     * <p>
     * が設定されているため、
     * 1つのカリキュラムに同じ科目を
     * 複数登録することはできません。
     *
     * @param curriculumId カリキュラムID
     * @param courseId     科目ID
     * @return 該当する関係。
     * 存在しない場合はnull。
     */
    public CurriculumCourse findByCurriculumIdAndCourseId(Integer curriculumId, Integer courseId) throws SQLException {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    requirement_type
                FROM curriculum_courses
                WHERE curriculum_id = ?
                  AND course_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, curriculumId);

            statement.setInt(2, courseId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    /**
     * 指定した区分の科目を取得します。
     * <p>
     * 例えば、
     * <p>
     * requirementType = "必修"
     * <p>
     * とした場合、
     * 指定したカリキュラムの必修科目だけを取得できます。
     *
     * @param curriculumId    カリキュラムID
     * @param requirementType 必修・選択などの区分
     * @return 該当する科目一覧
     */
    public List<CurriculumCourse> findByCurriculumIdAndRequirementType(Integer curriculumId, String requirementType) throws SQLException {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    requirement_type
                FROM curriculum_courses
                WHERE curriculum_id = ?
                  AND requirement_type = ?
                ORDER BY course_id
                """;

        List<CurriculumCourse> curriculumCourses = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, curriculumId);

            statement.setString(2, requirementType);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    curriculumCourses.add(mapRow(resultSet));
                }
            }
        }

        return curriculumCourses;
    }

    /**
     * カリキュラムと科目の関係を更新します。
     *
     * @param curriculumCourse 更新する関係
     */
    public void update(CurriculumCourse curriculumCourse) throws SQLException {

        String sql = """
                UPDATE curriculum_courses
                SET
                    curriculum_id = ?,
                    course_id = ?,
                    requirement_type = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, curriculumCourse.getCurriculumId());

            statement.setInt(2, curriculumCourse.getCourseId());

            statement.setString(3, curriculumCourse.getRequirementType());

            statement.setInt(4, curriculumCourse.getId());

            statement.executeUpdate();
        }
    }

    /**
     * IDを指定して関係を削除します。
     * <p>
     * 科目そのものやカリキュラムそのものを
     * 削除する処理ではありません。
     * <p>
     * 「このカリキュラムから、この科目を外す」
     * ために使用します。
     *
     * @param id CurriculumCourseのID
     */
    public void deleteById(Integer id) throws SQLException {

        String sql = """
                DELETE FROM curriculum_courses
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            statement.executeUpdate();
        }
    }

    /**
     * 特定のカリキュラムと科目の組み合わせを削除します。
     * <p>
     * GUIから
     * <p>
     * 「このカリキュラムからこの科目を削除」
     * <p>
     * という操作をする場合に便利です。
     *
     * @param curriculumId カリキュラムID
     * @param courseId     科目ID
     */
    public void deleteByCurriculumIdAndCourseId(Integer curriculumId, Integer courseId) throws SQLException {

        String sql = """
                DELETE FROM curriculum_courses
                WHERE curriculum_id = ?
                  AND course_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, curriculumId);

            statement.setInt(2, courseId);

            statement.executeUpdate();
        }
    }

    /**
     * ResultSetの1行を
     * CurriculumCourseオブジェクトへ変換します。
     */
    private CurriculumCourse mapRow(ResultSet resultSet) throws SQLException {

        Integer id = resultSet.getInt("id");

        Integer curriculumId = resultSet.getInt("curriculum_id");

        Integer courseId = resultSet.getInt("course_id");

        String requirementType = resultSet.getString("requirement_type");

        return new CurriculumCourse(id, curriculumId, courseId, requirementType);
    }
}