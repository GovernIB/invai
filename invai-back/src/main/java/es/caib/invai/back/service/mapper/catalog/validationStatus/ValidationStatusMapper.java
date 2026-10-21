package es.caib.invai.back.service.mapper.catalog.validationStatus;

import es.caib.invai.back.interna.catalog.validationStatus.DTO.ValidationStatusOutputDTO;
import es.caib.invai.back.persistence.model.catalog.validationStatus.LkupValidationStatusEntity;
import es.caib.invai.back.service.model.catalog.validationStatus.ValidationStatus;
import es.caib.invai.back.service.model.catalog.validationStatus.ValidationStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;
import java.util.Objects;

/**
 * MapStruct translation utility specializing in state conversions across the validation
 * status lookup entity, its plain business domain model, and the {@link ValidationStatusEnum}
 * enumeration used elsewhere (e.g. on {@code Dir3Validation} and {@code AppRole}), mirroring
 * {@code StatusMapper}.
 *
 * @since 1.0.5
 */
@Mapper(componentModel = "spring")
public interface ValidationStatusMapper {

    /**
     * Converts a persistent lookup entity into a plain business domain model.
     *
     * @param entity structural relational entity row model source
     * @return the matching business status wrapper model
     */
    ValidationStatus toModel(LkupValidationStatusEntity entity);

    /**
     * Converts a business status layout container down into a relational schema entity structure.
     *
     * @param model business model status context target
     * @return the persistent entity representation mapping the target layout
     */
    LkupValidationStatusEntity toEntity(ValidationStatus model);

    /**
     * Converts a domain business layer model into its outbound presentation DTO.
     *
     * @param model the business model to convert
     * @return the resulting outbound {@link ValidationStatusOutputDTO}
     */
    ValidationStatusOutputDTO toResponse(ValidationStatus model);

    /**
     * Converts a list of persistent lookup entities into their domain model representations.
     *
     * @param entities the persistent lookup entities to convert
     * @return the matching list of business domain models
     */
    List<ValidationStatus> toModelList(List<LkupValidationStatusEntity> entities);

    /**
     * Converts a list of domain models into their outbound presentation DTOs.
     *
     * @param models the business domain models to convert
     * @return the matching list of outbound {@link ValidationStatusOutputDTO} entries
     */
    List<ValidationStatusOutputDTO> toResponseList(List<ValidationStatus> models);

    /**
     * Contextual lookup reference instantiation utility building a placeholder domain unit around
     * a given ID tracking parameter index.
     *
     * @param value target primary identification database key index
     * @return a disconnected status structure stub tracking the configuration reference ID
     */
    default ValidationStatus map(Long value) {
        if (Objects.isNull(value)) {
            return null;
        }
        ValidationStatus status = new ValidationStatus();
        status.setId(value);
        return status;
    }

    /**
     * Resolves the fixed {@link ValidationStatusEnum} enum value matching a given lookup row id.
     *
     * @param value unique lookup row identifier
     * @return the resolved {@link ValidationStatusEnum}, or {@code null} if {@code value} is {@code null}
     * @throws IllegalArgumentException if the given id maps to no known {@link ValidationStatusEnum}
     */
    @Named("mapLongToValidationStatusEnum")
    default ValidationStatusEnum mapLongToValidationStatusEnum(Long value) {
        if (Objects.isNull(value)) {
            return null;
        }
        for (ValidationStatusEnum status : ValidationStatusEnum.values()) {
            if (status.getId().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ValidationStatus ID: " + value);
    }

    /**
     * Builds a lookup entity stub (id only) from a fixed {@link ValidationStatusEnum} enum value.
     *
     * @param status the enum value to convert
     * @return a lookup entity carrying only the matching id, or {@code null} if {@code status} is {@code null}
     */
    @Named("mapValidationStatusEnumToEntity")
    default LkupValidationStatusEntity mapValidationStatusEnumToEntity(ValidationStatusEnum status) {
        if (Objects.isNull(status)) {
            return null;
        }
        LkupValidationStatusEntity entity = new LkupValidationStatusEntity();
        entity.setId(status.getId());
        return entity;
    }

    /**
     * Resolves the fixed {@link ValidationStatusEnum} enum value matching a resolved lookup entity.
     *
     * @param entity the persisted lookup entity to translate
     * @return the matching {@link ValidationStatusEnum}, or {@code null} if mismatched or absent
     */
    default ValidationStatusEnum mapEntityToValidationStatusEnum(LkupValidationStatusEntity entity) {
        if (Objects.isNull(entity) || Objects.isNull(entity.getId())) {
            return null;
        }
        for (ValidationStatusEnum status : ValidationStatusEnum.values()) {
            if (status.getId().equals(entity.getId())) {
                return status;
            }
        }
        return null;
    }
}
