package service;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkiverse.renarde.service.AbstractEntityService;
import io.quarkus.panache.common.Sort;
import mapper.TodoMapper;
import model.Todo;
import dto.TodoDTO;

/**
 * Service layer for Todo entity with DTO support.
 * Demonstrates the service layer pattern with DTO mapping.
 *
 * Architecture: REST Controller → Service + DTO → JPA Entity
 *
 * Usage in controller:
 * <pre>
 *   &#64;Inject
 *   TodoService todoService;
 *
 *   public TemplateInstance todos() {
 *       return Templates.todos(todoService.findAllDTOs());
 *   }
 *
 *   public void add(@RestForm String task) {
 *       TodoDTO dto = new TodoDTO(task);
 *       todoService.createFromDTO(dto);
 *   }
 * </pre>
 */
@ApplicationScoped
public class TodoService extends AbstractEntityService<Todo> {

    @Inject
    TodoMapper todoMapper;

    @Override
    protected Class<Todo> getEntityClass() {
        return Todo.class;
    }

    // ========================================================================
    // DTO methods
    // ========================================================================

    /**
     * Find all todos as DTOs.
     *
     * @return list of todo DTOs
     */
    public List<TodoDTO> findAllDTOs() {
        return todoMapper.toDtoList(findAllSorted());
    }

    /**
     * Find todo by id as DTO.
     *
     * @param id the todo id
     * @return optional todo DTO
     */
    public java.util.Optional<TodoDTO> findDTOById(Long id) {
        return findById(id).map(todoMapper::toDto);
    }

    /**
     * Create a new todo from DTO.
     *
     * @param dto the todo DTO
     * @return the created todo DTO
     */
    public TodoDTO createFromDTO(TodoDTO dto) {
        Todo entity = todoMapper.createFromDto(dto);
        persist(entity);
        return todoMapper.toDto(entity);
    }

    /**
     * Update an existing todo from DTO.
     *
     * @param id the todo id
     * @param dto the todo DTO with updated values
     * @return the updated todo DTO, or empty if not found
     */
    public java.util.Optional<TodoDTO> updateFromDTO(Long id, TodoDTO dto) {
        java.util.Optional<Todo> entityOpt = findById(id);
        if (entityOpt.isPresent()) {
            Todo entity = entityOpt.get();
            todoMapper.updateFromDto(dto, entity);
            update(entity);
            return java.util.Optional.of(todoMapper.toDto(entity));
        }
        return java.util.Optional.empty();
    }

    // ========================================================================
    // Entity methods (for backward compatibility)
    // ========================================================================

    /**
     * Find all todos sorted by creation date (newest first).
     *
     * @return list of todos sorted by creation date descending
     */
    public List<Todo> findAllSorted() {
        return findAll(Sort.descending("id"));
    }

    /**
     * Find todos by completion status.
     *
     * @param completed true to find completed todos, false for active
     * @return list of todos with the specified completion status
     */
    public List<Todo> findByCompleted(boolean completed) {
        return find("completed", completed);
    }

    /**
     * Create a new todo with the given task.
     *
     * @param task the task description
     * @return the created todo
     */
    public Todo createTodo(String task) {
        Todo todo = new Todo();
        todo.task = task;
        todo.completed = false;
        persist(todo);
        return todo;
    }

    /**
     * Toggle the completed status of a todo.
     *
     * @param id the todo id
     * @return the updated todo, or empty if not found
     */
    public java.util.Optional<Todo> toggleCompleted(Long id) {
        java.util.Optional<Todo> todoOpt = findById(id);
        if (todoOpt.isPresent()) {
            Todo todo = todoOpt.get();
            todo.completed = !todo.completed;
            update(todo);
            return java.util.Optional.of(todo);
        }
        return java.util.Optional.empty();
    }

    /**
     * Delete a todo by id.
     *
     * @param id the todo id
     * @return true if deleted, false if not found
     */
    public boolean deleteTodo(Long id) {
        return deleteById(id);
    }

    /**
     * Count active (non-completed) todos.
     *
     * @return the count of active todos
     */
    public long countActive() {
        return count("completed", false);
    }

    /**
     * Count completed todos.
     *
     * @return the count of completed todos
     */
    public long countCompleted() {
        return count("completed", true);
    }
}
