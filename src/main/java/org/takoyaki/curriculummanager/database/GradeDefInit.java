package org.takoyaki.curriculummanager.database;

import org.takoyaki.curriculummanager.model.GradeDef;
import org.takoyaki.curriculummanager.repository.GradeDefRepository;
import java.sql.SQLException;

public final class GradeDefInit {
    private GradeDefInit() {
    }

    public static void initialize() {
        GradeDefRepository repository = new GradeDefRepository();
        addIfMissing(repository, new GradeDef("S", 4.0, true, true));
        addIfMissing(repository, new GradeDef("A", 4.0, true, true));
        addIfMissing(repository, new GradeDef("B", 3.0, true, true));
        addIfMissing(repository, new GradeDef("C", 2.0, true, true));
        addIfMissing(repository, new GradeDef("D", 1.0, true, true));
        addIfMissing(repository, new GradeDef("F", 0.0, false, true));
        addIfMissing(repository, new GradeDef("N", null, false, false));
        addIfMissing(repository, new GradeDef("P", null, true, false));
    }

    private static void addIfMissing(GradeDefRepository repository, GradeDef grade) {
        try {
            if (repository.findBySymbol(grade.getSymbol()) == null) {
                repository.save(grade);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
