package es.caib.invai.back.service.mapper.maintenance.general.category;

import es.caib.invai.back.interna.maintenance.general.category.DTO.CategoryInputDTO;
import es.caib.invai.back.interna.maintenance.general.category.DTO.CategoryOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.general.category.CategoryEntity;
import es.caib.invai.back.service.model.maintenance.general.category.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link CategoryMapperImpl}, exercising every conversion direction
 * declared on {@link CategoryMapper}.
 */
class CategoryMapperTest {

    private CategoryMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CategoryMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        CategoryEntity entity = new CategoryEntity();
        entity.setId(1L);
        entity.setName("Infrastructure");
        entity.setNameEs("Infraestructura");

        Category model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Infrastructure", model.getName());
        assertEquals("Infraestructura", model.getNameEs());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        Category model = new Category();
        model.setId(2L);
        model.setName("Security");
        model.setNameEs("Seguridad");

        CategoryEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Security", entity.getName());
        assertEquals("Seguridad", entity.getNameEs());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        Category model = new Category();
        model.setId(3L);
        model.setName("Networking");
        model.setNameEs("Redes");

        CategoryOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("Networking", response.getName());
        assertEquals("Redes", response.getNameEs());
    }

    @Test
    void toModelFromInput_ignoresAuditFieldsAndId() {
        CategoryInputDTO inputDTO = new CategoryInputDTO("Analytics", "Analitica");

        Category model = mapper.toModelFromInput(inputDTO);

        assertEquals("Analytics", model.getName());
        assertEquals("Analitica", model.getNameEs());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        Category existing = new Category();
        existing.setId(4L);
        existing.setName("Old Name");
        existing.setNameEs("Nombre Antiguo");
        CategoryInputDTO inputDTO = new CategoryInputDTO("New Name", "Nombre Nuevo");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("New Name", existing.getName());
        assertEquals("Nombre Nuevo", existing.getNameEs());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        CategoryOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }
}
