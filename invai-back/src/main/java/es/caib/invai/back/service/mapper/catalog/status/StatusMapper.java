package es.caib.invai.back.service.mapper.catalog.status;

import es.caib.invai.back.interna.catalog.status.DTO.StatusOutputDTO;
import es.caib.invai.back.persistence.model.catalog.status.LkupStatusEntity;
import es.caib.invai.back.service.model.catalog.status.Status;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;

/**
 * MapStruct translation utility specializing in state conversions across lifecycle tracking entities,
 * business model wrappers, and standardized system state enumeration keys.
 *
 * @since 1.0.1
 */
@Mapper(componentModel = "spring")
public interface StatusMapper {

    /**
     * Converts a persistent status relational row model into a standard business status entity block.
     *
     * @param entity structural relational entity row model source
     * @return the matching business status wrapper model
     */
    Status toModel(LkupStatusEntity entity);

    /**
     * Converts a business status layout container down into a relational schema entity structure.
     *
     * @param model business model status context target
     * @return the persistent entity representation mapping the target layout
     */
    LkupStatusEntity toEntity(Status model);

    /**
     * Converts a domain business layer model into its outbound presentation DTO.
     *
     * @param model the business model to convert
     * @return the resulting outbound {@link StatusOutputDTO}
     */
    StatusOutputDTO toResponse(Status model);

    /**
     * Converts a list of persistent lookup entities into their domain model representations.
     *
     * @param entities the persistent lookup entities to convert
     * @return the matching list of business domain models
     */
    List<Status> toModelList(List<LkupStatusEntity> entities);

    /**
     * Converts a list of domain models into their outbound presentation DTOs.
     *
     * @param models the business domain models to convert
     * @return the matching list of outbound {@link StatusOutputDTO} entries
     */
    List<StatusOutputDTO> toResponseList(List<Status> models);

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
    default LkupStatusEntity mapStatusEnumToStatusEntity(StatusEnum statusEnum) {
        if (statusEnum == null) return null;
        LkupStatusEntity entity = new LkupStatusEntity();
        entity.setId(statusEnum.getId());
        return entity;
    }

    /**
     * Evaluates database level persistent status definitions, matching their inner row entries against
     * structural system wide state enums.
     *
     * @param lkupStatusEntity database persistence record detailing lifecycle fields maps
     * @return the matching target domain {@link StatusEnum} key, or {@code null} if mismatched or absent
     */
    default StatusEnum mapStatusEntityToStatusEnum(LkupStatusEntity lkupStatusEntity) {
        if (lkupStatusEntity == null || lkupStatusEntity.getId() == null) return null;

        for (StatusEnum status : StatusEnum.values()) {
            if (status.getId().equals(lkupStatusEntity.getId())) {
                return status;
            }
        }
        return null;
    }
}