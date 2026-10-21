package es.caib.invai.back.service.mapper.catalog.validationStatus;

import es.caib.invai.back.persistence.model.catalog.validationStatus.LkupValidationStatusEntity;
import es.caib.invai.back.service.model.catalog.validationStatus.ValidationStatus;
import es.caib.invai.back.service.model.catalog.validationStatus.ValidationStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the generated {@link ValidationStatusMapperImpl}, exercising every conversion direction
 * declared on {@link ValidationStatusMapper}, mirroring {@code StatusMapperTest}.
 */
class ValidationStatusMapperTest {

    private ValidationStatusMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ValidationStatusMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        LkupValidationStatusEntity entity = new LkupValidationStatusEntity();
        entity.setId(1L);
        entity.setCode("VALIDATED");
        entity.setName("Validat");
        entity.setNameEs("Validado");

        ValidationStatus model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("VALIDATED", model.getCode());
        assertEquals("Validat", model.getName());
        assertEquals("Validado", model.getNameEs());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        ValidationStatus model = new ValidationStatus();
        model.setId(2L);
        model.setCode("NOT_VALIDATED");
        model.setName("No validat");
        model.setNameEs("No validado");

        LkupValidationStatusEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("NOT_VALIDATED", entity.getCode());
        assertEquals("No validat", entity.getName());
        assertEquals("No validado", entity.getNameEs());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        ValidationStatus stub = mapper.map(3L);

        assertEquals(3L, stub.getId());
        assertNull(stub.getName());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }

    @Test
    void mapLongToValidationStatusEnum_resolvesEveryKnownId() {
        assertEquals(ValidationStatusEnum.VALIDATED, mapper.mapLongToValidationStatusEnum(1L));
        assertEquals(ValidationStatusEnum.NOT_VALIDATED, mapper.mapLongToValidationStatusEnum(2L));
        assertEquals(ValidationStatusEnum.MANUAL, mapper.mapLongToValidationStatusEnum(3L));
        assertEquals(ValidationStatusEnum.NOT_APPLY, mapper.mapLongToValidationStatusEnum(4L));
    }

    @Test
    void mapLongToValidationStatusEnum_null_returnsNull() {
        assertNull(mapper.mapLongToValidationStatusEnum(null));
    }

    @Test
    void mapLongToValidationStatusEnum_unknownId_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mapper.mapLongToValidationStatusEnum(999L));
        assertEquals("Unknown ValidationStatus ID: 999", ex.getMessage());
    }

    @Test
    void mapValidationStatusEnumToEntity_buildsEntityCarryingOnlyId() {
        LkupValidationStatusEntity entity = mapper.mapValidationStatusEnumToEntity(ValidationStatusEnum.MANUAL);

        assertEquals(3L, entity.getId());
        assertNull(entity.getName());
    }

    @Test
    void mapValidationStatusEnumToEntity_null_returnsNull() {
        assertNull(mapper.mapValidationStatusEnumToEntity(null));
    }

    @Test
    void mapEntityToValidationStatusEnum_resolvesMatchingId() {
        LkupValidationStatusEntity entity = new LkupValidationStatusEntity();
        entity.setId(4L);

        assertEquals(ValidationStatusEnum.NOT_APPLY, mapper.mapEntityToValidationStatusEnum(entity));
    }

    @Test
    void mapEntityToValidationStatusEnum_unmatchedId_returnsNull() {
        LkupValidationStatusEntity entity = new LkupValidationStatusEntity();
        entity.setId(999L);

        assertNull(mapper.mapEntityToValidationStatusEnum(entity));
    }

    @Test
    void mapEntityToValidationStatusEnum_nullEntity_returnsNull() {
        assertNull(mapper.mapEntityToValidationStatusEnum(null));
    }

    @Test
    void mapEntityToValidationStatusEnum_nullId_returnsNull() {
        LkupValidationStatusEntity entity = new LkupValidationStatusEntity();
        entity.setId(null);

        assertNull(mapper.mapEntityToValidationStatusEnum(entity));
    }
}
