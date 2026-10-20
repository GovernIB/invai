package es.caib.invai.back.service.mapper.application.responsibleAuthorized.dir3;

import es.caib.invai.back.interna.application.responsibleAuthorized.dir3.DTO.Dir3ValidationOutputDTO;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.dir3.Dir3ValidationEntity;
import es.caib.invai.back.service.mapper.catalog.dir3Status.Dir3StatusMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.dir3.Dir3Validation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper converting between {@code Dir3Validation} persistence entities and their
 * business domain model. Delegates the {@code dir3Status} lookup conversion to
 * {@link Dir3StatusMapper}.
 *
 * @since 1.0.5
 */
@Mapper(componentModel = "spring", uses = {Dir3StatusMapper.class})
public interface Dir3ValidationMapper {

    /**
     * Converts a persistence entity into its business domain model equivalent.
     *
     * @param entity the entity to convert
     * @return the corresponding domain model
     */
    Dir3Validation toModel(Dir3ValidationEntity entity);

    /**
     * Converts a business domain model into its persistence entity equivalent.
     *
     * @param model the domain model to convert
     * @return the corresponding persistence entity
     */
    @Mapping(target = "dir3Status", source = "dir3Status", qualifiedByName = "mapDir3ValidationStatusToEntity")
    Dir3ValidationEntity toEntity(Dir3Validation model);

    /**
     * Converts a business domain model into its outbound presentation layer REST DTO.
     */
    Dir3ValidationOutputDTO toResponse(Dir3Validation model);
}
