package es.caib.invai.back.service.mapper.catalog.dir3Status;

import es.caib.invai.back.interna.catalog.dir3Status.DTO.Dir3StatusOutputDTO;
import es.caib.invai.back.persistence.model.catalog.dir3Status.LkupDir3StatusEntity;
import es.caib.invai.back.service.model.catalog.dir3Status.Dir3Status;
import es.caib.invai.back.service.model.catalog.dir3Status.Dir3ValidationStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;

/**
 * MapStruct translation utility specializing in state conversions across the DIR3 validation
 * status lookup entity, its plain business domain model, and the {@link Dir3ValidationStatus}
 * enumeration used elsewhere (e.g. on {@code Dir3Validation}), mirroring {@code StatusMapper}.
 *
 * @since 1.0.5
 */
@Mapper(componentModel = "spring")
public interface Dir3StatusMapper {

    /**
     * Converts a persistent lookup entity into a plain business domain model.
     *
     * @param entity structural relational entity row model source
     * @return the matching business status wrapper model
     */
    Dir3Status toModel(LkupDir3StatusEntity entity);

    /**
     * Converts a business status layout container down into a relational schema entity structure.
     *
     * @param model business model status context target
     * @return the persistent entity representation mapping the target layout
     */
    LkupDir3StatusEntity toEntity(Dir3Status model);

    /**
     * Converts a domain business layer model into its outbound presentation DTO.
     *
     * @param model the business model to convert
     * @return the resulting outbound {@link Dir3StatusOutputDTO}
     */
    Dir3StatusOutputDTO toResponse(Dir3Status model);

    /**
     * Converts a list of persistent lookup entities into their domain model representations.
     *
     * @param entities the persistent lookup entities to convert
     * @return the matching list of business domain models
     */
    List<Dir3Status> toModelList(List<LkupDir3StatusEntity> entities);

    /**
     * Converts a list of domain models into their outbound presentation DTOs.
     *
     * @param models the business domain models to convert
     * @return the matching list of outbound {@link Dir3StatusOutputDTO} entries
     */
    List<Dir3StatusOutputDTO> toResponseList(List<Dir3Status> models);

    /**
     * Contextual lookup reference instantiation utility building a placeholder domain unit around
     * a given ID tracking parameter index.
     *
     * @param value target primary identification database key index
     * @return a disconnected status structure stub tracking the configuration reference ID
     */
    default Dir3Status map(Long value) {
        if (value == null) {
            return null;
        }
        Dir3Status status = new Dir3Status();
        status.setId(value);
        return status;
    }

    /**
     * Resolves the fixed {@link Dir3ValidationStatus} enum value matching a given lookup row id.
     *
     * @param value unique lookup row identifier
     * @return the resolved {@link Dir3ValidationStatus}, or {@code null} if {@code value} is {@code null}
     * @throws IllegalArgumentException if the given id maps to no known {@link Dir3ValidationStatus}
     */
    @Named("mapLongToDir3ValidationStatus")
    default Dir3ValidationStatus mapLongToDir3ValidationStatus(Long value) {
        if (value == null) {
            return null;
        }
        for (Dir3ValidationStatus status : Dir3ValidationStatus.values()) {
            if (status.getId().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown Dir3Status ID: " + value);
    }

    /**
     * Builds a lookup entity stub (id only) from a fixed {@link Dir3ValidationStatus} enum value.
     *
     * @param status the enum value to convert
     * @return a lookup entity carrying only the matching id, or {@code null} if {@code status} is {@code null}
     */
    @Named("mapDir3ValidationStatusToEntity")
    default LkupDir3StatusEntity mapDir3ValidationStatusToEntity(Dir3ValidationStatus status) {
        if (status == null) {
            return null;
        }
        LkupDir3StatusEntity entity = new LkupDir3StatusEntity();
        entity.setId(status.getId());
        return entity;
    }

    /**
     * Resolves the fixed {@link Dir3ValidationStatus} enum value matching a resolved lookup entity.
     *
     * @param entity the persisted lookup entity to translate
     * @return the matching {@link Dir3ValidationStatus}, or {@code null} if mismatched or absent
     */
    default Dir3ValidationStatus mapEntityToDir3ValidationStatus(LkupDir3StatusEntity entity) {
        if (entity == null || entity.getId() == null) {
            return null;
        }
        for (Dir3ValidationStatus status : Dir3ValidationStatus.values()) {
            if (status.getId().equals(entity.getId())) {
                return status;
            }
        }
        return null;
    }
}
