package org.takoyaki.curriculummanager.service.abstracts;

import org.takoyaki.curriculummanager.repository.interfaces.CrudRepository;

public abstract class AbstractRepositoryService<T> {
    protected final CrudRepository<T, Integer> repository;

    protected AbstractRepositoryService(CrudRepository<T, Integer> repository) {
        this.repository = repository;
    }

    protected T requireEntity(T entity, String message) {
        if (entity == null) {
            throw new IllegalArgumentException(message);
        }

        return entity;
    }

    protected Integer requireId(Integer id, String message) {
        if (id == null) {
            throw new IllegalArgumentException(message);
        }

        return id;
    }

    protected String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }
}
