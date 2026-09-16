package org.takoyaki.curriculummanager.repository;

import org.takoyaki.curriculummanager.database.DatabaseManager;
import org.takoyaki.curriculummanager.model.GradeDef;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * GradeDefのデータベース操作を担当するRepositoryです。
 * <p>
 * このクラスでは、
 * <p>
 * ・成績の追加
 * ・成績の取得
 * ・成績一覧の取得
 * ・GPA対象成績の取得
 * ・成績の更新
 * ・成績の削除
 * <p>
 * など、grade_definitionsテーブルに対する操作を行います。
 * <p>
 * GPA計算などのビジネスロジックは、
 * このクラスではなくService側で担当します。
 */
public class GradeDefRepository {

    /**
     * 成績をデータベースに登録します。
     * <p>
     * 登録後、生成されたIDをGradeDefに設定します。
     *
     * @param gradeDef 登録する成績
     * @return IDが設定されたGradeDef
     */
    public GradeDef save(GradeDef gradeDef) throws SQLException {

        String sql = """
                INSERT INTO grade_definitions (
                    symbol,
                    gpa_points,
                    passed,
                    included_in_gpa
                )
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            /*
             * 成績記号
             *
             * 例:
             * S
             * A
             * B
             * C
             * D
             * N
             */
            statement.setString(1, gradeDef.getSymbol());

            /*
             * GPAポイントはNULLを許可しています。
             *
             * 例えば「N」のように
             * GPA計算対象外の成績では、
             * gpa_pointsをNULLにできます。
             */
            if (gradeDef.getGpaPoints() == null) {

                statement.setNull(2, java.sql.Types.REAL);

            } else {

                statement.setDouble(2, gradeDef.getGpaPoints());
            }

            /*
             * SQLiteではbooleanを
             * INTEGERとして保存します。
             *
             * true  → 1
             * false → 0
             */
            statement.setInt(3, gradeDef.isPassed() ? 1 : 0);

            statement.setInt(4, gradeDef.isIncludedInGpa() ? 1 : 0);

            statement.executeUpdate();

            /*
             * SQLiteが自動生成したIDを取得します。
             */
            try (ResultSet resultSet = statement.getGeneratedKeys()) {

                if (resultSet.next()) {

                    gradeDef.setId(resultSet.getInt(1));
                }
            }
        }

        return gradeDef;
    }

    /**
     * IDから成績を取得します。
     *
     * @param id 成績ID
     * @return 該当する成績。存在しない場合はnull
     */
    public GradeDef findById(Integer id) throws SQLException {

        String sql = """
                SELECT
                    id,
                    symbol,
                    gpa_points,
                    passed,
                    included_in_gpa
                FROM grade_definitions
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
     * すべての成績を取得します。
     * <p>
     * 成績記号順ではなくID順で取得します。
     * <p>
     * 例えば初期データでは、
     * <p>
     * S
     * A
     * B
     * C
     * D
     * N
     * <p>
     * の登録順になります。
     *
     * @return 成績一覧
     */
    public List<GradeDef> findAll() throws SQLException {

        String sql = """
                SELECT
                    id,
                    symbol,
                    gpa_points,
                    passed,
                    included_in_gpa
                FROM grade_definitions
                ORDER BY id
                """;

        List<GradeDef> grades = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                grades.add(mapRow(resultSet));
            }
        }

        return grades;
    }

    /**
     * 成績記号から成績を取得します。
     * <p>
     * 例:
     * <p>
     * findBySymbol("A")
     *
     * @param symbol 成績記号
     * @return 該当する成績。存在しない場合はnull
     */
    public GradeDef findBySymbol(String symbol) throws SQLException {

        String sql = """
                SELECT
                    id,
                    symbol,
                    gpa_points,
                    passed,
                    included_in_gpa
                FROM grade_definitions
                WHERE symbol = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, symbol);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    /**
     * GPA計算対象の成績だけを取得します。
     * <p>
     * 例えば、
     * <p>
     * S
     * A
     * B
     * C
     * D
     * <p>
     * はGPA対象、
     * <p>
     * N
     * <p>
     * はGPA対象外、
     * という設定にできます。
     *
     * @return GPA対象の成績一覧
     */
    public List<GradeDef> findIncludedInGpa() throws SQLException {

        String sql = """
                SELECT
                    id,
                    symbol,
                    gpa_points,
                    passed,
                    included_in_gpa
                FROM grade_definitions
                WHERE included_in_gpa = 1
                ORDER BY id
                """;

        List<GradeDef> grades = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                grades.add(mapRow(resultSet));
            }
        }

        return grades;
    }

    /**
     * 成績を更新します。
     *
     * @param gradeDef 更新する成績
     */
    public void update(GradeDef gradeDef) throws SQLException {

        String sql = """
                UPDATE grade_definitions
                SET
                    symbol = ?,
                    gpa_points = ?,
                    passed = ?,
                    included_in_gpa = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, gradeDef.getSymbol());

            /*
             * gpa_pointsはNULLを許可しています。
             */
            if (gradeDef.getGpaPoints() == null) {

                statement.setNull(2, java.sql.Types.REAL);

            } else {

                statement.setDouble(2, gradeDef.getGpaPoints());
            }

            /*
             * boolean → SQLite INTEGER
             */
            statement.setInt(3, gradeDef.isPassed() ? 1 : 0);

            statement.setInt(4, gradeDef.isIncludedInGpa() ? 1 : 0);

            statement.setInt(5, gradeDef.getId());

            statement.executeUpdate();
        }
    }

    /**
     * IDを指定して成績を削除します。
     * <p>
     * enrollmentsからこの成績を参照している場合、
     * 外部キーのON DELETE SET NULLによって
     * enrollment側のgrade_idだけがNULLになります。
     * <p>
     * 履修記録そのものは削除されません。
     *
     * @param id 削除する成績ID
     */
    public void deleteById(Integer id) throws SQLException {

        String sql = """
                DELETE FROM grade_definitions
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            statement.executeUpdate();
        }
    }

    /**
     * ResultSetの1行をGradeDefへ変換します。
     * <p>
     * SQLiteではbooleanがINTEGERとして保存されているため、
     * <p>
     * 0 → false
     * 1 → true
     * <p>
     * として読み込みます。
     */
    private GradeDef mapRow(ResultSet resultSet) throws SQLException {

        Integer id = resultSet.getInt("id");

        String symbol = resultSet.getString("symbol");

        /*
         * gpa_pointsはNULL可能です。
         *
         * getDouble()だけでは、
         * NULLの場合も0.0として取得されるため、
         * wasNull()で確認します。
         */
        double gpaPointsValue = resultSet.getDouble("gpa_points");

        Double gpaPoints;

        if (resultSet.wasNull()) {
            gpaPoints = null;
        } else {
            gpaPoints = gpaPointsValue;
        }

        /*
         * SQLite INTEGER → boolean
         */
        boolean passed = resultSet.getInt("passed") != 0;

        boolean includedInGpa = resultSet.getInt("included_in_gpa") != 0;

        return new GradeDef(id, symbol, gpaPoints, passed, includedInGpa);
    }
}
