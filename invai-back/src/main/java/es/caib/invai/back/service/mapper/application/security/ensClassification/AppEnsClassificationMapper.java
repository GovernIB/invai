package es.caib.invai.back.service.mapper.application.security.ensClassification;

import es.caib.invai.back.interna.application.security.ensClassification.DTO.AppEnsClassificationInputDTO;
import es.caib.invai.back.interna.application.security.ensClassification.DTO.AppEnsClassificationOutputDTO;
import es.caib.invai.back.persistence.model.application.security.ensClassification.AppEnsClassificationEntity;
import es.caib.invai.back.service.model.application.security.ensClassification.AppEnsClassification;
import es.caib.invai.back.service.model.maintenance.security.identityProvider.IdentityProvider;
import es.caib.invai.back.service.model.maintenance.security.personalDataProcessing.PersonalDataProcessing;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import es.caib.invai.back.service.mapper.application.security.core.AppSecurityMapper;
import es.caib.invai.back.service.mapper.maintenance.security.identityProvider.IdentityProviderMapper;
import es.caib.invai.back.service.mapper.catalog.ensSubject.EnsSubjectMapper;
import es.caib.invai.back.service.mapper.maintenance.security.personalDataProcessing.PersonalDataProcessingMapper;
import es.caib.invai.back.service.mapper.catalog.securityLevel.SecurityLevelMapper;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * AppEnsClassification relation entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.4
 */
@Mapper(componentModel = "spring", uses = {AppSecurityMapper.class, IdentityProviderMapper.class, EnsSubjectMapper.class, PersonalDataProcessingMapper.class, SecurityLevelMapper.class})
public interface AppEnsClassificationMapper {

    /**
     * Materializes a persistent relation entity into a business domain aggregate.
     *
     * @param entity the persistent relation entity source node
     * @return a clean domain business layout graph
     */
    AppEnsClassification toModel(AppEnsClassificationEntity entity);

    /**
     * Maps business domain representations down to persistent relation database entities.
     *
     * @param model the active composite business domain model node
     * @return a mapped relational database entity layout
     */
    AppEnsClassificationEntity toEntity(AppEnsClassification model);

    /**
     * Flattens and structuralizes domain graphs into outbound API presentation response layers.
     *
     * @param model source domain business state schema instance
     * @return the outbound presentation API data carrier DTO
     */
    AppEnsClassificationOutputDTO toResponse(AppEnsClassification model);

    /**
     * Maps incoming flat reference fields into a decoupled domain state instance,
     * resolving relationship IDs (security anchor, identity provider, ENS subjection, personal
     * data processing, security levels) to their respective nested model IDs, while ignoring
     * audit fields. The parent security anchor is mandatory; every other foreign key is optional
     * and, since this always builds a brand-new instance with nothing to preserve, is left fully
     * {@code null} (not an empty stub) when its id is absent from the payload — see
     * {@link #mapIdentityProviderId} and {@link #mapPersonalDataProcessingId} (the LKUP-typed
     * optional fields reuse the equivalent {@code map(Long)} stub already declared on their own
     * mappers). Contrast with {@link #updateModelFromInput}, which preserves rather than nulls out
     * an omitted optional field on an existing instance.
     *
     * @param inputDTO inbound client creation payload containing mapping configuration
     * @return a decoupled domain state instance ready for orchestration processing pipelines
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appSecurity.id", source = "appSecurityId")
    @Mapping(target = "identityProvider", source = "identityProviderId")
    @Mapping(target = "ensSubject", source = "ensSubjectId")
    @Mapping(target = "personalDataProcessing", source = "personalDataProcessingId")
    @Mapping(target = "confidentiality", source = "confidentialityId")
    @Mapping(target = "integrity", source = "integrityId")
    @Mapping(target = "traceability", source = "traceabilityId")
    @Mapping(target = "availability", source = "availabilityId")
    @Mapping(target = "authenticity", source = "authenticityId")
    @Mapping(target = "overallGrade", source = "overallGradeId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppEnsClassification toModelFromInput(AppEnsClassificationInputDTO inputDTO);

    /**
     * Integrates update parameters from flat payload tracking definitions directly over an active
     * business entity, avoiding logical soft-delete and primary key attribute modifications. Unlike
     * {@link #toModelFromInput}, an omitted optional foreign key ({@code
     * NullValuePropertyMappingStrategy.IGNORE} on every nested {@code .id} mapping below) leaves
     * the existing nested value on {@code model} untouched rather than nulling it out — this is a
     * partial update, so a field absent from the payload means "no change", not "clear it".
     *
     * @param inputDTO delta parameter updates tracking values payload DTO
     * @param model    the active operational business graph container targeted for modifier updates
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appSecurity.id", source = "appSecurityId")
    @Mapping(target = "identityProvider.id", source = "identityProviderId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "ensSubject.id", source = "ensSubjectId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "personalDataProcessing.id", source = "personalDataProcessingId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "confidentiality.id", source = "confidentialityId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "integrity.id", source = "integrityId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "traceability.id", source = "traceabilityId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "availability.id", source = "availabilityId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "authenticity.id", source = "authenticityId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "overallGrade.id", source = "overallGradeId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(AppEnsClassificationInputDTO inputDTO, @MappingTarget AppEnsClassification model);

    /**
     * Resolves an {@link IdentityProvider} stub from its identifier, or {@code null} if the
     * identifier itself is {@code null} — this field is optional on {@link AppEnsClassification}.
     *
     * @param identityProviderId the identity provider identifier, possibly {@code null}
     * @return a stub carrying only the given id, or {@code null}
     */
    default IdentityProvider mapIdentityProviderId(Long identityProviderId) {
        if (identityProviderId == null) {
            return null;
        }
        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setId(identityProviderId);
        return identityProvider;
    }

    /**
     * Resolves a {@link PersonalDataProcessing} stub from its identifier, or {@code null} if the
     * identifier itself is {@code null} — this field is optional on {@link AppEnsClassification}.
     *
     * @param personalDataProcessingId the personal data processing identifier, possibly {@code null}
     * @return a stub carrying only the given id, or {@code null}
     */
    default PersonalDataProcessing mapPersonalDataProcessingId(Long personalDataProcessingId) {
        if (personalDataProcessingId == null) {
            return null;
        }
        PersonalDataProcessing personalDataProcessing = new PersonalDataProcessing();
        personalDataProcessing.setId(personalDataProcessingId);
        return personalDataProcessing;
    }

}
