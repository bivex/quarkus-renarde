# DTO Layer with MapStruct

## Overview

Quarkus Renarde supports **Data Transfer Objects (DTO)** pattern to isolate REST API from JPA entities.

## Architecture

```
REST Controller → DTO → Service → Entity → JPA
     ↓              ↓         ↓
 Validation    MapStruct   Business Logic
```

## Benefits

| Benefit | Description |
|---------|-------------|
| **API Isolation** | Entity changes don't break API |
| **Security** | Selective field exposure |
| **Validation** | API boundary validation |
| **Performance** | Avoid lazy loading issues |
| **Flexibility** | Different DTOs for different use cases |

## Base Classes

### EntityDTO

Interface defining common entity metadata:

```java
public interface EntityDTO {
    Long getId();
    LocalDateTime getCreated();
    LocalDateTime getUpdated();
    Long getVersion();
    boolean isNew();
}
```

### AbstractEntityDTO

Base class with standard metadata fields:

```java
public class TodoDTO extends AbstractEntityDTO {
    private String task;
    private boolean completed;
    // getters/setters
}
```

### EntityMapper<E, D>

Generic MapStruct interface:

```java
public interface EntityMapper<E, D extends EntityDTO> {
    D toDto(E entity);
    E toEntity(D dto);
    List<D> toDtoList(List<E> entities);
    void updateFromDto(D dto, @MappingTarget E entity);
    E createFromDto(D dto);
}
```

## Creating a DTO

```java
public class TodoDTO extends AbstractEntityDTO {

    @NotBlank(message = "Task is required")
    @Size(min = 1, max = 500)
    private String task;

    private boolean completed;

    // getters/setters
}
```

## Creating a Mapper

```java
@Mapper(componentModel = "cdi")
public interface TodoMapper extends EntityMapper<Todo, TodoDTO> {
    // MapStruct generates implementation at compile time
}
```

## Using DTOs in Service

```java
@ApplicationScoped
public class TodoService extends AbstractEntityService<Todo> {

    @Inject
    TodoMapper mapper;

    public List<TodoDTO> findAllDTOs() {
        return mapper.toDtoList(findAll());
    }

    public TodoDTO createFromDTO(TodoDTO dto) {
        Todo entity = mapper.createFromDto(dto);
        persist(entity);
        return mapper.toDto(entity);
    }
}
```

## Using in Controller

```java
@Path("/todos")
public class Todos extends Controller {

    @Inject
    TodoService todoService;

    @GET
    public List<TodoDTO> list() {
        return todoService.findAllDTOs();
    }

    @POST
    public Response create(@Valid TodoDTO dto) {
        TodoDTO created = todoService.createFromDTO(dto);
        return Response.created(created).build();
    }
}
```

## MapStruct Configuration

Add to `pom.xml`:

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
```

MapStruct generates implementations at **compile time**.

## Migration from Direct Entity Access

**Before** (entity in API):
```java
@GET
public List<Todo> list() {
    return Todo.listAll(); // Returns entities directly
}
```

**After** (DTO pattern):
```java
@GET
public List<TodoDTO> list() {
    return todoService.findAllDTOs(); // Returns DTOs
}
```

## Best Practices

1. **One DTO per use case**: Create DTOs for specific needs (ListDTO, DetailDTO, CreateDTO)
2. **Validate DTOs**: Use `@Valid` in controller methods
3. **Keep DTOs simple**: Plain POJOs, no business logic
4. **Use MapStruct**: Automatic mapping, compile-time validation
5. **Version DTOs**: Maintain API compatibility with versioned DTOs

## Custom Mapping

For complex mappings, add methods to mapper:

```java
@Mapper(componentModel = "cdi")
public interface TodoMapper extends EntityMapper<Todo, TodoDTO> {

    @Mapping(target = "created", dateFormat = "ISO_LOCAL_DATE_TIME")
    TodoDTO toDto(Todo entity);

    default String formatTask(Todo entity) {
        return entity.task.toUpperCase();
    }
}
```

## Testing

```java
@QuarkusTest
public class TodoMapperTest {

    @Inject
    TodoMapper mapper;

    @Test
    public void testToDto() {
        Todo entity = new Todo();
        entity.task = "Test";
        entity.persist();

        TodoDTO dto = mapper.toDto(entity);

        assertEquals("Test", dto.getTask());
    }
}
```
