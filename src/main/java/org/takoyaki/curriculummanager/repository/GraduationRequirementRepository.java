package org.takoyaki.curriculummanager.repository;

import org.takoyaki.curriculummanager.database.DatabaseManager;
import org.takoyaki.curriculummanager.model.GraduationRequirement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * GraduationRequirementのデータベース操作を担当するRepositoryです。
 * <p>
 * 卒業要件を管理します。
 * <p>
 * 例えば、
 * <p>
 * カリキュラム全体
 * └─ 卒業必要単位       124単位
 * <p>
 * 一般教養
 * └─ 必要単位            20単位
 * <p>
 * 基礎教育
 * └─ 必要単位            30単位
 * <p>
 * 専門科目
 * └─ 必要単位            60単位
 * <p>
 * のような情報を保存できます。
 */
public class GraduationRequirementRepository {

    /**
     * 卒業要件を新規登録します。
     *
     * @param requirement 登録する卒業要件
     * @return IDが設定された卒業要件
     */
    public GraduationRequirement save(GraduationRequirement requirement) throws SQLException {

        String sql = """
                INSERT INTO graduation_requirements (
                    curriculum_id,
                    category_id,
                    name,
                    required_credits
                )
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            /*
             * カリキュラムID
             */
            statement.setInt(1, requirement.getCurriculumId());

            /*
             * カテゴリーID。
             *
             * NULLの場合は、
             * カリキュラム全体に対する卒業要件です。
             */
            if (requirement.getCategoryId() == null) {

                statement.setNull(2, java.sql.Types.INTEGER);

            } else {

                statement.setInt(2, requirement.getCategoryId());
            }

            /*
             * 卒業要件の名前
             */
            statement.setString(3, requirement.getName());

            /*
             * 必要単位数
             */
            statement.setDouble(4, requirement.getRequiredCredits());

            statement.executeUpdate();

            /*
             * 自動生成されたIDを取得します。
             */
            try (ResultSet resultSet = statement.getGeneratedKeys()) {

                if (resultSet.next()) {

                    requirement.setId(resultSet.getInt(1));
                }
            }
        }

        return requirement;
    }

    /**
     * IDを指定して卒業要件を1件取得します。
     *
     * @param id 卒業要件ID
     * @return 該当する卒業要件。
     * 存在しない場合はnull。
     */
    public GraduationRequirement findById(Integer id) throws SQLException {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    category_id,
                    name,
                    required_credits
                FROM graduation_requirements
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
     * すべての卒業要件を取得します。
     * <p>
     * カリキュラムID、カテゴリーID、
     * IDの順番で並べます。
     *
     * @return すべての卒業要件
     */
    public List<GraduationRequirement> findAll() throws SQLException {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    category_id,
                    name,
                    required_credits
                FROM graduation_requirements
                ORDER BY
                    curriculum_id,
                    category_id,
                    id
                """;

        List<GraduationRequirement> requirements = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                requirements.add(mapRow(resultSet));
            }
        }

        return requirements;
    }

    /**
     * 指定したカリキュラムの
     * 卒業要件をすべて取得します。
     * <p>
     * カリキュラム全体の要件と
     * カテゴリー単位の要件の両方が含まれます。
     *
     * @param curriculumId カリキュラムID
     * @return 卒業要件一覧
     */
    public List<GraduationRequirement> findByCurriculumId(Integer curriculumId) throws SQLException {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    category_id,
                    name,
                    required_credits
                FROM graduation_requirements
                WHERE curriculum_id = ?
                ORDER BY
                    category_id,
                    id
                """;

        List<GraduationRequirement> requirements = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, curriculumId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    requirements.add(mapRow(resultSet));
                }
            }
        }

        return requirements;
    }

    /**
     * 指定したカテゴリーの卒業要件を取得します。
     * <p>
     * 例えば、
     * <p>
     * 一般教養 → 20単位必要
     * <p>
     * のようなカテゴリー単位の卒業要件を
     * 取得するために使用します。
     *
     * @param categoryId カテゴリーID
     * @return 該当する卒業要件一覧
     */
    public List<GraduationRequirement> findByCategoryId(Integer categoryId) throws SQLException {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    category_id,
                    name,
                    required_credits
                FROM graduation_requirements
                WHERE category_id = ?
                ORDER BY id
                """;

        List<GraduationRequirement> requirements = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    requirements.add(mapRow(resultSet));
                }
            }
        }

        return requirements;
    }

    /**
     * カリキュラム全体に対する卒業要件だけを取得します。
     * <p>
     * category_idがNULLのレコードを取得します。
     * <p>
     * 例えば、
     * <p>
     * 「卒業には124単位必要」
     * <p>
     * のような要件です。
     *
     * @param curriculumId カリキュラムID
     * @return カリキュラム全体の卒業要件一覧
     */
    public List<GraduationRequirement> findOverallRequirements(Integer curriculumId) throws SQLException {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    category_id,
                    name,
                    required_credits
                FROM graduation_requirements
                WHERE curriculum_id = ?
                  AND category_id IS NULL
                ORDER BY id
                """;

        List<GraduationRequirement> requirements = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, curriculumId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    requirements.add(mapRow(resultSet));
                }
            }
        }

        return requirements;
    }

    /**
     * 卒業要件を更新します。
     *
     * @param requirement 更新する卒業要件
     */
    public void update(GraduationRequirement requirement) throws SQLException {

        String sql = """
                UPDATE graduation_requirements
                SET
                    curriculum_id = ?,
                    category_id = ?,
                    name = ?,
                    required_credits = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, requirement.getCurriculumId());

            /*
             * カテゴリーが存在しない場合はNULL。
             */
            if (requirement.getCategoryId() == null) {

                statement.setNull(2, java.sql.Types.INTEGER);

            } else {

                statement.setInt(2, requirement.getCategoryId());
            }

            statement.setString(3, requirement.getName());

            statement.setDouble(4, requirement.getRequiredCredits());

            statement.setInt(5, requirement.getId());

            statement.executeUpdate();
        }
    }

    /**
     * IDを指定して卒業要件を削除します。
     *
     * @param id 削除する卒業要件ID
     */
    public void deleteById(Integer id) throws SQLException {

        String sql = """
                DELETE FROM graduation_requirements
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
     * GraduationRequirementオブジェクトへ変換します。
     * <p>
     * category_idはNULLになる可能性があるため、
     * wasNull()を使用してNULLを判定します。
     */
    private GraduationRequirement mapRow(ResultSet resultSet) throws SQLException {

        Integer id = resultSet.getInt("id");

        Integer curriculumId = resultSet.getInt("curriculum_id");

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

        String name = resultSet.getString("name");

        double requiredCredits = resultSet.getDouble("required_credits");

        return new GraduationRequirement(id, curriculumId, categoryId, name, requiredCredits);
    }
}