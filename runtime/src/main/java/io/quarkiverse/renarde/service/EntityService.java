package io.quarkiverse.renarde.service;

import java.util.List;
import java.util.Optional;

import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Parameters;

/**
 * Base service interface for entity operations.
 * Provides abstraction layer between REST controllers and JPA entities.
 *
 * @param <T> the entity type
 */
public interface EntityService<T> {

    /**
     * Find all entities.
     *
     * @return list of all entities
     */
    List<T> findAll();

    /**
     * Find all entities with sorting.
     *
     * @param sort the sort criteria
     * @return list of all entities sorted
     */
    List<T> findAll(Sort sort);

    /**
     * Find entity by id.
     *
     * @param id the entity id
     * @return optional containing the entity, or empty if not found
     */
    Optional<T> findById(Object id);

    /**
     * Find entities with a query.
     *
     * @param query the query string
     * @return list of entities matching the query
     */
    List<T> find(String query);

    /**
     * Find entities with a named query.
     *
     * @param query the named query
     * @param params the query parameters
     * @return list of entities matching the query
     */
    List<T> find(String query, Parameters params);

    /**
     * Count all entities.
     *
     * @return the total count
     */
    long count();

    /**
     * Count entities with a query.
     *
     * @param query the query string
     * @return the count of entities matching the query
     */
    long count(String query);

    /**
     * Count entities with a named query.
     *
     * @param query the named query
     * @param params the query parameters
     * @return the count of entities matching the query
     */
    long count(String query, Parameters params);

    /**
     * Persist a new entity.
     *
     * @param entity the entity to persist
     */
    void persist(T entity);

    /**
     * Update an existing entity.
     *
     * @param entity the entity to update
     * @return the updated entity
     */
    T update(T entity);

    /**
     * Delete an entity by id.
     *
     * @param id the entity id
     * @return true if deleted, false if not found
     */
    boolean deleteById(Object id);

    /**
     * Delete an entity.
     *
     * @param entity the entity to delete
     */
    void delete(T entity);

    /**
     * Check if an entity exists by id.
     *
     * @param id the entity id
     * @return true if exists, false otherwise
     */
    boolean existsById(Object id);
}
