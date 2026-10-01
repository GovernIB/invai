package es.caib.invai.back.service.mapper.application.responsibleAuthorized.dir3;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.dir3.Dir3ValidationEntity;
import es.caib.invai.back.persistence.model.catalog.dir3Status.LkupDir3StatusEntity;
import es.caib.invai.back.service.mapper.catalog.dir3Status.Dir3StatusMapperImpl;
import es.caib.invai.back.service.model.application.responsibleAuthorized.dir3.Dir3Validation;
import es.caib.invai.back.service.model.catalog.dir3Status.Dir3ValidationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link Dir3ValidationMapperImpl}, exercising every conversion
 * direction declared on {@link Dir3ValidationMapper}.
 * <p>
 * {@link Dir3ValidationMapper} declares {@code uses = {Dir3StatusMapper.class}}, so the generated
 * implementation carries an {@code @Autowired} field for that collaborator. Since no Spring
 * context is bootstrapped here, a real {@link Dir3StatusMapperImpl} instance is injected manually
 * via {@link ReflectionTestUtils}, following the same pattern used in {@code AppResponsibleMapperTest}.
 * </p>
 */
class Dir3ValidationMapperTest {

    private Dir3ValidationMapper mapper;

    @BeforeEach
    void setUp() {
        Dir3ValidationMapperImpl mapperImpl = new Dir3ValidationMapperImpl();
        ReflectionTestUtils.setField(mapperImpl, "dir3StatusMapper", new Dir3StatusMapperImpl());
        mapper = mapperImpl;
    }

    @Test
    void toModel_mapsEveryEntityFieldIncludingResolvedDir3Status() {
        Dir3ValidationEntity entity = new Dir3ValidationEntity();
        entity.setId(1L);
        LkupDir3StatusEntity statusEntity = new LkupDir3StatusEntity();
        statusEntity.setId(2L);
        entity.setDir3Status(statusEntity);
        LocalDateTime manualValidatedAt = LocalDateTime.of(2025, 3, 1, 10, 0);
        entity.setManualValidatedAt(manualValidatedAt);
        entity.setManualValidatedBy("jdoe");
        entity.setReason("DIR3 corregit manualment per l'administrador");
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        entity.setCreatedAt(createdAt);
        entity.setCreatedBy("creator");
        LocalDateTime updatedAt = LocalDateTime.of(2025, 2, 1, 0, 0);
        entity.setUpdatedAt(updatedAt);
        entity.setUpdatedBy("updater");

        Dir3Validation model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals(Dir3ValidationStatus.NOT_VALIDATED, model.getDir3Status());
        assertEquals(manualValidatedAt, model.getManualValidatedAt());
        assertEquals("jdoe", model.getManualValidatedBy());
        assertEquals("DIR3 corregit manualment per l'administrador", model.getReason());
        assertEquals(createdAt, model.getCreatedAt());
        assertEquals("creator", model.getCreatedBy());
        assertEquals(updatedAt, model.getUpdatedAt());
        assertEquals("updater", model.getUpdatedBy());
        assertNull(model.getDeletedAt());
        assertNull(model.getDeletedBy());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        Dir3Validation model = Dir3Validation.builder()
                .id(9L)
                .dir3Status(Dir3ValidationStatus.MANUAL)
                .manualValidatedAt(LocalDateTime.of(2025, 3, 1, 10, 0))
                .manualValidatedBy("jdoe")
                .reason("DIR3 corregit manualment per l'administrador")
                .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .createdBy("creator")
                .build();

        Dir3ValidationEntity entity = mapper.toEntity(model);

        assertEquals(9L, entity.getId());
        assertEquals(3L, entity.getDir3Status().getId());
        assertEquals(LocalDateTime.of(2025, 3, 1, 10, 0), entity.getManualValidatedAt());
        assertEquals("jdoe", entity.getManualValidatedBy());
        assertEquals("DIR3 corregit manualment per l'administrador", entity.getReason());
        assertEquals(LocalDateTime.of(2025, 1, 1, 0, 0), entity.getCreatedAt());
        assertEquals("creator", entity.getCreatedBy());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }
}
