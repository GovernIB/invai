package es.caib.invai.back.service.mapper.application.development.core;

import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentInputDTO;
import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentOutputDTO;
import es.caib.invai.back.persistence.model.application.development.core.AppDevelopmentEntity;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapper;
import es.caib.invai.back.service.mapper.catalog.modality.ModalityMapper;
import es.caib.invai.back.service.mapper.catalog.standardAdaption.StandardAdaptionMapper;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapper;

/**
 * MapStruct mapper converting between {@link AppDevelopment} domain models, {@link AppDevelopmentEntity}
 * persistence entities, and the development input/output DTOs, resolving the modality, standard
 * adaption, and application lookup references along the way.
 *
 * @since 1.0.2
 */
@Mapper(componentModel = "spring", uses = {ModalityMapper.class, StandardAdaptionMapper.class, StatusMapper.class, ApplicationMapper.class})
public interface AppDevelopmentMapper {

    /**
     * Maps a persistence {@link AppDevelopmentEntity} to its corresponding {@link AppDevelopment} domain model.
     *
     * @param entity the JPA entity to convert
     * @return the mapped domain model
     */
    AppDevelopment toModel(AppDevelopmentEntity entity);

    /**
     * Maps an {@link AppDevelopment} domain model to its corresponding {@link AppDevelopmentEntity} persistence entity.
     *
     * @param model the domain model to convert
     * @return the mapped JPA entity
     */
    AppDevelopmentEntity toEntity(AppDevelopment model);

    /**
     * Maps an {@link AppDevelopment} domain model to an outbound {@link DevelopmentOutputDTO}.
     * Returns {@code null} when {@code model} is {@code null}, so {@code getById} on a missing
     * record silently comes back as {@code null} rather than throwing.
     *
     * @param model the domain model to convert, may be {@code null}
     * @return the mapped outbound DTO, or {@code null} if {@code model} is {@code null}
     */
    DevelopmentOutputDTO toResponse(AppDevelopment model);

    /**
     * Maps an inbound {@link DevelopmentInputDTO} to a new {@link AppDevelopment} domain model,
     * resolving the application, environment, modality and standard adaption references, and
     * leaving the id and audit metadata fields unset.
     *
     * @param inputDTO the inbound DTO containing development data
     * @return the mapped domain model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "application.id", source = "applicationId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "environment.id", source = "environmentId")
    @Mapping(target = "modality", source = "modalityId")
    @Mapping(target = "standardAdaption", source = "standardAdaptionId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppDevelopment toModelFromInput(DevelopmentInputDTO inputDTO);

    /**
     * Applies the values of an inbound {@link DevelopmentInputDTO} onto an existing {@link AppDevelopment}
     * domain model in place, resolving the application, environment, modality and standard adaption
     * references, and leaving the id and audit metadata fields untouched.
     *
     * @param inputDTO the inbound DTO containing updated development data
     * @param model    the existing domain model to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "application.id", source = "applicationId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "environment.id", source = "environmentId")
    @Mapping(target = "modality", source = "modalityId")
    @Mapping(target = "standardAdaption", source = "standardAdaptionId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(DevelopmentInputDTO inputDTO, @MappingTarget AppDevelopment model);
}
