package es.caib.invai.back.service.mapper.maintenance.systems.server;

import es.caib.invai.back.interna.maintenance.systems.environment.DTO.EnvironmentOutputDTO;
import es.caib.invai.back.interna.maintenance.systems.server.DTO.ServerInputDTO;
import es.caib.invai.back.interna.maintenance.systems.server.DTO.ServerOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.systems.environment.EnvironmentEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.server.ServerEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.serverType.LkupServerTypeEntity;
import es.caib.invai.back.service.model.maintenance.systems.environment.Environment;
import es.caib.invai.back.service.model.maintenance.systems.server.Server;
import es.caib.invai.back.service.model.maintenance.systems.serverType.ServerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import es.caib.invai.back.service.mapper.maintenance.systems.environment.EnvironmentMapper;
import es.caib.invai.back.service.mapper.maintenance.systems.serverType.ServerTypeMapper;

/**
 * Unit tests for the generated {@link ServerMapperImpl}, exercising every conversion direction
 * declared on {@link ServerMapper}, including its {@link EnvironmentMapper} and
 * {@link ServerTypeMapper} nested collaborators.
 */
@ExtendWith(MockitoExtension.class)
class ServerMapperTest {

    @Mock
    private EnvironmentMapper environmentMapper;

    @Mock
    private ServerTypeMapper serverTypeMapper;

    private ServerMapper mapper;

    @BeforeEach
    void setUp() {
        ServerMapperImpl impl = new ServerMapperImpl();
        ReflectionTestUtils.setField(impl, "environmentMapper", environmentMapper);
        ReflectionTestUtils.setField(impl, "serverTypeMapper", serverTypeMapper);
        mapper = impl;
    }

    @Test
    void toModel_mapsEveryEntityField() {
        ServerEntity entity = new ServerEntity();
        entity.setId(1L);
        entity.setName("srv-01");
        EnvironmentEntity envEntity = new EnvironmentEntity();
        LkupServerTypeEntity typeEntity = new LkupServerTypeEntity();
        entity.setEnvironment(envEntity);
        entity.setServerType(typeEntity);
        entity.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        entity.setCreatedBy("admin");
        entity.setUpdatedAt(LocalDateTime.of(2026, 2, 1, 0, 0));
        entity.setUpdatedBy("admin2");
        entity.setDeletedAt(LocalDateTime.of(2026, 3, 1, 0, 0));
        entity.setDeletedBy("admin3");

        Environment envModel = new Environment();
        ServerType typeModel = new ServerType();
        when(environmentMapper.toModel(envEntity)).thenReturn(envModel);
        when(serverTypeMapper.toModel(typeEntity)).thenReturn(typeModel);

        Server model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("srv-01", model.getName());
        assertSame(envModel, model.getEnvironment());
        assertSame(typeModel, model.getServerType());
        assertEquals(entity.getCreatedAt(), model.getCreatedAt());
        assertEquals(entity.getCreatedBy(), model.getCreatedBy());
        assertEquals(entity.getUpdatedAt(), model.getUpdatedAt());
        assertEquals(entity.getUpdatedBy(), model.getUpdatedBy());
        assertEquals(entity.getDeletedAt(), model.getDeletedAt());
        assertEquals(entity.getDeletedBy(), model.getDeletedBy());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        Server model = new Server();
        model.setId(2L);
        model.setName("srv-02");
        Environment envModel = new Environment();
        ServerType typeModel = new ServerType();
        model.setEnvironment(envModel);
        model.setServerType(typeModel);

        EnvironmentEntity envEntity = new EnvironmentEntity();
        LkupServerTypeEntity typeEntity = new LkupServerTypeEntity();
        when(environmentMapper.toEntity(envModel)).thenReturn(envEntity);
        when(serverTypeMapper.toEntity(typeModel)).thenReturn(typeEntity);

        ServerEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("srv-02", entity.getName());
        assertSame(envEntity, entity.getEnvironment());
        assertSame(typeEntity, entity.getServerType());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTOWithoutConvertingServerType() {
        Server model = new Server();
        model.setId(3L);
        model.setName("srv-03");
        Environment envModel = new Environment();
        ServerType typeModel = new ServerType();
        typeModel.setId(9L);
        model.setEnvironment(envModel);
        model.setServerType(typeModel);
        model.setDeletedAt(LocalDateTime.of(2026, 4, 1, 0, 0));

        EnvironmentOutputDTO envDto = new EnvironmentOutputDTO();
        when(environmentMapper.toResponse(envModel)).thenReturn(envDto);

        ServerOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("srv-03", response.getName());
        assertSame(envDto, response.getEnvironment());
        // ServerMapper passes the domain ServerType through untouched (no ServerTypeMapper conversion).
        assertSame(typeModel, response.getServerType());
        assertEquals(model.getDeletedAt(), response.getDeletedAt());
        verifyNoInteractions(serverTypeMapper);
    }

    @Test
    void toResponse_null_returnsNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void toModelFromInput_setsNestedIdsAndIgnoresAuditFields() {
        ServerInputDTO inputDTO = new ServerInputDTO();
        inputDTO.setName("New Server");
        inputDTO.setEnvironmentId(5L);
        inputDTO.setServerTypeId(7L);

        Server model = mapper.toModelFromInput(inputDTO);

        assertEquals("New Server", model.getName());
        assertEquals(5L, model.getEnvironment().getId());
        assertEquals(7L, model.getServerType().getId());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void toModelFromInput_null_returnsNull() {
        assertNull(mapper.toModelFromInput(null));
    }

    @Test
    void updateModelFromInput_createsNestedObjectsWhenMissing() {
        Server existing = new Server();
        existing.setId(4L);
        ServerInputDTO inputDTO = new ServerInputDTO();
        inputDTO.setName("Updated Server");
        inputDTO.setEnvironmentId(10L);
        inputDTO.setServerTypeId(20L);

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("Updated Server", existing.getName());
        assertNotNull(existing.getEnvironment());
        assertEquals(10L, existing.getEnvironment().getId());
        assertNotNull(existing.getServerType());
        assertEquals(20L, existing.getServerType().getId());
    }

    @Test
    void updateModelFromInput_preservesExistingNestedFieldsOtherThanId() {
        Server existing = new Server();
        Environment env = new Environment();
        env.setId(1L);
        env.setCode("PROD");
        existing.setEnvironment(env);
        ServerType type = new ServerType();
        type.setId(2L);
        type.setCode("DATABASE");
        existing.setServerType(type);

        ServerInputDTO inputDTO = new ServerInputDTO();
        inputDTO.setName("X");
        inputDTO.setEnvironmentId(99L);
        inputDTO.setServerTypeId(88L);

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(99L, existing.getEnvironment().getId());
        assertEquals("PROD", existing.getEnvironment().getCode());
        assertEquals(88L, existing.getServerType().getId());
        assertEquals("DATABASE", existing.getServerType().getCode());
    }
}
