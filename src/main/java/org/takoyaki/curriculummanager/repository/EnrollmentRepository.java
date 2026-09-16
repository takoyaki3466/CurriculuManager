package org.takoyaki.curriculummanager.repository;

import org.takoyaki.curriculummanager.database.DatabaseManager;
import org.takoyaki.curriculummanager.model.Enrollment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * 履修情報をSQLiteへ保存・取得するRepository。
 * <p>
 * 担当する処理：
 * <p>
 * ・履修情報の保存
 * ・履修情報の取得
 * ・履修情報の更新
 * ・履修情報の削除
 * ・カリキュラム別の取得
 * ・年度・学期別の取得
 * ・履修の重複確認
 */
public class EnrollmentRepository {

    /**
     * 履修情報をデータベースへ保存する。
     * <p>
     * 保存に成功すると、
     * 自動採番されたIDがEnrollmentへ設定される。
     *
     * @param enrollment 保存する履修情報
     */
    public void save(Enrollment enrollment) {

        String sql = """
                INSERT INTO enrollments (
                    curriculum_id,
                    course_id,
                    year,
                    semester,
                    grade_id
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            /*
             * curriculum_id
             *
             * 既存データなどでNULLになる可能性があるため、
             * setNullにも対応する。
             */
            if (enrollment.getCurriculumId() == null) {

                statement.setNull(1, Types.INTEGER);

            } else {

                statement.setInt(1, enrollment.getCurriculumId());
            }

            /*
             * course_id
             */
            statement.setInt(2, enrollment.getCourseId());

            /*
             * year
             */
            statement.setInt(3, enrollment.getYear());

            /*
             * semester
             */
            statement.setString(4, enrollment.getSemester());

            /*
             * grade_id
             *
             * 成績未入力の場合はNULL。
             */
            if (enrollment.getGradeId() == null) {

                statement.setNull(5, Types.INTEGER);

            } else {

                statement.setInt(5, enrollment.getGradeId());
            }

            statement.executeUpdate();

            /*
             * SQLiteが自動生成したIDを取得する。
             */
            try (ResultSet keys = statement.getGeneratedKeys()) {

                if (keys.next()) {

                    enrollment.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException("履修情報の保存に失敗しました。", e);
        }
    }

    /**
     * IDから履修情報を1件取得する。
     *
     * @param id 履修情報ID
     * @return 履修情報。存在しない場合はnull。
     */
    public Enrollment findById(Integer id) {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    year,
                    semester,
                    grade_id
                FROM enrollments
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    return mapRow(result);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException("履修情報の取得に失敗しました。", e);
        }

        return null;
    }

    /**
     * すべての履修情報を取得する。
     * <p>
     * 年度、学期、IDの順に並べる。
     *
     * @return 履修情報一覧
     */
    public List<Enrollment> findAll() {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    year,
                    semester,
                    grade_id
                FROM enrollments
                ORDER BY
                    year,
                    semester,
                    id
                """;

        List<Enrollment> enrollments = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                enrollments.add(mapRow(result));
            }

        } catch (SQLException e) {

            throw new RuntimeException("履修情報一覧の取得に失敗しました。", e);
        }

        return enrollments;
    }

    /**
     * カリキュラムIDから履修情報を取得する。
     * <p>
     * 今後の履修画面・成績画面・卒業判定では、
     * 基本的にこのメソッドを使用する。
     *
     * @param curriculumId カリキュラムID
     * @return 指定カリキュラムの履修情報
     */
    public List<Enrollment> findByCurriculumId(Integer curriculumId) {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    year,
                    semester,
                    grade_id
                FROM enrollments
                WHERE curriculum_id = ?
                ORDER BY
                    year,
                    semester,
                    id
                """;

        List<Enrollment> enrollments = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, curriculumId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    enrollments.add(mapRow(result));
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException("カリキュラム別履修情報の取得に失敗しました。", e);
        }

        return enrollments;
    }

    /**
     * カリキュラムと年度を指定して取得する。
     *
     * @param curriculumId カリキュラムID
     * @param year         年度
     * @return 履修情報一覧
     */
    public List<Enrollment> findByCurriculumIdAndYear(Integer curriculumId, int year) {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    year,
                    semester,
                    grade_id
                FROM enrollments
                WHERE curriculum_id = ?
                  AND year = ?
                ORDER BY
                    semester,
                    id
                """;

        List<Enrollment> enrollments = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, curriculumId);

            statement.setInt(2, year);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    enrollments.add(mapRow(result));
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException("年度別履修情報の取得に失敗しました。", e);
        }

        return enrollments;
    }

    /**
     * カリキュラム・年度・学期を指定して取得する。
     *
     * @param curriculumId カリキュラムID
     * @param year         年度
     * @param semester     学期
     * @return 履修情報一覧
     */
    public List<Enrollment> findByCurriculumIdAndYearAndSemester(Integer curriculumId, int year, String semester) {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    year,
                    semester,
                    grade_id
                FROM enrollments
                WHERE curriculum_id = ?
                  AND year = ?
                  AND semester = ?
                ORDER BY id
                """;

        List<Enrollment> enrollments = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, curriculumId);

            statement.setInt(2, year);

            statement.setString(3, semester);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    enrollments.add(mapRow(result));
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException("学期別履修情報の取得に失敗しました。", e);
        }

        return enrollments;
    }

    /**
     * 科目IDから履修履歴を取得する。
     * <p>
     * 同じ科目を過去に履修したか確認するときなどに使用する。
     *
     * @param courseId 科目ID
     * @return 履修履歴
     */
    public List<Enrollment> findByCourseId(Integer courseId) {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    year,
                    semester,
                    grade_id
                FROM enrollments
                WHERE course_id = ?
                ORDER BY
                    year,
                    semester,
                    id
                """;

        List<Enrollment> enrollments = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, courseId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    enrollments.add(mapRow(result));
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException("科目別履修情報の取得に失敗しました。", e);
        }

        return enrollments;
    }

    /**
     * 履修情報を更新する。
     *
     * @param enrollment 更新後の履修情報
     */
    public void update(Enrollment enrollment) {

        String sql = """
                UPDATE enrollments
                SET
                    curriculum_id = ?,
                    course_id = ?,
                    year = ?,
                    semester = ?,
                    grade_id = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            /*
             * curriculum_id
             */
            if (enrollment.getCurriculumId() == null) {

                statement.setNull(1, Types.INTEGER);

            } else {

                statement.setInt(1, enrollment.getCurriculumId());
            }

            /*
             * course_id
             */
            statement.setInt(2, enrollment.getCourseId());

            /*
             * year
             */
            statement.setInt(3, enrollment.getYear());

            /*
             * semester
             */
            statement.setString(4, enrollment.getSemester());

            /*
             * grade_id
             */
            if (enrollment.getGradeId() == null) {

                statement.setNull(5, Types.INTEGER);

            } else {

                statement.setInt(5, enrollment.getGradeId());
            }

            /*
             * WHERE id
             */
            statement.setInt(6, enrollment.getId());

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException("履修情報の更新に失敗しました。", e);
        }
    }

    /**
     * IDを指定して履修情報を削除する。
     *
     * @param id 履修情報ID
     */
    public void deleteById(Integer id) {

        String sql = """
                DELETE FROM enrollments
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException("履修情報の削除に失敗しました。", e);
        }
    }

    /**
     * 同じカリキュラム・科目・年度・学期の履修が
     * 既に存在するか確認する。
     *
     * @param curriculumId カリキュラムID
     * @param courseId     科目ID
     * @param year         年度
     * @param semester     学期
     * @return 存在する場合true
     */
    public boolean exists(Integer curriculumId, Integer courseId, int year, String semester) {

        String sql = """
                SELECT 1
                FROM enrollments
                WHERE curriculum_id = ?
                  AND course_id = ?
                  AND year = ?
                  AND semester = ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, curriculumId);

            statement.setInt(2, courseId);

            statement.setInt(3, year);

            statement.setString(4, semester);

            try (ResultSet result = statement.executeQuery()) {

                return result.next();
            }

        } catch (SQLException e) {

            throw new RuntimeException("履修情報の重複確認に失敗しました。", e);
        }
    }

    /**
     * ResultSetの1行をEnrollmentへ変換する。
     * <p>
     * SQLiteではNULLのINTEGERをgetInt()すると0になるため、
     * wasNull()を使用してNULLを正しく復元する。
     */
    private Enrollment mapRow(ResultSet result) throws SQLException {

        /*
         * curriculum_id
         */
        int curriculumId = result.getInt("curriculum_id");

        Integer nullableCurriculumId = result.wasNull() ? null : curriculumId;

        /*
         * grade_id
         */
        int gradeId = result.getInt("grade_id");

        Integer nullableGradeId = result.wasNull() ? null : gradeId;

        return new Enrollment(result.getInt("id"), nullableCurriculumId, result.getInt("course_id"), result.getInt("year"), result.getString("semester"), nullableGradeId);
    }
}
