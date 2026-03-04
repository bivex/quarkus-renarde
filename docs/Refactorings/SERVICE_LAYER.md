# Service Layer Architecture

## Overview

Quarkus Renarde now supports a **service layer pattern** to separate business logic from REST controllers.

```
REST Controller → Service → JPA Entity
```

## Benefits

- **Separation of concerns**: Controllers handle HTTP, Services handle business logic
- **Testability**: Services can be unit tested without HTTP layer
- **Reusability**: Business logic can be shared across multiple controllers
- **Maintainability**: Easier to modify business logic without touching controllers

## Base Classes

### EntityService<T>

Interface defining standard CRUD operations:

```java
public interface EntityService<T> {
    List<T> findAll();
    Optional<T> findById(Object id);
    void persist(T entity);
    T update(T entity);
    boolean deleteById(Object id);
    // ... more methods
}
```

### AbstractEntityService<T>

Base implementation with default CRUD operations for Panache entities.

## Creating a Service

```java
@ApplicationScoped
public class TodoService extends AbstractEntityService<Todo> {

    @Override
    protected Class<Todo> getEntityClass() {
        return Todo.class;
    }

    // Custom business methods
    public List<Todo> findActive() {
        return find("completed", false);
    }
}
```

## Using in Controllers

```java
@Path("/todos")
public class Todos extends Controller {

    @Inject
    TodoService todoService;

    @GET
    public TemplateInstance list() {
        return Templates.todos(todoService.findAllSorted());
    }

    @POST
    public void add(@RestForm String task) {
        if(validationFailed()) {
            list();
        }
        todoService.createTodo(task);
        list();
    }
}
```

## Migration from Direct Entity Access

**Before** (direct access):
```java
public void add(String task) {
    Todo todo = new Todo();
    todo.task = task;
    todo.persist();
    todos();
}
```

**After** (service layer):
```java
@Inject
TodoService todoService;

public void add(String task) {
    todoService.createTodo(task);
    todos();
}
```

## Available Methods

From `AbstractEntityService`:

- `findAll()` / `findAll(Sort)`
- `findById(Object id)`
- `find(String query)` / `find(String query, Parameters params)`
- `persist(T entity)`
- `update(T entity)`
- `deleteById(Object id)` / `delete(T entity)`
- `count()` / `count(String query)`
- `existsById(Object id)`

Pagination support:
- `findAllPage(int page, int pageSize)`
- `findPage(String query, Parameters params, int page, int pageSize)`

## Transaction Management

Services are `@ApplicationScoped` by default. Use `@Transactional` for methods that require transactions:

```java
@ApplicationScoped
public class OrderService extends AbstractEntityService<Order> {

    @Transactional
    public Order createOrderWithItems(OrderDTO dto) {
        Order order = new Order();
        persist(order);
        // ... add items
        return order;
    }
}
```
