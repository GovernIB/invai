package es.caib.invai.back.service.mapper.maintenance.integration.externalSystem;

import es.caib.invai.back.interna.maintenance.integration.externalSystem.DTO.ExternalSystemInputDTO;
import es.caib.invai.back.interna.maintenance.integration.externalSystem.DTO.ExternalSystemOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.integration.externalSystem.ExternalSystemEntity;
import es.caib.invai.back.service.mapper.maintenance.responsible.company.CompanyMapper;
import es.caib.invai.back.service.model.maintenance.integration.externalSystem.ExternalSystem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * External System database entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.5
 */
@Mapper(componentModel = "spring", uses = {CompanyMapper.class})
public interface ExternalSystemMapper {

    /**
     * Converts a persistence entity into the corresponding domain model.
     *
     * @param entity the entity to convert
     * @return the converted domain model
     */
    ExternalSystem toModel(ExternalSystemEntity entity);

    /**
     * Converts a domain model into the corresponding persistence entity.
     *
     * @param model the domain model to convert
     * @return the converted entity
     */
    ExternalSystemEntity toEntity(ExternalSystem model);

    /**
     * Converts a domain model into the corresponding outbound response DTO.
     *
     * @param model the domain model to convert
     * @return the converted response DTO
     */
    ExternalSystemOutputDTO toResponse(ExternalSystem model);

    /**
     * Converts an inbound input DTO into a new domain model, leaving audit fields unset.
     *
     * @param inputDTO the input DTO to convert
     * @return the converted domain model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company.id", source = "companyId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    ExternalSystem toModelFromInput(ExternalSystemInputDTO inputDTO);

    /**
     * Applies the fields of an inbound input DTO onto an existing domain model, leaving audit fields untouched.
     *
     * @param inputDTO the input DTO holding the new values
     * @param model    the target domain model to update in place
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company.id", source = "companyId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(ExternalSystemInputDTO inputDTO, @MappingTarget ExternalSystem model);

    /**
     * Convenience reference lookup utility initializing a detached shallow domain reference
     * using a target primary index key sequence.
     *
     * @param value primary tracking index key reference identity
     * @return a stub domain model tracking the target index, or {@code null} if input is null
     */
    default ExternalSystemOutputDTO map(Long value) {
        if (value == null) {
            return null;
        }
        ExternalSystemOutputDTO externalSystem = new ExternalSystemOutputDTO();
        externalSystem.setId(value);
        return externalSystem;
    }
}
