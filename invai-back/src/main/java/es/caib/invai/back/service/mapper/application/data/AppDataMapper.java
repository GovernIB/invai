package es.caib.invai.back.service.mapper.application.data;

import es.caib.invai.back.interna.application.data.DTO.AppDataInputDTO;
import es.caib.invai.back.interna.application.data.DTO.AppDataOutputDTO;
import es.caib.invai.back.persistence.model.application.data.AppDataEntity;
import es.caib.invai.back.service.model.application.data.AppData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapper;

/**
 * MapStruct mapper converting between {@link AppData} domain models, {@link
 * AppDataEntity} persistence entities, and the inbound/outbound AppData DTOs. {@code
 * openData} on {@link AppDataOutputDTO} is never populated by this mapper - it holds no
 * persisted counterpart on the domain model, and is instead set by the facade after the live
 * external fetch.
 *
 * @since 1.0.5
 */
@Mapper(componentModel = "spring", uses = {ApplicationMapper.class})
public interface AppDataMapper {

    /**
     * Maps a persistence {@link AppDataEntity} to its corresponding {@link AppData} domain model.
     *
     * @param entity the JPA entity to convert
     * @return the mapped domain model
     */
    AppData toModel(AppDataEntity entity);

    /**
     * Maps an {@link AppData} domain model to its corresponding {@link AppDataEntity} persistence entity.
     *
     * @param model the domain model to convert
     * @return the mapped JPA entity
     */
    AppDataEntity toEntity(AppData model);

    /**
     * Maps an {@link AppData} domain model to its outbound {@link AppDataOutputDTO},
     * leaving {@code openData}/{@code reuse} unset - the facade fills them in after fetching each live.
     *
     * @param model the domain model to convert
     * @return the mapped outbound DTO
     */
    @Mapping(target = "openData", ignore = true)
    @Mapping(target = "reuse", ignore = true)
    AppDataOutputDTO toResponse(AppData model);

    /**
     * Maps an inbound {@link AppDataInputDTO} to a new {@link AppData} domain model,
     * resolving {@code applicationId} to its nested reference ID and leaving audit metadata fields unset.
     *
     * @param inputDTO the inbound DTO containing open data anchor data
     * @return the mapped domain model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "application.id", source = "applicationId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppData toModelFromInput(AppDataInputDTO inputDTO);

    /**
     * Applies the values of an inbound {@link AppDataInputDTO} onto an existing {@link
     * AppData} domain model in place, leaving audit metadata fields untouched.
     *
     * @param inputDTO the inbound DTO containing updated open data anchor data
     * @param model    the existing domain model to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "application.id", source = "applicationId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(AppDataInputDTO inputDTO, @MappingTarget AppData model);
}
