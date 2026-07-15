package es.caib.invai.api.service.mapper;

import es.caib.invai.api.persistence.model.StatusEntity;
import es.caib.invai.api.service.model.Status;
import es.caib.invai.api.service.model.StatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

/**
 * MapStruct translation utility specializing in state conversions across lifecycle tracking entities,
 * business model wrappers, and standardized system state enumeration keys.
 *
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface StatusMapper {

    /**
     * Converts a persistent status relational row model into a standard business status entity block.
     *
     * @param entity structural relational entity row model source
     * @return the matching business status wrapper model
     */
    Status toModel(StatusEntity entity);

    /**
     * Converts a business status layout container down into a relational schema entity structure.
     *
     * @param model business model status context target
     * @return the persistent entity representation mapping the target layout
     */
    StatusEntity toEntity(Status model);

    /**
     * Contextual lookup reference instantiation utility building a placeholder status domain unit
     * around a given ID tracking parameter index.
     *
     * @param value target primary identification database key index
     * @return a disconnected status structure stub tracking the configuration reference ID
     */
    default Status map(Long value) {
        if (value == null) {
            return null;
        }
        Status status = new Status();
        status.setId(value);
        return status;
    }

    /**
     * Performs reverse state translation mapping raw long numerical indicators directly into
     * standardized domain state enums.
     *
     * @param value unique lifecycle sequence identifier mapping the target state definition
     * @return the resolved system wide {@link StatusEnum} configuration
     * @throws IllegalArgumentException if the provided identifier maps to no known tracking state
     */
    @Named("mapLongToStatusEnum")
    default StatusEnum mapLongToStatusEnum(Long value) {
        if (value == null) {
            return null;
        }
        for (StatusEnum status : StatusEnum.values()) {
            if (status.getId().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown Status ID: " + value);
    }

    /**
     * MapStruct customized extension handler constructing a brand-new database relational entity state block
     * out of structural business domain state enum instances.
     *
     * @param statusEnum target context business domain state enumeration key
     * @return a database ready persistence record containing the core identifier definitions
     */
    @Named("mapStatusEnumToStatusEntity")
    default StatusEntity mapStatusEnumToStatusEntity(StatusEnum statusEnum) {
        if (statusEnum == null) return null;
        StatusEntity entity = new StatusEntity();
        entity.setId(statusEnum.getId());
        return entity;
    }

    /**
     * Evaluates database level persistent status definitions, matching their inner row entries against
     * structural system wide state enums.
     *
     * @param statusEntity database persistence record detailing lifecycle fields maps
     * @return the matching target domain {@link StatusEnum} key, or {@code null} if mismatched or absent
     */
    default StatusEnum mapStatusEntityToStatusEnum(StatusEntity statusEntity) {
        if (statusEntity == null || statusEntity.getId() == null) return null;

        for (StatusEnum status : StatusEnum.values()) {
            if (status.getId().equals(statusEntity.getId())) {
                return status;
            }
        }
        return null;
    }
}