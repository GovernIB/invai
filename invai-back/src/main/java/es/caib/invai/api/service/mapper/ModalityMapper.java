package es.caib.invai.api.service.mapper;

import es.caib.invai.api.persistence.model.catalog.LkupModalityEntity;
import es.caib.invai.api.service.model.Modality;
import org.mapstruct.Mapper;

/**
 * MapStruct translation utility specializing in state conversions across development modality
 * lookup entities, business model wrappers, and outbound presentation schemas.
 *
 * @since 1.0.2
 */
@Mapper(componentModel = "spring")
public interface ModalityMapper {

    /**
     * Converts a database relational lookup entity into a plain business domain model.
     *
     * @param entity the persistent lookup entity source node
     * @return the matching business domain wrapper model
     */
    Modality toModel(LkupModalityEntity entity);

    /**
     * Converts a domain business layer model into a relational database entity representation.
     *
     * @param model the business model context target
     * @return the persistent entity representation mapping the target layout
     */
    LkupModalityEntity toEntity(Modality model);

    /**
     * Contextual lookup reference instantiation utility building a placeholder modality domain unit
     * around a given ID tracking parameter index.
     *
     * @param value target primary identification database key index
     * @return a disconnected modality structure stub tracking the configuration reference ID, or {@code null} if absent
     */
    default Modality map(Long value) {
        if (value == null) {
            return null;
        }
        Modality modality = new Modality();
        modality.setId(value);
        return modality;
    }
}
