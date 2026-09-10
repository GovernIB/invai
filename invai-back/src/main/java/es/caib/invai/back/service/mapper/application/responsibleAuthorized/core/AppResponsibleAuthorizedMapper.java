package es.caib.invai.back.service.mapper.application.responsibleAuthorized.core;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.core.AppResponsibleAuthorizedEntity;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapper;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper converting between the {@code AppResponsibleAuthorized} anchor entity and its
 * business domain model. No input/output DTO mappings are needed here: the anchor has no REST
 * endpoints of its own, it is auto-provisioned alongside its parent {@code Application}.
 * {@code StatusMapper} is required to resolve the nested
 * {@code Application.status} field (mirrors {@code AppInformationSystemDbMapper}).
 * {@code ApplicationMapper} is used to delegate the nested {@code Application} conversion so the
 * fully-annotated mapping (which explicitly resolves {@code status} via the qualified enum-to-entity
 * conversion) is reused instead of an incomplete auto-generated one.
 *
 * @since 1.0.3
 */
@Mapper(componentModel = "spring", uses = {StatusMapper.class, ApplicationMapper.class})
public interface AppResponsibleAuthorizedMapper {

    /**
     * Converts a persistence entity into its business domain model equivalent.
     *
     * @param entity the anchor entity to convert
     * @return the corresponding domain model
     */
    AppResponsibleAuthorized toModel(AppResponsibleAuthorizedEntity entity);

    /**
     * Converts a business domain model into its persistence entity equivalent.
     *
     * @param model the domain model to convert
     * @return the corresponding persistence entity
     */
    AppResponsibleAuthorizedEntity toEntity(AppResponsibleAuthorized model);
}
