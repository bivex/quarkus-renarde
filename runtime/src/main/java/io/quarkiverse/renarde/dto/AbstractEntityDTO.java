package io.quarkiverse.renarde.dto;

import java.time.LocalDateTime;

/**
 * Abstract base class for Entity DTOs with common metadata fields.
 * Extend this class for your DTOs to inherit standard entity metadata.
 *
 * Example usage:
 * <pre>
 * public class TodoDTO extends AbstractEntityDTO {
 *     private String task;
 *     private boolean completed;
 *     // getters/setters
 * }
 * </pre>
 */
public abstract class AbstractEntityDTO implements EntityDTO {

    private Long id;
    private LocalDateTime created;
    private LocalDateTime updated;
    private Long version;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public LocalDateTime getCreated() {
        return created;
    }

    @Override
    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    @Override
    public LocalDateTime getUpdated() {
        return updated;
    }

    @Override
    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    @Override
    public Long getVersion() {
        return version;
    }

    @Override
    public void setVersion(Long version) {
        this.version = version;
    }
}
