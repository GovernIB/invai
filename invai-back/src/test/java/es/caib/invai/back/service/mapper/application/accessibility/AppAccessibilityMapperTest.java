package es.caib.invai.back.service.mapper.application.accessibility;

import es.caib.invai.back.interna.application.accessibility.DTO.AppAccessibilityInputDTO;
import es.caib.invai.back.interna.application.accessibility.DTO.AppAccessibilityOutputDTO;
import es.caib.invai.back.persistence.model.application.accessibility.AppAccessibilityEntity;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.maintenance.complianceSituation.ComplianceSituationEntity;
import es.caib.invai.back.persistence.model.maintenance.classificationSegment.ClassificationSegmentEntity;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapperImpl;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.complianceSituation.ComplianceSituationMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.category.CategoryMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.commission.CommissionMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.field.FieldMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.general.systemType.SystemTypeMapperImpl;
import es.caib.invai.back.service.mapper.maintenance.classificationSegment.ClassificationSegmentMapperImpl;
import es.caib.invai.back.service.model.application.accessibility.AppAccessibility;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.maintenance.complianceSituation.ComplianceSituation;
import es.caib.invai.back.service.model.maintenance.classificationSegment.ClassificationSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link AppAccessibilityMapperImpl}, exercising every
 * conversion direction declared on {@link AppAccessibilityMapper}.
 *
 * <p>{@link AppAccessibilityMapper} declares {@code uses = {ApplicationMapper.class,
 * ComplianceSituationMapper.class, ClassificationSegmentMapper.class}}. Fully wired
 * implementations of all three are injected into the generated {@code @Autowired} fields via
 * {@link ReflectionTestUtils}. The nested {@code Application}/{@code ApplicationEntity} graph
 * itself is already exhaustively covered by {@code ApplicationMapperTest}/{@code
 * AppSecurityMapperTest}, so a shallow, representative fixture is used here - this test's own
 * job is to lock in AppAccessibility's own scalar fields and its two catalog relations.</p>
 */
class AppAccessibilityMapperTest {

    private AppAccessibilityMapper mapper;

    @BeforeEach
    void setUp() {
        ApplicationMapperImpl applicationMapperImpl = new ApplicationMapperImpl();
        ReflectionTestUtils.setField(applicationMapperImpl, "categoryMapper", new CategoryMapperImpl());
        ReflectionTestUtils.setField(applicationMapperImpl, "systemTypeMapper", new SystemTypeMapperImpl());
        ReflectionTestUtils.setField(applicationMapperImpl, "fieldMapper", new FieldMapperImpl());
        ReflectionTestUtils.setField(applicationMapperImpl, "commissionMapper", new CommissionMapperImpl());
        ReflectionTestUtils.setField(applicationMapperImpl, "statusMapper", new StatusMapperImpl());

        AppAccessibilityMapperImpl impl = new AppAccessibilityMapperImpl();
        ReflectionTestUtils.setField(impl, "applicationMapper", applicationMapperImpl);
        ReflectionTestUtils.setField(impl, "complianceSituationMapper", new ComplianceSituationMapperImpl());
        ReflectionTestUtils.setField(impl, "classificationSegmentMapper", new ClassificationSegmentMapperImpl());
        mapper = impl;
    }

    private AppAccessibilityEntity buildDeepEntity() {
        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(200L);
        applicationEntity.setCode("APP001");
        applicationEntity.setName("Application name");

        ComplianceSituationEntity complianceEntity = new ComplianceSituationEntity();
        complianceEntity.setId(400L);
        complianceEntity.setName("Conforme");
        complianceEntity.setNameEs("Conforme");

        ClassificationSegmentEntity classificationSegmentEntity = new ClassificationSegmentEntity();
        classificationSegmentEntity.setId(401L);
        classificationSegmentEntity.setName("Segment I");
        classificationSegmentEntity.setNameEs("Segmento I");

        AppAccessibilityEntity entity = new AppAccessibilityEntity();
        entity.setId(1L);
        entity.setApplication(applicationEntity);
        entity.setCompliance(complianceEntity);
        entity.setClassificationSegment(classificationSegmentEntity);
        entity.setPublicUrl("https://invai.example.test");
        entity.setMobileApplication(Boolean.TRUE);
        entity.setMobileApplicationName("APP INVAI Android");
        entity.setNonAccessibleContent("PDF antic sense text alternatiu");
        entity.setObservations("Pendent de revisio");
        entity.setExpireDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        entity.setCreatedAt(LocalDateTime.of(2024, 1, 1, 0, 0));
        entity.setCreatedBy("creator");
        entity.setUpdatedAt(LocalDateTime.of(2024, 2, 1, 0, 0));
        entity.setUpdatedBy("updater");
        entity.setDeletedAt(LocalDateTime.of(2024, 3, 1, 0, 0));
        entity.setDeletedBy("deleter");
        return entity;
    }

    private AppAccessibility buildDeepModel() {
        Application application = new Application();
        application.setId(300L);
        application.setCode("APP002");
        application.setName("Application two");

        ComplianceSituation compliance = new ComplianceSituation();
        compliance.setId(410L);
        compliance.setName("Parcialment");
        compliance.setNameEs("Parcialmente");

        ClassificationSegment classificationSegment = new ClassificationSegment();
        classificationSegment.setId(411L);
        classificationSegment.setName("Segment II");
        classificationSegment.setNameEs("Segmento II");

        AppAccessibility model = new AppAccessibility();
        model.setId(2L);
        model.setApplication(application);
        model.setCompliance(compliance);
        model.setClassificationSegment(classificationSegment);
        model.setPublicUrl("https://invai2.example.test");
        model.setMobileApplication(Boolean.FALSE);
        model.setMobileApplicationName("APP INVAI iOS");
        model.setNonAccessibleContent("Video sense subtitols");
        model.setObservations("Revisio programada");
        model.setExpireDate(LocalDateTime.of(2026, 6, 1, 0, 0));
        model.setCreatedAt(LocalDateTime.of(2023, 1, 1, 0, 0));
        model.setCreatedBy("model-creator");
        model.setDeletedAt(LocalDateTime.of(2023, 3, 1, 0, 0));
        model.setDeletedBy("model-deleter");
        return model;
    }

    @Test
    void toModel_deepEntityGraph_mapsEveryOwnField() {
        AppAccessibilityEntity entity = buildDeepEntity();

        AppAccessibility model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("https://invai.example.test", model.getPublicUrl());
        assertEquals(Boolean.TRUE, model.getMobileApplication());
        assertEquals("APP INVAI Android", model.getMobileApplicationName());
        assertEquals("PDF antic sense text alternatiu", model.getNonAccessibleContent());
        assertEquals("Pendent de revisio", model.getObservations());
        assertEquals(entity.getExpireDate(), model.getExpireDate());
        assertEquals("creator", model.getCreatedBy());
        assertEquals("updater", model.getUpdatedBy());
        assertEquals("deleter", model.getDeletedBy());

        assertEquals(200L, model.getApplication().getId());
        assertEquals("APP001", model.getApplication().getCode());

        assertEquals(400L, model.getCompliance().getId());
        assertEquals("Conforme", model.getCompliance().getName());

        assertEquals(401L, model.getClassificationSegment().getId());
        assertEquals("Segment I", model.getClassificationSegment().getName());
    }

    @Test
    void toModel_withNullRelations_leavesNestedModelFieldsNull() {
        AppAccessibilityEntity entity = new AppAccessibilityEntity();
        entity.setId(2L);
        entity.setObservations("No application yet");

        AppAccessibility model = mapper.toModel(entity);

        assertEquals(2L, model.getId());
        assertEquals("No application yet", model.getObservations());
        assertNull(model.getApplication());
        assertNull(model.getCompliance());
        assertNull(model.getClassificationSegment());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_deepModelGraph_mapsEveryOwnField() {
        AppAccessibility model = buildDeepModel();

        AppAccessibilityEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("https://invai2.example.test", entity.getPublicUrl());
        assertEquals(Boolean.FALSE, entity.getMobileApplication());
        assertEquals("APP INVAI iOS", entity.getMobileApplicationName());
        assertEquals("Video sense subtitols", entity.getNonAccessibleContent());
        assertEquals("Revisio programada", entity.getObservations());
        assertEquals(model.getExpireDate(), entity.getExpireDate());
        assertEquals("model-creator", entity.getCreatedBy());
        assertEquals("model-deleter", entity.getDeletedBy());

        assertEquals(300L, entity.getApplication().getId());
        assertEquals("APP002", entity.getApplication().getCode());

        assertEquals(410L, entity.getCompliance().getId());
        assertEquals("Parcialment", entity.getCompliance().getName());

        assertEquals(411L, entity.getClassificationSegment().getId());
        assertEquals("Segment II", entity.getClassificationSegment().getName());
    }

    @Test
    void toEntity_withNullRelations_leavesNestedEntityFieldsNull() {
        AppAccessibility model = new AppAccessibility();
        model.setId(4L);
        model.setObservations("No application yet");

        AppAccessibilityEntity entity = mapper.toEntity(model);

        assertEquals(4L, entity.getId());
        assertEquals("No application yet", entity.getObservations());
        assertNull(entity.getApplication());
        assertNull(entity.getCompliance());
        assertNull(entity.getClassificationSegment());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toResponse_deepModel_flattensEveryOwnFieldIntoOutputDTO() {
        AppAccessibility model = buildDeepModel();

        AppAccessibilityOutputDTO response = mapper.toResponse(model);

        assertEquals(2L, response.getId());
        assertEquals("https://invai2.example.test", response.getPublicUrl());
        assertEquals(Boolean.FALSE, response.getMobileApplication());
        assertEquals("APP INVAI iOS", response.getMobileApplicationName());
        assertEquals("Video sense subtitols", response.getNonAccessibleContent());
        assertEquals("Revisio programada", response.getObservations());
        assertEquals(model.getDeletedAt(), response.getDeletedAt());

        assertEquals(300L, response.getApplication().getId());
        assertEquals("APP002", response.getApplication().getCode());

        assertEquals(410L, response.getCompliance().getId());
        assertEquals("Parcialment", response.getCompliance().getName());

        assertEquals(411L, response.getClassificationSegment().getId());
        assertEquals("Segment II", response.getClassificationSegment().getName());
    }

    @Test
    void toResponse_withNullRelations_leavesNestedDtoFieldsNull() {
        AppAccessibility model = new AppAccessibility();
        model.setId(6L);
        model.setObservations("No application yet");

        AppAccessibilityOutputDTO response = mapper.toResponse(model);

        assertEquals(6L, response.getId());
        assertNull(response.getApplication());
        assertNull(response.getCompliance());
        assertNull(response.getClassificationSegment());
    }

    @Test
    void toResponse_null_returnsNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void toModelFromInput_resolvesRelationIdsAndIgnoresAuditFields() {
        AppAccessibilityInputDTO inputDTO = new AppAccessibilityInputDTO(
                50L, 400L, 401L, "https://invai.example.test",
                Boolean.TRUE, "APP INVAI Android", "PDF antic", "Observacio", LocalDateTime.of(2026, 1, 1, 0, 0));

        AppAccessibility model = mapper.toModelFromInput(inputDTO);

        assertEquals(50L, model.getApplication().getId());
        assertEquals(400L, model.getCompliance().getId());
        assertEquals(401L, model.getClassificationSegment().getId());
        assertEquals("APP INVAI Android", model.getMobileApplicationName());
        assertEquals("PDF antic", model.getNonAccessibleContent());
        assertEquals("Observacio", model.getObservations());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getCreatedBy());
        assertNull(model.getUpdatedAt());
        assertNull(model.getUpdatedBy());
        assertNull(model.getDeletedAt());
        assertNull(model.getDeletedBy());
    }

    @Test
    void toModelFromInput_nullRelationIds_leavesRelationIdsNull() {
        AppAccessibilityInputDTO inputDTO = new AppAccessibilityInputDTO(
                50L, null, null, null, null, null, null, "New notes", null);

        AppAccessibility model = mapper.toModelFromInput(inputDTO);

        assertEquals(50L, model.getApplication().getId());
        // NullValuePropertyMappingStrategy.IGNORE skips setting the nested id, but MapStruct
        // still instantiates the nested target object itself (same as AppSecurityMeasureMapper's
        // type.id/ensRequirement.id handling).
        assertNull(model.getCompliance().getId());
        assertNull(model.getClassificationSegment().getId());
        assertEquals("New notes", model.getObservations());
    }

    @Test
    void toModelFromInput_null_returnsNull() {
        assertNull(mapper.toModelFromInput(null));
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        Application existingApplication = new Application();
        existingApplication.setId(60L);
        AppAccessibility existing = new AppAccessibility();
        existing.setId(4L);
        existing.setApplication(existingApplication);
        existing.setObservations("Old notes");
        AppAccessibilityInputDTO inputDTO = new AppAccessibilityInputDTO(
                70L, 402L, 403L, null, null, null, null, "New notes", null);

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals(70L, existing.getApplication().getId());
        assertEquals(402L, existing.getCompliance().getId());
        assertEquals(403L, existing.getClassificationSegment().getId());
        assertEquals("New notes", existing.getObservations());
    }

    @Test
    void updateModelFromInput_null_doesNothing() {
        AppAccessibility existing = new AppAccessibility();
        existing.setId(7L);
        existing.setObservations("untouched");

        mapper.updateModelFromInput(null, existing);

        assertEquals(7L, existing.getId());
        assertEquals("untouched", existing.getObservations());
    }
}
