package rest;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.CheckedTemplate;
import io.quarkiverse.renarde.Controller;
import dto.TodoDTO;
import model.Todo;
import service.TodoService;

/**
 * REST controller using Service + DTO pattern.
 *
 * Architecture:
 *   REST Controller → Service (DTO) → JPA Entity
 *
 * Benefits:
 * - Controllers handle HTTP only
 * - Services contain business logic
 * - DTOs isolate entities from API
 */
public class Todos extends Controller {

    @Inject
    TodoService todoService;

    /**
     * Qute templates available in src/main/resources/templates/Classname/method.html
     */
    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance index();
        public static native TemplateInstance todos(List<TodoDTO> todos);
        public static native TemplateInstance todo(TodoDTO todo);
    }

    @Path("/renarde")
    public TemplateInstance index() {
        return Templates.index();
    }

    /**
     * List all todos using DTOs.
     */
    @GET
    public TemplateInstance todos() {
        List<TodoDTO> todos = todoService.findAllDTOs();
        return Templates.todos(todos);
    }

    /**
     * Create a new todo from form data.
     * Uses DTO for request handling.
     */
    @POST
    public void add(@RestForm @NotBlank String task) {
        if (validationFailed()) {
            todos();
            return;
        }
        // Use DTO pattern
        TodoDTO dto = new TodoDTO(task);
        todoService.createFromDTO(dto);
        todos();
    }

    /**
     * View a single todo.
     */
    @Path("/{id}")
    public TemplateInstance view(@RestPath Long id) {
        return todoService.findDTOById(id)
                .map(Templates::todo)
                .orElse(Templates.todos(todoService.findAllDTOs()));
    }

    /**
     * Toggle todo completion status.
     */
    @POST
    @Path("/{id}/toggle")
    public void toggle(@RestPath Long id) {
        todoService.toggleCompleted(id);
        todos();
    }

    /**
     * Delete a todo.
     */
    @DELETE
    @Path("/{id}")
    public void delete(@RestPath Long id) {
        todoService.deleteTodo(id);
        todos();
    }

    /**
     * Update a todo.
     */
    @PUT
    @Path("/{id}")
    public void update(@RestPath Long id, @RestForm String task, @RestForm boolean completed) {
        TodoDTO dto = new TodoDTO();
        dto.setTask(task);
        dto.setCompleted(completed);
        todoService.updateFromDTO(id, dto);
        todos();
    }
}
