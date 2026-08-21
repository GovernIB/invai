package es.caib.invai.back.service.mapper.catalog.standardAdaption;

import es.caib.invai.back.interna.catalog.standardAdaption.DTO.StandardAdaptionOutputDTO;
import es.caib.invai.back.persistence.model.catalog.standardAdaption.LkupStandardAdaptionEntity;
import es.caib.invai.back.service.model.catalog.standardAdaption.StandardAdaption;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct translation utility specializing in state conversions across GOIB standards
 * compliance lookup entities, business model wrappers, and outbound presentation schemas.
 *
 * @since 1.0.2
 */
@Mapper(componentModel = "spring")
public interface StandardAdaptionMapper {

    /**
     * Converts a database relational lookup entity into a plain business domain model.
     *
     * @param entity the persistent lookup entity source node
     * @return the matching business domain wrapper model
     */
    StandardAdaption toModel(LkupStandardAdaptionEntity entity);

    /**
     * Converts a domain business layer model into a relational database entity representation.
     *
     * @param model the business model context target
     * @return the persistent entity representation mapping the target layout
     */
    LkupStandardAdaptionEntity toEntity(StandardAdaption model);

    /**
     * Converts a domain business layer model into its outbound presentation DTO.
     *
     * @param model the business model to convert
     * @return the resulting outbound {@link StandardAdaptionOutputDTO}
     */
    StandardAdaptionOutputDTO toResponse(StandardAdaption model);

    /**
     * Converts a list of persistent lookup entities into their domain model representations.
     *
     * @param entities the persistent lookup entities to convert
     * @return the matching list of business domain models
     */
    List<StandardAdaption> toModelList(List<LkupStandardAdaptionEntity> entities);

    /**
     * Converts a list of domain models into their outbound presentation DTOs.
     *
     * @param models the business domain models to convert
     * @return the matching list of outbound {@link StandardAdaptionOutputDTO} entries
     */
    List<StandardAdaptionOutputDTO> toResponseList(List<StandardAdaption> models);

    /**
     * Contextual lookup reference instantiation utility building a placeholder compliance domain unit
     * around a given ID tracking parameter index.
     *
     * @param value target primary identification database key index
     * @return a disconnected compliance structure stub tracking the configuration reference ID, or {@code null} if absent
     */
    default StandardAdaption map(Long value) {
        if (value == null) {
            return null;
        }
        StandardAdaption standardAdaption = new StandardAdaption();
        standardAdaption.setId(value);
        return standardAdaption;
    }
}
