package dto;

import java.time.LocalDateTime;

import io.quarkiverse.renarde.dto.AbstractEntityDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for Todo entity.
 * Used for REST API requests/responses instead of direct entity exposure.
 *
 * Benefits of using DTO:
 * - Decouples API from entity structure
 * - Allows selective field exposure
 * - Enables validation at API boundary
 * - Prevents JPA lazy loading issues
 */
public class TodoDTO extends AbstractEntityDTO {

    @NotBlank(message = "Task is required")
    @Size(min = 1, max = 500, message = "Task must be between 1 and 500 characters")
    private String task;

    private boolean completed;

    public TodoDTO() {
    }

    public TodoDTO(String task) {
        this.task = task;
        this.completed = false;
    }

    public TodoDTO(Long id, String task, boolean completed) {
        this.setId(id);
        this.task = task;
        this.completed = completed;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        return "TodoDTO{" +
                "id=" + getId() +
                ", task='" + task + '\'' +
                ", completed=" + completed +
                ", created=" + getCreated() +
                ", updated=" + getUpdated() +
                '}';
    }
}
