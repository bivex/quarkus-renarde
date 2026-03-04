package io.quarkiverse.renarde.dto;

import java.util.List;

import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Generic MapStruct mapper interface for entity-DTO mapping.
 *
 * Extend this interface for your specific mappers:
 * <pre>
 * &#64;Mapper(componentModel = "cdi")
 * public interface TodoMapper extends EntityMapper<Todo, TodoDTO> {
 * }
 * </pre>
 *
 * @param <E> the entity type
 * @param <D> the DTO type
 */
public interface EntityMapper<E, D extends EntityDTO> {

    /**
     * Convert entity to DTO.
     *
     * @param entity the entity to convert
     * @return the converted DTO, or null if entity is null
     */
    D toDto(E entity);

    /**
     * Convert DTO to entity.
     *
     * @param dto the DTO to convert
     * @return the converted entity, or null if DTO is null
     */
    E toEntity(D dto);

    /**
     * Convert list of entities to list of DTOs.
     *
     * @param entities the entities to convert
     * @return the list of converted DTOs
     */
    List<D> toDtoList(List<E> entities);

    /**
     * Convert list of DTOs to list of entities.
     *
     * @param dtos the DTOs to convert
     * @return the list of converted entities
     */
    List<E> toEntityList(List<D> dtos);

    /**
     * Update entity from DTO.
     * Copies non-null values from DTO to existing entity.
     *
     * @param dto the source DTO
     * @param entity the target entity to update
     */
    void updateFromDto(D dto, @MappingTarget E entity);

    /**
     * Create a new entity from DTO.
     * Unlike toEntity(), this always creates a new instance.
     *
     * @param dto the source DTO
     * @return a new entity instance
     */
    E createFromDto(D dto);
}
