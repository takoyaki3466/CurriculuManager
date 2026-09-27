package org.takoyaki.curriculummanager.service.abstracts;

import org.takoyaki.curriculummanager.repository.interfaces.CrudRepository;

public abstract class AbstractRepositoryService<T> {
    protected final CrudRepository<T, Integer> repository;

    protected AbstractRepositoryService(CrudRepository<T, Integer> repository) {
        this.repository = repository;
    }

}
