package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.repository.DepartmentRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * 学部に関する処理をまとめるService。
 *
 * <p>
 * ControllerからDepartmentRepositoryを直接操作せず、
 * 学部に関するデータ取得・登録・更新・削除を
 * このServiceに集約します。
 * </p>
 */
public class DepartmentService {

    /**
     * 学部Repository。
     */
    private final DepartmentRepository repository;

    /**
     * Serviceを生成します。
     */
    public DepartmentService() {

        repository = new DepartmentRepository();
    }

    /**
     * 学部一覧を取得します。
     *
     * @return 学部一覧
     */
    public List<Department> getDepartments() throws SQLException {

        return repository.findAll();
    }

    /**
     * IDから学部を取得します。
     *
     * @param id 学部ID
     * @return 学部。存在しない場合はnull
     */
    public Department getDepartment(Integer id) throws SQLException {

        if (id == null) {

            throw new IllegalArgumentException("学部が指定されていません。");
        }

        return repository.findById(id);
    }

    /**
     * 学部を追加します。
     *
     * @param department 追加する学部
     */
    public void addDepartment(Department department) throws SQLException {

        validateDepartment(department);

        /*
         * 新規登録時にはIDを指定する必要はありません。
         */
        repository.save(department);
    }

    /**
     * 学部を更新します。
     *
     * @param department 更新する学部
     */
    public void updateDepartment(Department department) throws SQLException {

        if (department == null || department.getId() == null) {

            throw new IllegalArgumentException("更新する学部が指定されていません。");
        }

        validateDepartment(department);

        repository.update(department);
    }

    /**
     * 学部を削除します。
     *
     * <p>
     * 学部を削除すると、データベース側の
     * 外部キー設定により所属する学科も削除されます。
     * </p>
     *
     * @param id 削除する学部ID
     */
    public void deleteDepartment(Integer id) throws SQLException {

        if (id == null) {

            throw new IllegalArgumentException("削除する学部が指定されていません。");
        }

        repository.deleteById(id);
    }

    /**
     * 学部の入力内容を検証します。
     *
     * @param department 検証する学部
     */
    private void validateDepartment(Department department) {

        if (department == null) {

            throw new IllegalArgumentException("学部が指定されていません。");
        }

        if (department.getName() == null || department.getName().isBlank()) {

            throw new IllegalArgumentException("学部名を入力してください。");
        }

        /*
         * 前後の空白を除いた状態で登録・更新するため、
         * 空白だけの名前は上記のチェックで弾きます。
         */
        department.setName(department.getName().trim());
    }
}
