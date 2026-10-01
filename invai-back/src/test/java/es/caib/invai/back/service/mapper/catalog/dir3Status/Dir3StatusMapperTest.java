package es.caib.invai.back.service.mapper.catalog.dir3Status;

import es.caib.invai.back.persistence.model.catalog.dir3Status.LkupDir3StatusEntity;
import es.caib.invai.back.service.model.catalog.dir3Status.Dir3Status;
import es.caib.invai.back.service.model.catalog.dir3Status.Dir3ValidationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the generated {@link Dir3StatusMapperImpl}, exercising every conversion direction
 * declared on {@link Dir3StatusMapper}, mirroring {@code StatusMapperTest}.
 */
class Dir3StatusMapperTest {

    private Dir3StatusMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new Dir3StatusMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        LkupDir3StatusEntity entity = new LkupDir3StatusEntity();
        entity.setId(1L);
        entity.setCode("VALIDATED");
        entity.setName("Validat");
        entity.setNameEs("Validado");

        Dir3Status model = mapper.toModel(entity);

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
        Dir3Status model = new Dir3Status();
        model.setId(2L);
        model.setCode("NOT_VALIDATED");
        model.setName("No validat");
        model.setNameEs("No validado");

        LkupDir3StatusEntity entity = mapper.toEntity(model);

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
        Dir3Status stub = mapper.map(3L);

        assertEquals(3L, stub.getId());
        assertNull(stub.getName());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }

    @Test
    void mapLongToDir3ValidationStatus_resolvesEveryKnownId() {
        assertEquals(Dir3ValidationStatus.VALIDATED, mapper.mapLongToDir3ValidationStatus(1L));
        assertEquals(Dir3ValidationStatus.NOT_VALIDATED, mapper.mapLongToDir3ValidationStatus(2L));
        assertEquals(Dir3ValidationStatus.MANUAL, mapper.mapLongToDir3ValidationStatus(3L));
        assertEquals(Dir3ValidationStatus.NOT_APPLY, mapper.mapLongToDir3ValidationStatus(4L));
    }

    @Test
    void mapLongToDir3ValidationStatus_null_returnsNull() {
        assertNull(mapper.mapLongToDir3ValidationStatus(null));
    }

    @Test
    void mapLongToDir3ValidationStatus_unknownId_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mapper.mapLongToDir3ValidationStatus(999L));
        assertEquals("Unknown Dir3Status ID: 999", ex.getMessage());
    }

    @Test
    void mapDir3ValidationStatusToEntity_buildsEntityCarryingOnlyId() {
        LkupDir3StatusEntity entity = mapper.mapDir3ValidationStatusToEntity(Dir3ValidationStatus.MANUAL);

        assertEquals(3L, entity.getId());
        assertNull(entity.getName());
    }

    @Test
    void mapDir3ValidationStatusToEntity_null_returnsNull() {
        assertNull(mapper.mapDir3ValidationStatusToEntity(null));
    }

    @Test
    void mapEntityToDir3ValidationStatus_resolvesMatchingId() {
        LkupDir3StatusEntity entity = new LkupDir3StatusEntity();
        entity.setId(4L);

        assertEquals(Dir3ValidationStatus.NOT_APPLY, mapper.mapEntityToDir3ValidationStatus(entity));
    }

    @Test
    void mapEntityToDir3ValidationStatus_unmatchedId_returnsNull() {
        LkupDir3StatusEntity entity = new LkupDir3StatusEntity();
        entity.setId(999L);

        assertNull(mapper.mapEntityToDir3ValidationStatus(entity));
    }

    @Test
    void mapEntityToDir3ValidationStatus_nullEntity_returnsNull() {
        assertNull(mapper.mapEntityToDir3ValidationStatus(null));
    }

    @Test
    void mapEntityToDir3ValidationStatus_nullId_returnsNull() {
        LkupDir3StatusEntity entity = new LkupDir3StatusEntity();
        entity.setId(null);

        assertNull(mapper.mapEntityToDir3ValidationStatus(entity));
    }
}
