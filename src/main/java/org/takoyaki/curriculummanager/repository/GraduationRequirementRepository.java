package org.takoyaki.curriculummanager.repository;

import org.takoyaki.curriculummanager.database.DatabaseManager;
import org.takoyaki.curriculummanager.model.GraduationRequirement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * GraduationRequirementのデータベース操作を担当するRepositoryです。
 *
 * <p>
 * 卒業要件は、
 * </p>
 *
 * <ul>
 *     <li>カリキュラム全体</li>
 *     <li>1つのカテゴリ</li>
 *     <li>複数のカテゴリ</li>
 * </ul>
 *
 * <p>
 * を対象として設定できます。
 * </p>
 *
 * <p>
 * カテゴリとの関連は
 * graduation_requirement_categories
 * テーブルで管理します。
 * </p>
 *
 * <p>
 * graduation_requirements.category_id は
 * 旧データとの互換性維持のため残しており、
 * 複数カテゴリが設定されている場合は
 * 先頭カテゴリのみ保存します。
 * </p>
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

        try (Connection connection = DatabaseManager.getConnection()) {

            connection.setAutoCommit(false);

            try {

                try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                    /*
                     * カリキュラムID
                     */
                    statement.setInt(1, requirement.getCurriculumId());

                    /*
                     * 旧category_idとの互換性維持用です。
                     *
                     * 複数カテゴリの場合は
                     * 先頭のカテゴリだけを保存します。
                     *
                     * 全体対象の場合はNULLになります。
                     */
                    setLegacyCategoryId(statement, 2, requirement);

                    /*
                     * 卒業要件名
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

                        } else {

                            throw new SQLException("卒業要件のIDを取得できませんでした。");
                        }
                    }
                }

                /*
                 * 複数カテゴリを中間テーブルへ保存します。
                 */
                insertCategoryRelations(connection, requirement);

                connection.commit();

                return requirement;

            } catch (SQLException e) {

                connection.rollback();

                throw e;

            } finally {

                connection.setAutoCommit(true);
            }
        }
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

                    return mapRow(connection, resultSet);
                }
            }
        }

        return null;
    }

    /**
     * すべての卒業要件を取得します。
     *
     * @return すべての卒業要件
     */
    public List<GraduationRequirement> findAll() throws SQLException {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    name,
                    required_credits
                FROM graduation_requirements
                ORDER BY
                    curriculum_id,
                    id
                """;

        List<GraduationRequirement> requirements = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                requirements.add(mapRow(connection, resultSet));
            }
        }

        return requirements;
    }

    /**
     * 指定したカリキュラムの
     * 卒業要件をすべて取得します。
     *
     * <p>
     * カリキュラム全体の要件、
     * 単一カテゴリの要件、
     * 複数カテゴリの要件の
     * すべてが含まれます。
     * </p>
     *
     * @param curriculumId カリキュラムID
     * @return 卒業要件一覧
     */
    public List<GraduationRequirement> findByCurriculumId(Integer curriculumId) throws SQLException {

        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    name,
                    required_credits
                FROM graduation_requirements
                WHERE curriculum_id = ?
                ORDER BY id
                """;

        List<GraduationRequirement> requirements = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, curriculumId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    requirements.add(mapRow(connection, resultSet));
                }
            }
        }

        return requirements;
    }

    /**
     * 指定したカテゴリを対象に含む卒業要件を取得します。
     *
     * <p>
     * 単一カテゴリ要件だけでなく、
     * 複数カテゴリ要件の中に指定カテゴリが
     * 含まれている場合も取得できます。
     * </p>
     *
     * @param categoryId カテゴリID
     * @return 該当する卒業要件一覧
     */
    public List<GraduationRequirement> findByCategoryId(Integer categoryId) throws SQLException {

        String sql = """
                SELECT DISTINCT
                    gr.id,
                    gr.curriculum_id,
                    gr.name,
                    gr.required_credits
                FROM graduation_requirements gr
                
                INNER JOIN graduation_requirement_categories grc
                    ON gr.id = grc.requirement_id
                
                WHERE grc.category_id = ?
                
                ORDER BY gr.id
                """;

        List<GraduationRequirement> requirements = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    requirements.add(mapRow(connection, resultSet));
                }
            }
        }

        return requirements;
    }

    /**
     * カリキュラム全体を対象とする卒業要件を取得します。
     *
     * <p>
     * 新しい構造では、
     * graduation_requirement_categories に
     * 関連カテゴリが1件も存在しない卒業要件を
     * 「すべて」と判定します。
     * </p>
     *
     * @param curriculumId カリキュラムID
     * @return カリキュラム全体の卒業要件一覧
     */
    public List<GraduationRequirement> findOverallRequirements(Integer curriculumId) throws SQLException {

        String sql = """
                SELECT
                    gr.id,
                    gr.curriculum_id,
                    gr.name,
                    gr.required_credits
                FROM graduation_requirements gr
                
                WHERE gr.curriculum_id = ?
                
                  AND NOT EXISTS (
                      SELECT 1
                      FROM graduation_requirement_categories grc
                      WHERE grc.requirement_id = gr.id
                  )
                
                ORDER BY gr.id
                """;

        List<GraduationRequirement> requirements = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, curriculumId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    requirements.add(mapRow(connection, resultSet));
                }
            }
        }

        return requirements;
    }

    /**
     * 卒業要件を更新します。
     *
     * <p>
     * 基本情報を更新したあと、
     * 既存のカテゴリ関連を削除して
     * 現在設定されているカテゴリを登録し直します。
     * </p>
     *
     * @param requirement 更新する卒業要件
     */
    public void update(GraduationRequirement requirement) throws SQLException {

        if (requirement.getId() == null) {

            throw new IllegalArgumentException("更新する卒業要件のIDが設定されていません。");
        }

        String sql = """
                UPDATE graduation_requirements
                SET
                    curriculum_id = ?,
                    category_id = ?,
                    name = ?,
                    required_credits = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection()) {

            connection.setAutoCommit(false);

            try {

                try (PreparedStatement statement = connection.prepareStatement(sql)) {

                    statement.setInt(1, requirement.getCurriculumId());

                    /*
                     * 旧category_idとの互換性維持用。
                     */
                    setLegacyCategoryId(statement, 2, requirement);

                    statement.setString(3, requirement.getName());

                    statement.setDouble(4, requirement.getRequiredCredits());

                    statement.setInt(5, requirement.getId());

                    int updated = statement.executeUpdate();

                    if (updated == 0) {

                        throw new SQLException("更新対象の卒業要件が存在しません。ID: " + requirement.getId());
                    }
                }

                /*
                 * 古いカテゴリ関連を削除します。
                 */
                deleteCategoryRelations(connection, requirement.getId());

                /*
                 * 現在のカテゴリを登録し直します。
                 */
                insertCategoryRelations(connection, requirement);

                connection.commit();

            } catch (SQLException e) {

                connection.rollback();

                throw e;

            } finally {

                connection.setAutoCommit(true);
            }
        }
    }

    /**
     * IDを指定して卒業要件を削除します。
     *
     * <p>
     * graduation_requirement_categories は
     * ON DELETE CASCADE のため、
     * 卒業要件を削除すると関連データも削除されます。
     * </p>
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
     * 卒業要件とカテゴリの関連を登録します。
     *
     * <p>
     * categoryIdsが空の場合は
     * 「すべて」を意味するため、
     * 何も登録しません。
     * </p>
     */
    private void insertCategoryRelations(Connection connection, GraduationRequirement requirement) throws SQLException {

        List<Integer> categoryIds = normalizeCategoryIds(requirement.getCategoryIds());

        if (categoryIds.isEmpty()) {

            return;
        }

        String sql = """
                INSERT OR IGNORE INTO
                graduation_requirement_categories (
                    requirement_id,
                    category_id
                )
                VALUES (?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            for (Integer categoryId : categoryIds) {

                statement.setInt(1, requirement.getId());

                statement.setInt(2, categoryId);

                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    /**
     * 指定した卒業要件のカテゴリ関連を
     * すべて削除します。
     */
    private void deleteCategoryRelations(Connection connection, Integer requirementId) throws SQLException {

        String sql = """
                DELETE FROM graduation_requirement_categories
                WHERE requirement_id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, requirementId);

            statement.executeUpdate();
        }
    }

    /**
     * 卒業要件に関連付けられている
     * カテゴリID一覧を取得します。
     */
    private List<Integer> findCategoryIds(Connection connection, Integer requirementId) throws SQLException {

        String sql = """
                SELECT category_id
                FROM graduation_requirement_categories
                WHERE requirement_id = ?
                ORDER BY id
                """;

        List<Integer> categoryIds = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, requirementId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    categoryIds.add(resultSet.getInt("category_id"));
                }
            }
        }

        return categoryIds;
    }

    /**
     * 旧graduation_requirements.category_idへ
     * 互換性用の値を設定します。
     *
     * <p>
     * カテゴリが1件以上存在する場合は
     * 先頭のカテゴリIDを保存します。
     * </p>
     *
     * <p>
     * カテゴリが存在しない場合はNULLを保存します。
     * </p>
     */
    private void setLegacyCategoryId(PreparedStatement statement, int parameterIndex, GraduationRequirement requirement) throws SQLException {

        List<Integer> categoryIds = normalizeCategoryIds(requirement.getCategoryIds());

        if (categoryIds.isEmpty()) {

            statement.setNull(parameterIndex, Types.INTEGER);

            return;
        }

        statement.setInt(parameterIndex, categoryIds.get(0));
    }

    /**
     * カテゴリID一覧から
     * nullと重複を除去します。
     *
     * <p>
     * LinkedHashSetを使用することで、
     * 選択順序は維持します。
     * </p>
     */
    private List<Integer> normalizeCategoryIds(List<Integer> categoryIds) {

        if (categoryIds == null || categoryIds.isEmpty()) {

            return new ArrayList<>();
        }

        Set<Integer> uniqueIds = new LinkedHashSet<>();

        for (Integer categoryId : categoryIds) {

            if (categoryId != null) {

                uniqueIds.add(categoryId);
            }
        }

        return new ArrayList<>(uniqueIds);
    }

    /**
     * ResultSetの1行を
     * GraduationRequirementへ変換します。
     *
     * <p>
     * カテゴリについては
     * graduation_requirement_categoriesから
     * すべて取得します。
     * </p>
     */
    private GraduationRequirement mapRow(Connection connection, ResultSet resultSet) throws SQLException {

        Integer id = resultSet.getInt("id");

        Integer curriculumId = resultSet.getInt("curriculum_id");

        String name = resultSet.getString("name");

        double requiredCredits = resultSet.getDouble("required_credits");

        List<Integer> categoryIds = findCategoryIds(connection, id);

        return new GraduationRequirement(id, curriculumId, categoryIds, name, requiredCredits);
    }
}