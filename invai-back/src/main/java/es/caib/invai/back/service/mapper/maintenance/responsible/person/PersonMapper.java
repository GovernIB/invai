package es.caib.invai.back.service.mapper.maintenance.responsible.person;

import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.responsible.person.PersonEntity;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import es.caib.invai.back.service.mapper.maintenance.responsible.company.CompanyMapper;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * Person database entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.3
 */
@Mapper(componentModel = "spring", uses = {CompanyMapper.class})
public interface PersonMapper {

    /**
     * Converts a database relational layer entity into a plain business domain model.
     */
    Person toModel(PersonEntity entity);

    /**
     * Converts a domain business layer model into a relational database entity representation.
     */
    PersonEntity toEntity(Person model);

    /**
     * Converts a domain model configuration into an outbound presentation layer REST DTO.
     */
    PersonOutputDTO toResponse(Person model);

    /**
     * Constructs a pure domain business structure from incoming input payload parameter DTOs.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company.id", source = "companyId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Person toModelFromInput(PersonInputDTO inputDTO);

    /**
     * Merges update parameters from an API input payload DTO into an existing active domain business model.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company.id", source = "companyId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(PersonInputDTO inputDTO, @MappingTarget Person model);
}
