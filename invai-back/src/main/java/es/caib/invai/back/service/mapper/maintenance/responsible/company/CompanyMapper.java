package es.caib.invai.back.service.mapper.maintenance.responsible.company;

import es.caib.invai.back.interna.maintenance.responsible.company.DTO.CompanyInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.company.DTO.CompanyOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.responsible.company.CompanyEntity;
import es.caib.invai.back.service.model.maintenance.responsible.company.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * Company database entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.3
 */
@Mapper(componentModel = "spring")
public interface CompanyMapper {

    /**
     * Converts a persistence entity into the corresponding domain model.
     *
     * @param entity the entity to convert
     * @return the converted domain model
     */
    Company toModel(CompanyEntity entity);

    /**
     * Converts a domain model into the corresponding persistence entity.
     *
     * @param model the domain model to convert
     * @return the converted entity
     */
    CompanyEntity toEntity(Company model);

    /**
     * Converts a domain model into the corresponding outbound response DTO.
     *
     * @param model the domain model to convert
     * @return the converted response DTO
     */
    CompanyOutputDTO toResponse(Company model);

    /**
     * Converts an inbound input DTO into a new domain model, leaving audit fields unset.
     *
     * @param inputDTO the input DTO to convert
     * @return the converted domain model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Company toModelFromInput(CompanyInputDTO inputDTO);

    /**
     * Applies the fields of an inbound input DTO onto an existing domain model, leaving audit fields untouched.
     *
     * @param inputDTO the input DTO holding the new values
     * @param model    the target domain model to update in place
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(CompanyInputDTO inputDTO, @MappingTarget Company model);

    /**
     * Convenience reference lookup utility initializing a detached shallow domain reference
     * using a target primary index key sequence.
     *
     * @param value primary tracking index key reference identity
     * @return a stub domain model tracking the target index, or {@code null} if input is null
     */
    default CompanyOutputDTO map(Long value) {
        if (value == null) {
            return null;
        }
        CompanyOutputDTO company = new CompanyOutputDTO();
        company.setId(value);
        return company;
    }
}
