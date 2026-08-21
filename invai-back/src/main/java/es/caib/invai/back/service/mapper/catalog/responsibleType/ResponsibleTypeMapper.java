package es.caib.invai.back.service.mapper.catalog.responsibleType;

import es.caib.invai.back.interna.catalog.responsibleType.DTO.ResponsibleTypeOutputDTO;
import es.caib.invai.back.persistence.model.catalog.responsibleType.LkupResponsibleTypeEntity;
import es.caib.invai.back.service.model.catalog.responsibleType.ResponsibleType;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct translation utility specializing in state conversions across responsible type
 * lookup entities, their plain business domain model, and outbound presentation schemas.
 * The domain model is also nested directly inside other entities' response payloads
 * (e.g. {@code AppResponsibleOutputDTO}).
 *
 * @since 1.0.3
 */
@Mapper(componentModel = "spring")
public interface ResponsibleTypeMapper {

    /**
     * Converts a database relational lookup entity into a plain business domain model.
     *
     * @param entity the persistent lookup entity source node
     * @return the matching business domain wrapper model
     */
    ResponsibleType toModel(LkupResponsibleTypeEntity entity);

    /**
     * Converts a domain business layer model into a relational database entity representation.
     *
     * @param model the business model context target
     * @return the persistent entity representation mapping the target layout
     */
    LkupResponsibleTypeEntity toEntity(ResponsibleType model);

    /**
     * Converts a domain business layer model into its outbound presentation DTO.
     *
     * @param model the business model to convert
     * @return the resulting outbound {@link ResponsibleTypeOutputDTO}
     */
    ResponsibleTypeOutputDTO toResponse(ResponsibleType model);

    /**
     * Converts a list of persistent lookup entities into their domain model representations.
     *
     * @param entities the persistent lookup entities to convert
     * @return the matching list of business domain models
     */
    List<ResponsibleType> toModelList(List<LkupResponsibleTypeEntity> entities);

    /**
     * Converts a list of domain models into their outbound presentation DTOs.
     *
     * @param models the business domain models to convert
     * @return the matching list of outbound {@link ResponsibleTypeOutputDTO} entries
     */
    List<ResponsibleTypeOutputDTO> toResponseList(List<ResponsibleType> models);

    /**
     * Convenience reference lookup utility initializing a detached shallow domain reference
     * using a target primary index key sequence.
     *
     * @param value primary tracking index key reference identity
     * @return a stub domain model tracking the target index, or {@code null} if input is null
     */
    default ResponsibleType map(Long value) {
        if (value == null) {
            return null;
        }
        ResponsibleType responsibleType = new ResponsibleType();
        responsibleType.setId(value);
        return responsibleType;
    }
}
