package es.caib.invai.back.service.mapper.maintenance.documentation.documentCategory;

import es.caib.invai.back.interna.maintenance.documentation.category.DTO.DocumentCategoryInputDTO;
import es.caib.invai.back.interna.maintenance.documentation.category.DTO.DocumentCategoryOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryEntity;
import es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the generated {@link DocumentCategoryMapperImpl}, exercising every conversion
 * direction declared on {@link DocumentCategoryMapper}.
 */
class DocumentCategoryMapperTest {

    private DocumentCategoryMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DocumentCategoryMapperImpl();
    }

    @Test
    void toModel_subcategory_mapsEveryFieldAndTheParentIdentifier() {
        DocumentCategoryEntity parent = new DocumentCategoryEntity();
        parent.setId(1L);
        DocumentCategoryEntity entity = new DocumentCategoryEntity();
        entity.setId(2L);
        entity.setName("API");
        entity.setNameEs("API es");
        entity.setParent(parent);
        entity.setDeletedAt(LocalDateTime.of(2026, 10, 8, 9, 0));
        entity.setDeletedBy("jdoe");

        DocumentCategory model = mapper.toModel(entity);

        assertEquals(2L, model.getId());
        assertEquals("API", model.getName());
        assertEquals("API es", model.getNameEs());
        assertEquals(1L, model.getParentId());
        assertEquals(LocalDateTime.of(2026, 10, 8, 9, 0), model.getDeletedAt());
        assertEquals("jdoe", model.getDeletedBy());
        assertTrue(model.getSubcategories().isEmpty());
        assertTrue(!model.isCategory());
    }

    @Test
    void toModel_category_hasNoParent() {
        DocumentCategoryEntity entity = new DocumentCategoryEntity();
        entity.setId(1L);
        entity.setName("Tècnica");

        DocumentCategory model = mapper.toModel(entity);

        assertNull(model.getParentId());
        assertTrue(model.isCategory());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_subcategory_carriesTheParentAsAReferenceHoldingOnlyItsId() {
        DocumentCategory model = new DocumentCategory();
        model.setId(2L);
        model.setName("API");
        model.setNameEs("API es");
        model.setParentId(1L);

        DocumentCategoryEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("API", entity.getName());
        assertEquals("API es", entity.getNameEs());
        assertEquals(1L, entity.getParent().getId());
        assertNull(entity.getParent().getName());
    }

    @Test
    void toEntity_category_hasNoParent() {
        DocumentCategory model = new DocumentCategory();
        model.setName("Tècnica");

        assertNull(mapper.toEntity(model).getParent());
    }

    @Test
    void toResponse_category_exposesItsSubcategories() {
        DocumentCategory child = new DocumentCategory();
        child.setId(2L);
        child.setName("API");
        child.setParentId(1L);
        DocumentCategory category = new DocumentCategory();
        category.setId(1L);
        category.setName("Tècnica");
        category.setNameEs("Técnica");
        category.setSubcategories(List.of(child));

        DocumentCategoryOutputDTO dto = mapper.toResponse(category);

        assertEquals(1L, dto.getId());
        assertEquals("Tècnica", dto.getName());
        assertEquals("Técnica", dto.getNameEs());
        assertNull(dto.getParentId());
        assertEquals(1, dto.getSubcategories().size());
        assertEquals(2L, dto.getSubcategories().get(0).getId());
        assertEquals(1L, dto.getSubcategories().get(0).getParentId());
        assertTrue(dto.getSubcategories().get(0).getSubcategories().isEmpty());
    }

    @Test
    void toModelFromInput_mapsTheNamesAndTheParentAndLeavesTheRestUnset() {
        DocumentCategory model = mapper.toModelFromInput(new DocumentCategoryInputDTO("API", "API es", 1L));

        assertEquals("API", model.getName());
        assertEquals("API es", model.getNameEs());
        assertEquals(1L, model.getParentId());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
        assertTrue(model.getSubcategories().isEmpty());
    }

    @Test
    void updateModelFromInput_overwritesTheEditableFieldsAndKeepsIdentityAuditAndSubcategories() {
        DocumentCategory child = new DocumentCategory();
        DocumentCategory model = new DocumentCategory();
        model.setId(7L);
        model.setName("old");
        model.setNameEs("old es");
        model.setParentId(1L);
        model.setCreatedBy("creator");
        model.setSubcategories(List.of(child));

        mapper.updateModelFromInput(new DocumentCategoryInputDTO("new", "new es", 5L), model);

        assertEquals(7L, model.getId());
        assertEquals("new", model.getName());
        assertEquals("new es", model.getNameEs());
        assertEquals(5L, model.getParentId());
        assertEquals("creator", model.getCreatedBy());
        assertEquals(List.of(child), model.getSubcategories());
    }

    @Test
    void mapParentReference_nullId_returnsNull() {
        assertNull(mapper.mapParentReference(null));
    }

    @Test
    void mapParentReference_id_returnsAReferenceCarryingOnlyIt() {
        DocumentCategoryEntity reference = mapper.mapParentReference(3L);

        assertEquals(3L, reference.getId());
        assertNull(reference.getName());
    }
}
