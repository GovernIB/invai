package es.caib.invai.back.service.mapper.maintenance.security.ensRequirement;

import es.caib.invai.back.interna.maintenance.security.ensRequirement.DTO.EnsRequirementInputDTO;
import es.caib.invai.back.interna.maintenance.security.ensRequirement.DTO.EnsRequirementOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.security.ensRequirement.EnsRequirementEntity;
import es.caib.invai.back.service.model.maintenance.security.ensRequirement.EnsRequirement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * EnsRequirement database entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.4
 */
@Mapper(componentModel = "spring")
public interface EnsRequirementMapper {

    /**
     * Converts a JPA entity into its business domain model representation.
     *
     * @param entity the entity to convert
     * @return the corresponding domain model
     */
    EnsRequirement toModel(EnsRequirementEntity entity);

    /**
     * Converts a business domain model into its JPA entity representation.
     *
     * @param model the domain model to convert
     * @return the corresponding entity
     */
    EnsRequirementEntity toEntity(EnsRequirement model);

    /**
     * Converts a business domain model into its outbound API response DTO representation.
     *
     * @param model the domain model to convert
     * @return the corresponding output DTO
     */
    EnsRequirementOutputDTO toResponse(EnsRequirement model);

    /**
     * Converts an inbound API request DTO into a new domain model, ignoring auditing fields
     * which are not provided by the client.
     *
     * @param inputDTO the input DTO to convert
     * @return the corresponding domain model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    EnsRequirement toModelFromInput(EnsRequirementInputDTO inputDTO);

    /**
     * Applies the values from an inbound API request DTO onto an existing domain model in place,
     * leaving auditing fields untouched.
     *
     * @param inputDTO the input DTO providing the new values
     * @param model the existing domain model to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(EnsRequirementInputDTO inputDTO, @MappingTarget EnsRequirement model);

    /**
     * Convenience reference lookup utility initializing a detached shallow domain reference
     * using a target primary index key sequence.
     *
     * @param value primary tracking index key reference identity
     * @return a stub domain model tracking the target index, or {@code null} if input is null
     */
    default EnsRequirementOutputDTO map(Long value) {
        if (value == null) {
            return null;
        }
        EnsRequirementOutputDTO ensRequirement = new EnsRequirementOutputDTO();
        ensRequirement.setId(value);
        return ensRequirement;
    }
}
