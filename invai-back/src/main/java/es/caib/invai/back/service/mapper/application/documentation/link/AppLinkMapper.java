package es.caib.invai.back.service.mapper.application.documentation.link;

import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkInputDTO;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkOutputDTO;
import es.caib.invai.back.interna.application.documentation.link.DTO.AppDocumentationLinkUpdateInputDTO;
import es.caib.invai.back.persistence.model.application.documentation.link.AppLinkEntity;
import es.caib.invai.back.service.mapper.catalog.appSection.AppSectionMapper;
import es.caib.invai.back.service.model.application.documentation.link.AppLink;
import es.caib.invai.back.service.model.catalog.appSection.AppSection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper converting links between entities, domain models and API transfer schemas. The
 * section of the inbound payloads is carried as a model holding only its identifier; the
 * repository turns it into a reference.
 *
 * @since 1.0.6
 */
@Mapper(componentModel = "spring", uses = {AppSectionMapper.class})
public interface AppLinkMapper {

    /**
     * Converts a persistence entity into the domain model.
     *
     * @param entity the entity to convert
     * @return the converted domain model
     */
    @Mapping(target = "applicationId", source = "application.id")
    AppLink toModel(AppLinkEntity entity);

    /**
     * Converts a domain model into the persistence entity, leaving the application and the section
     * to be set by the repository.
     *
     * @param model the domain model to convert
     * @return the converted entity
     */
    @Mapping(target = "application", ignore = true)
    @Mapping(target = "section", ignore = true)
    AppLinkEntity toEntity(AppLink model);

    /**
     * Converts a domain model into the response DTO.
     *
     * @param model the domain model to convert
     * @return the converted response DTO
     */
    AppDocumentationLinkOutputDTO toResponse(AppLink model);

    /**
     * Converts an inbound input DTO into a new domain model, leaving audit fields unset.
     *
     * @param inputDTO the input DTO to convert
     * @return the converted domain model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "section", expression = "java(newSection(inputDTO.getSectionId()))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppLink toModelFromInput(AppDocumentationLinkInputDTO inputDTO);

    /**
     * Applies the fields of an inbound update DTO onto an existing domain model, leaving the
     * application and the audit fields untouched.
     *
     * @param inputDTO the input DTO holding the new values
     * @param model    the target domain model to update in place
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "section", expression = "java(newSection(inputDTO.getSectionId()))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(AppDocumentationLinkUpdateInputDTO inputDTO, @MappingTarget AppLink model);

    /**
     * Builds a section holding only the given identifier.
     *
     * @param id the identifier of the section
     * @return the section
     */
    default AppSection newSection(Long id) {
        AppSection section = new AppSection();
        section.setId(id);
        return section;
    }
}
