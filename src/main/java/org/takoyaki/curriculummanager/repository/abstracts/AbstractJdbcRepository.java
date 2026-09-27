package org.takoyaki.curriculummanager.repository.abstracts;

import org.takoyaki.curriculummanager.repository.interfaces.CrudRepository;

public abstract class AbstractJdbcRepository<T> implements CrudRepository<T, Integer> {
}
