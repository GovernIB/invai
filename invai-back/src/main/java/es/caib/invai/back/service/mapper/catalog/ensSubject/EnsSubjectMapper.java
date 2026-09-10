package es.caib.invai.back.service.mapper.catalog.ensSubject;

import es.caib.invai.back.interna.catalog.ensSubject.DTO.EnsSubjectOutputDTO;
import es.caib.invai.back.persistence.model.catalog.ensSubject.LkupEnsSubjectEntity;
import es.caib.invai.back.service.model.catalog.ensSubject.EnsSubject;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct translation utility specializing in state conversions across ENS subjection
 * lookup entities, their plain business domain model, and outbound presentation schemas.
 * The domain model is also nested directly inside other entities' response payloads
 * (e.g. {@code AppResponsibleOutputDTO}).
 *
 * @since 1.0.4
 */
@Mapper(componentModel = "spring")
public interface EnsSubjectMapper {

    /**
     * Converts a database relational lookup entity into a plain business domain model.
     *
     * @param entity the persistent lookup entity source node
     * @return the matching business domain wrapper model
     */
    EnsSubject toModel(LkupEnsSubjectEntity entity);

    /**
     * Converts a domain business layer model into a relational database entity representation.
     *
     * @param model the business model context target
     * @return the persistent entity representation mapping the target layout
     */
    LkupEnsSubjectEntity toEntity(EnsSubject model);

    /**
     * Converts a domain business layer model into its outbound presentation DTO.
     *
     * @param model the business model to convert
     * @return the resulting outbound {@link EnsSubjectOutputDTO}
     */
    EnsSubjectOutputDTO toResponse(EnsSubject model);

    /**
     * Converts a list of persistent lookup entities into their domain model representations.
     *
     * @param entities the persistent lookup entities to convert
     * @return the matching list of business domain models
     */
    List<EnsSubject> toModelList(List<LkupEnsSubjectEntity> entities);

    /**
     * Converts a list of domain models into their outbound presentation DTOs.
     *
     * @param models the business domain models to convert
     * @return the matching list of outbound {@link EnsSubjectOutputDTO} entries
     */
    List<EnsSubjectOutputDTO> toResponseList(List<EnsSubject> models);

    /**
     * Convenience reference lookup utility initializing a detached shallow domain reference
     * using a target primary index key sequence.
     *
     * @param value primary tracking index key reference identity
     * @return a stub domain model tracking the target index, or {@code null} if input is null
     */
    default EnsSubject map(Long value) {
        if (value == null) {
            return null;
        }
        EnsSubject ensSubject = new EnsSubject();
        ensSubject.setId(value);
        return ensSubject;
    }
}
