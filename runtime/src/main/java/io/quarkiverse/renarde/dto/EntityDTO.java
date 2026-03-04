package io.quarkiverse.renarde.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base interface for all Entity DTOs.
 * Provides common metadata fields for entity representations.
 *
 * Implementations should be simple POJOs with:
 * - Private fields
 * - Getters/setters or public fields
 * - Proper validation annotations
 */
public interface EntityDTO {

    /**
     * Get the entity ID.
     *
     * @return the entity ID, or null for new entities
     */
    Long getId();

    /**
     * Set the entity ID.
     *
     * @param id the entity ID
     */
    void setId(Long id);

    /**
     * Get the creation timestamp.
     *
     * @return the creation timestamp, or null if not set
     */
    LocalDateTime getCreated();

    /**
     * Set the creation timestamp.
     *
     * @param created the creation timestamp
     */
    void setCreated(LocalDateTime created);

    /**
     * Get the last update timestamp.
     *
     * @return the last update timestamp, or null if not set
     */
    LocalDateTime getUpdated();

    /**
     * Set the last update timestamp.
     *
     * @param updated the last update timestamp
     */
    void setUpdated(LocalDateTime updated);

    /**
     * Get the version for optimistic locking.
     *
     * @return the version number, or null if not set
     */
    Long getVersion();

    /**
     * Set the version for optimistic locking.
     *
     * @param version the version number
     */
    void setVersion(Long version);

    /**
     * Check if this is a new entity (not yet persisted).
     *
     * @return true if ID is null, false otherwise
     */
    default boolean isNew() {
        return getId() == null;
    }
}
