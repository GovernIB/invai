package es.caib.invai.back.service.mapper.maintenance.systems.system;

import es.caib.invai.back.interna.maintenance.systems.server.DTO.ServerOutputDTO;
import es.caib.invai.back.interna.maintenance.systems.system.DTO.SystemInputDTO;
import es.caib.invai.back.interna.maintenance.systems.system.DTO.SystemOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.systems.server.ServerEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.system.SystemEntity;
import es.caib.invai.back.service.model.maintenance.systems.server.Server;
import es.caib.invai.back.service.model.maintenance.systems.system.System;
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
import static org.mockito.Mockito.when;
import es.caib.invai.back.service.mapper.maintenance.systems.server.ServerMapper;

/**
 * Unit tests for the generated {@link SystemMapperImpl}, exercising every conversion direction
 * declared on {@link SystemMapper}, including its {@link ServerMapper} nested collaborator.
 */
@ExtendWith(MockitoExtension.class)
class SystemMapperTest {

    @Mock
    private ServerMapper serverMapper;

    private SystemMapper mapper;

    @BeforeEach
    void setUp() {
        SystemMapperImpl impl = new SystemMapperImpl();
        ReflectionTestUtils.setField(impl, "serverMapper", serverMapper);
        mapper = impl;
    }

    @Test
    void toModel_mapsEveryEntityField() {
        SystemEntity entity = new SystemEntity();
        entity.setId(1L);
        ServerEntity serverEntity = new ServerEntity();
        entity.setServer(serverEntity);
        entity.setInstance("INST1");
        entity.setPort(1521);
        entity.setVersion("1.0");
        entity.setDescription("Production database instance");
        entity.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        entity.setCreatedBy("admin");
        entity.setUpdatedAt(LocalDateTime.of(2026, 2, 1, 0, 0));
        entity.setUpdatedBy("admin2");
        entity.setDeletedAt(LocalDateTime.of(2026, 3, 1, 0, 0));
        entity.setDeletedBy("admin3");

        Server serverModel = new Server();
        when(serverMapper.toModel(serverEntity)).thenReturn(serverModel);

        System model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertSame(serverModel, model.getServer());
        assertEquals("INST1", model.getInstance());
        assertEquals(1521, model.getPort());
        assertEquals("1.0", model.getVersion());
        assertEquals("Production database instance", model.getDescription());
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
        Server serverModel = new Server();
        System model = System.builder()
                .id(2L)
                .server(serverModel)
                .instance("INST2")
                .port(80)
                .version("2.0")
                .description("Secondary instance")
                .build();

        ServerEntity serverEntity = new ServerEntity();
        when(serverMapper.toEntity(serverModel)).thenReturn(serverEntity);

        SystemEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertSame(serverEntity, entity.getServer());
        assertEquals("INST2", entity.getInstance());
        assertEquals(80, entity.getPort());
        assertEquals("2.0", entity.getVersion());
        assertEquals("Secondary instance", entity.getDescription());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        Server serverModel = new Server();
        System model = System.builder()
                .id(3L)
                .server(serverModel)
                .instance("INST3")
                .port(443)
                .version("3.0")
                .description("Reporting instance")
                .deletedAt(LocalDateTime.of(2026, 4, 1, 0, 0))
                .build();

        ServerOutputDTO serverDto = new ServerOutputDTO();
        when(serverMapper.toResponse(serverModel)).thenReturn(serverDto);

        SystemOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertSame(serverDto, response.getServer());
        assertEquals("INST3", response.getInstance());
        assertEquals(443, response.getPort());
        assertEquals("3.0", response.getVersion());
        assertEquals("Reporting instance", response.getDescription());
        assertEquals(model.getDeletedAt(), response.getDeletedAt());
    }

    @Test
    void toResponse_null_returnsNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void toModelFromInput_setsServerIdAndIgnoresAuditFields() {
        SystemInputDTO inputDTO = new SystemInputDTO();
        inputDTO.setServerId(5L);
        inputDTO.setInstance("INST5");
        inputDTO.setPort(21);
        inputDTO.setVersion("5.0");
        inputDTO.setDescription("Desc");

        System model = mapper.toModelFromInput(inputDTO);

        assertEquals(5L, model.getServer().getId());
        assertEquals("INST5", model.getInstance());
        assertEquals(21, model.getPort());
        assertEquals("5.0", model.getVersion());
        assertEquals("Desc", model.getDescription());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void toModelFromInput_null_returnsNull() {
        assertNull(mapper.toModelFromInput(null));
    }

    @Test
    void updateModelFromInput_createsServerWhenMissing() {
        System existing = new System();
        existing.setId(6L);
        SystemInputDTO inputDTO = new SystemInputDTO();
        inputDTO.setServerId(15L);
        inputDTO.setInstance("Updated");
        inputDTO.setPort(99);
        inputDTO.setVersion("6.0");
        inputDTO.setDescription("Updated desc");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(6L, existing.getId());
        assertNotNull(existing.getServer());
        assertEquals(15L, existing.getServer().getId());
        assertEquals("Updated", existing.getInstance());
        assertEquals(99, existing.getPort());
        assertEquals("6.0", existing.getVersion());
        assertEquals("Updated desc", existing.getDescription());
    }

    @Test
    void updateModelFromInput_preservesExistingServerFieldsOtherThanId() {
        System existing = new System();
        Server server = new Server();
        server.setId(1L);
        server.setName("srv-existing");
        existing.setServer(server);

        SystemInputDTO inputDTO = new SystemInputDTO();
        inputDTO.setServerId(77L);
        inputDTO.setInstance("Inst");
        inputDTO.setPort(1);
        inputDTO.setVersion("v");

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(77L, existing.getServer().getId());
        assertEquals("srv-existing", existing.getServer().getName());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        SystemOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map((Long) null));
    }

    @Test
    void map_passThroughSystem_returnsSameInstance() {
        System system = new System();

        assertSame(system, mapper.map(system));
    }

    @Test
    void map_passThroughSystem_null_returnsNull() {
        assertNull(mapper.map((System) null));
    }
}
