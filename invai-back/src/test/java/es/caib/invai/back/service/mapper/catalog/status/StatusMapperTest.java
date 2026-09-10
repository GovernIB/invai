package es.caib.invai.back.service.mapper.catalog.status;

import es.caib.invai.back.persistence.model.catalog.status.LkupStatusEntity;
import es.caib.invai.back.service.model.catalog.status.Status;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the generated {@link StatusMapperImpl}, exercising every conversion direction
 * declared on {@link StatusMapper}: the plain {@code toModel}/{@code toEntity} row mappings plus
 * every default method that bridges the relational {@link LkupStatusEntity} catalog row and the
 * {@link StatusEnum} domain enumeration ({@code map}, {@code mapLongToStatusEnum},
 * {@code mapStatusEnumToStatusEntity}, {@code mapStatusEntityToStatusEnum}).
 */
class StatusMapperTest {

    private StatusMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new StatusMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        LkupStatusEntity entity = new LkupStatusEntity();
        entity.setId(1L);
        entity.setName("Active");
        entity.setNameEs("Activo");

        Status model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Active", model.getName());
        assertEquals("Activo", model.getNameEs());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        Status model = new Status();
        model.setId(2L);
        model.setName("Inactive");
        model.setNameEs("Inactivo");

        LkupStatusEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Inactive", entity.getName());
        assertEquals("Inactivo", entity.getNameEs());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        Status stub = mapper.map(3L);

        assertEquals(3L, stub.getId());
        assertNull(stub.getName());
        assertNull(stub.getNameEs());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }

    @Test
    void mapLongToStatusEnum_resolvesActiveId() {
        assertEquals(StatusEnum.ACTIVE, mapper.mapLongToStatusEnum(1L));
    }

    @Test
    void mapLongToStatusEnum_resolvesInactiveId() {
        assertEquals(StatusEnum.INACTIVE, mapper.mapLongToStatusEnum(2L));
    }

    @Test
    void mapLongToStatusEnum_null_returnsNull() {
        assertNull(mapper.mapLongToStatusEnum(null));
    }

    @Test
    void mapLongToStatusEnum_unknownId_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mapper.mapLongToStatusEnum(999L));
        assertEquals("Unknown Status ID: 999", ex.getMessage());
    }

    @Test
    void mapStatusEnumToStatusEntity_buildsEntityCarryingOnlyId() {
        LkupStatusEntity entity = mapper.mapStatusEnumToStatusEntity(StatusEnum.ACTIVE);

        assertEquals(1L, entity.getId());
        assertNull(entity.getName());
        assertNull(entity.getNameEs());
    }

    @Test
    void mapStatusEnumToStatusEntity_null_returnsNull() {
        assertNull(mapper.mapStatusEnumToStatusEntity(null));
    }

    @Test
    void mapStatusEntityToStatusEnum_resolvesMatchingId() {
        LkupStatusEntity entity = new LkupStatusEntity();
        entity.setId(2L);

        assertEquals(StatusEnum.INACTIVE, mapper.mapStatusEntityToStatusEnum(entity));
    }

    @Test
    void mapStatusEntityToStatusEnum_unmatchedId_returnsNull() {
        LkupStatusEntity entity = new LkupStatusEntity();
        entity.setId(999L);

        assertNull(mapper.mapStatusEntityToStatusEnum(entity));
    }

    @Test
    void mapStatusEntityToStatusEnum_nullEntity_returnsNull() {
        assertNull(mapper.mapStatusEntityToStatusEnum(null));
    }

    @Test
    void mapStatusEntityToStatusEnum_nullId_returnsNull() {
        LkupStatusEntity entity = new LkupStatusEntity();
        entity.setId(null);

        assertNull(mapper.mapStatusEntityToStatusEnum(entity));
    }
}
