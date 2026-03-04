package io.quarkiverse.renarde.service;

import java.util.List;
import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Page;

/**
 * Abstract base implementation of EntityService for Panache entities.
 * Provides default implementations for common CRUD operations.
 *
 * Subclasses should:
 * - Provide the entity class via getEntityClass()
 * - Override methods as needed for custom business logic
 * - Add custom business methods
 *
 * @param <T> the entity type, must extend PanacheEntity
 */
public abstract class AbstractEntityService<T extends PanacheEntity> implements EntityService<T> {

    /**
     * Get the entity class for this service.
     * Used for generic type safety and static method delegation.
     *
     * @return the entity class
     */
    protected abstract Class<T> getEntityClass();

    /**
     * Get the entity name for queries.
     * Default implementation uses the simple class name.
     *
     * @return the entity name
     */
    protected String getEntityName() {
        return getEntityClass().getSimpleName();
    }

    @Override
    public List<T> findAll() {
        return getEntityClass().listAll();
    }

    @Override
    public List<T> findAll(Sort sort) {
        return getEntityClass().listAll(sort);
    }

    @Override
    public Optional<T> findById(Object id) {
        return getEntityClass().findByIdOptional(id);
    }

    @Override
    public List<T> find(String query) {
        return getEntityClass().list(query);
    }

    @Override
    public List<T> find(String query, Parameters params) {
        return getEntityClass().list(query, params);
    }

    @Override
    public long count() {
        return getEntityClass().count();
    }

    @Override
    public long count(String query) {
        return getEntityClass().count(query);
    }

    @Override
    public long count(String query, Parameters params) {
        return getEntityClass().count(query, params);
    }

    @Override
    public void persist(T entity) {
        entity.persist();
    }

    @Override
    public T update(T entity) {
        entity.persist();
        return entity;
    }

    @Override
    public boolean deleteById(Object id) {
        return getEntityClass().deleteById(id);
    }

    @Override
    public void delete(T entity) {
        entity.delete();
    }

    @Override
    public boolean existsById(Object id) {
        return findById(id).isPresent();
    }

    // ========================================================================
    // Pagination support
    // ========================================================================

    /**
     * Find entities with pagination.
     *
     * @param query the query string
     * @param params the query parameters
     * @param page the page number (0-indexed)
     * @param pageSize the page size
     * @return list of entities for the requested page
     */
    public List<T> findPage(String query, Parameters params, int page, int pageSize) {
        return getEntityClass().find(query, params).page(Page.of(page, pageSize)).list();
    }

    /**
     * Find all entities with pagination.
     *
     * @param page the page number (0-indexed)
     * @param pageSize the page size
     * @return list of entities for the requested page
     */
    public List<T> findAllPage(int page, int pageSize) {
        return getEntityClass().findAll().page(Page.of(page, pageSize)).list();
    }

    /**
     * Find all entities with pagination and sorting.
     *
     * @param sort the sort criteria
     * @param page the page number (0-indexed)
     * @param pageSize the page size
     * @return list of entities for the requested page
     */
    public List<T> findAllPage(Sort sort, int page, int pageSize) {
        return getEntityClass().findAll(sort).page(Page.of(page, pageSize)).list();
    }

    /**
     * Find entities with pagination and sorting.
     *
     * @param query the query string
     * @param sort the sort criteria
     * @param page the page number (0-indexed)
     * @param pageSize the page size
     * @return list of entities for the requested page
     */
    public List<T> findPage(String query, Sort sort, int page, int pageSize) {
        return getEntityClass().find(query, sort).page(Page.of(page, pageSize)).list();
    }
}
