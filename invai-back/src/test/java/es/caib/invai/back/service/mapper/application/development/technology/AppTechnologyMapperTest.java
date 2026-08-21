package es.caib.invai.back.service.mapper.application.development.technology;

import es.caib.invai.back.interna.application.development.technology.DTO.AppTechnologyInputDTO;
import es.caib.invai.back.interna.application.development.technology.DTO.AppTechnologyOutputDTO;
import es.caib.invai.back.interna.maintenance.development.layer.DTO.LayerOutputDTO;
import es.caib.invai.back.interna.maintenance.development.technology.DTO.TechnologyOutputDTO;
import es.caib.invai.back.service.model.application.development.technology.AppTechnology;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.service.model.maintenance.development.layer.Layer;
import es.caib.invai.back.service.model.maintenance.development.technology.Technology;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;
import es.caib.invai.back.service.mapper.maintenance.development.layer.LayerMapper;
import es.caib.invai.back.service.mapper.maintenance.development.technology.TechnologyMapper;
import es.caib.invai.back.persistence.model.application.development.core.AppDevelopmentEntity;
import es.caib.invai.back.persistence.model.application.development.technology.AppTechnologyEntity;
import es.caib.invai.back.persistence.model.maintenance.development.layer.LayerEntity;
import es.caib.invai.back.persistence.model.maintenance.development.technology.TechnologyEntity;

/**
 * Unit tests for the generated {@link AppTechnologyMapperImpl}, exercising every conversion
 * direction declared on {@link AppTechnologyMapper}.
 *
 * <p>{@link AppTechnologyMapper} declares {@code uses = {DevelopmentMapper.class, LayerMapper.class,
 * TechnologyMapper.class}}, so the generated implementation depends on those collaborators via
 * field injection. Since there is no Spring context in a pure unit test, the collaborators are
 * mocked and wired into the private {@code @Autowired} fields via reflection.
 */
@ExtendWith(MockitoExtension.class)
class AppTechnologyMapperTest {

    @Mock
    private es.caib.invai.back.service.mapper.application.development.core.AppDevelopmentMapper appDevelopmentMapper;

    @Mock
    private LayerMapper layerMapper;

    @Mock
    private TechnologyMapper technologyMapper;

    private AppTechnologyMapper mapper;

    @BeforeEach
    void setUp() throws Exception {
        AppTechnologyMapperImpl impl = new AppTechnologyMapperImpl();
        setField(impl, "appDevelopmentMapper", appDevelopmentMapper);
        setField(impl, "layerMapper", layerMapper);
        setField(impl, "technologyMapper", technologyMapper);
        mapper = impl;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void toModel_mapsEveryEntityFieldAndDelegatesNestedAssociations() {
        AppTechnologyEntity entity = new AppTechnologyEntity();
        entity.setId(1L);
        entity.setVersion("1.2.3");
        entity.setArchitecture("x86_64");
        entity.setCreatedAt(LocalDateTime.of(2023, 1, 1, 0, 0));
        entity.setDeletedBy("deleter");
        AppDevelopmentEntity devEntity = new AppDevelopmentEntity();
        LayerEntity layerEntity = new LayerEntity();
        TechnologyEntity techEntity = new TechnologyEntity();
        entity.setAppDevelopment(devEntity);
        entity.setLayer(layerEntity);
        entity.setTechnology(techEntity);

        AppDevelopment devModel = new AppDevelopment();
        Layer layerModel = new Layer();
        Technology techModel = new Technology();
        when(appDevelopmentMapper.toModel(devEntity)).thenReturn(devModel);
        when(layerMapper.toModel(layerEntity)).thenReturn(layerModel);
        when(technologyMapper.toModel(techEntity)).thenReturn(techModel);

        AppTechnology model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("1.2.3", model.getVersion());
        assertEquals("x86_64", model.getArchitecture());
        assertEquals("deleter", model.getDeletedBy());
        assertSame(devModel, model.getAppDevelopment());
        assertSame(layerModel, model.getLayer());
        assertSame(techModel, model.getTechnology());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelFieldAndDelegatesNestedAssociations() {
        AppTechnology model = new AppTechnology();
        model.setId(2L);
        model.setVersion("2.0.0");
        model.setArchitecture("ARM");
        AppDevelopment devModel = new AppDevelopment();
        Layer layerModel = new Layer();
        Technology techModel = new Technology();
        model.setAppDevelopment(devModel);
        model.setLayer(layerModel);
        model.setTechnology(techModel);

        AppDevelopmentEntity devEntity = new AppDevelopmentEntity();
        LayerEntity layerEntity = new LayerEntity();
        TechnologyEntity techEntity = new TechnologyEntity();
        when(appDevelopmentMapper.toEntity(devModel)).thenReturn(devEntity);
        when(layerMapper.toEntity(layerModel)).thenReturn(layerEntity);
        when(technologyMapper.toEntity(techModel)).thenReturn(techEntity);

        AppTechnologyEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("2.0.0", entity.getVersion());
        assertEquals("ARM", entity.getArchitecture());
        assertSame(devEntity, entity.getAppDevelopment());
        assertSame(layerEntity, entity.getLayer());
        assertSame(techEntity, entity.getTechnology());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toResponse_flattensDomainModelAndDelegatesNestedMapping() {
        AppTechnology model = new AppTechnology();
        model.setId(3L);
        model.setVersion("3.1.4");
        model.setArchitecture("ARM64");
        model.setDeletedAt(LocalDateTime.of(2024, 6, 1, 0, 0));
        Layer layerModel = new Layer();
        Technology techModel = new Technology();
        model.setLayer(layerModel);
        model.setTechnology(techModel);

        LayerOutputDTO layerOutputDTO = new LayerOutputDTO();
        TechnologyOutputDTO technologyOutputDTO = new TechnologyOutputDTO();
        when(layerMapper.toResponse(layerModel)).thenReturn(layerOutputDTO);
        when(technologyMapper.toResponse(techModel)).thenReturn(technologyOutputDTO);

        AppTechnologyOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("3.1.4", response.getVersion());
        assertEquals("ARM64", response.getArchitecture());
        assertEquals(model.getDeletedAt(), response.getDeletedAt());
        assertSame(layerOutputDTO, response.getLayer());
        assertSame(technologyOutputDTO, response.getTechnology());
    }

    @Test
    void toModelFromInput_setsShallowIdReferencesAndIgnoresAuditFields() {
        AppTechnologyInputDTO inputDTO = new AppTechnologyInputDTO(2L, 3L, 4L, "1.0.0", "monolith");

        AppTechnology model = mapper.toModelFromInput(inputDTO);

        assertEquals(2L, model.getAppDevelopment().getId());
        assertEquals(3L, model.getLayer().getId());
        assertEquals(4L, model.getTechnology().getId());
        assertEquals("1.0.0", model.getVersion());
        assertEquals("monolith", model.getArchitecture());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        AppTechnology existing = new AppTechnology();
        existing.setId(9L);
        existing.setAppDevelopment(AppDevelopment.builder().id(1L).build());
        existing.setLayer(new Layer());
        existing.setTechnology(new Technology());
        AppTechnologyInputDTO inputDTO = new AppTechnologyInputDTO(5L, 6L, 7L, "9.9.9", "microservices");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(9L, existing.getId());
        assertEquals(5L, existing.getAppDevelopment().getId());
        assertEquals(6L, existing.getLayer().getId());
        assertEquals(7L, existing.getTechnology().getId());
        assertEquals("9.9.9", existing.getVersion());
        assertEquals("microservices", existing.getArchitecture());
    }
}
