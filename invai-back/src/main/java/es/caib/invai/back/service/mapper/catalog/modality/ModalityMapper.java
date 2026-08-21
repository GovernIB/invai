package es.caib.invai.back.service.mapper.catalog.modality;

import es.caib.invai.back.interna.catalog.modality.DTO.ModalityOutputDTO;
import es.caib.invai.back.persistence.model.catalog.modality.LkupModalityEntity;
import es.caib.invai.back.service.model.catalog.modality.Modality;
import org.mapstruct.Mapper;

import java.util.List;

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
     * Converts a domain business layer model into its outbound presentation DTO.
     *
     * @param model the business model to convert
     * @return the resulting outbound {@link ModalityOutputDTO}
     */
    ModalityOutputDTO toResponse(Modality model);

    /**
     * Converts a list of persistent lookup entities into their domain model representations.
     *
     * @param entities the persistent lookup entities to convert
     * @return the matching list of business domain models
     */
    List<Modality> toModelList(List<LkupModalityEntity> entities);

    /**
     * Converts a list of domain models into their outbound presentation DTOs.
     *
     * @param models the business domain models to convert
     * @return the matching list of outbound {@link ModalityOutputDTO} entries
     */
    List<ModalityOutputDTO> toResponseList(List<Modality> models);

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
