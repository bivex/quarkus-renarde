package mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

import dto.TodoDTO;
import io.quarkiverse.renarde.dto.EntityMapper;
import model.Todo;

/**
 * MapStruct mapper for Todo entity and TodoDTO.
 *
 * MapStruct will generate the implementation at compile time.
 * The mapper is a CDI bean and can be injected.
 *
 * Usage:
 * <pre>
 * &#64;Inject
 * TodoMapper todoMapper;
 *
 * // Create entity from DTO
 * Todo entity = todoMapper.createFromDto(todoDTO);
 *
 * // Convert entity to DTO for response
 * TodoDTO dto = todoMapper.toDto(todo);
 * </pre>
 */
@Mapper(
    componentModel = "cdi",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {} // add custom mapping methods here if needed
)
public interface TodoMapper extends EntityMapper<Todo, TodoDTO> {

    @Override
    TodoDTO toDto(Todo entity);

    @Override
    Todo toEntity(TodoDTO dto);

    @Override
    List<TodoDTO> toDtoList(List<Todo> entities);

    @Override
    List<Todo> toEntityList(List<TodoDTO> dtos);

    @Override
    void updateFromDto(TodoDTO dto, @MappingTarget Todo entity);

    @Override
    default Todo createFromDto(TodoDTO dto) {
        if (dto == null) {
            return null;
        }
        Todo entity = new Todo();
        updateFromDto(dto, entity);
        return entity;
    }

    /**
     * Custom mapping method to update entity from DTO.
     * Only copies non-null values to preserve existing data.
     *
     * @param dto the source DTO
     * @param entity the target entity
     */
    default void updateFromDto(TodoDTO dto, @MappingTarget Todo entity) {
        if (dto == null || entity == null) {
            return;
        }
        if (dto.getTask() != null) {
            entity.task = dto.getTask();
        }
        entity.completed = dto.isCompleted();
    }
}
