package es.caib.invai.api.service.mapper;

import es.caib.invai.api.interna.application.system_database.system.DTO.AppSystemInputDTO;
import es.caib.invai.api.interna.application.system_database.system.DTO.AppSystemOutputDTO;
import es.caib.invai.api.persistence.model.AppSystemEntity;
import es.caib.invai.api.service.model.AppSystem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
        componentModel = "spring", uses = {StatusMapper.class})
public interface AppSystemMapper {

    AppSystem toModel(AppSystemEntity entity);

    AppSystemEntity toEntity(AppSystem model);

    AppSystemOutputDTO toResponse(AppSystem model);

    @Mapping(target = "informationSystemDb.id", source = "informationSystemDbId")
    @Mapping(target = "system.id", source = "systemId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppSystem toModelFromInput(AppSystemInputDTO inputDTO);

    @Mapping(target = "informationSystemDb.id", source = "informationSystemDbId")
    @Mapping(target = "system.id", source = "systemId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(AppSystemInputDTO inputDTO, @MappingTarget AppSystem model);
}
