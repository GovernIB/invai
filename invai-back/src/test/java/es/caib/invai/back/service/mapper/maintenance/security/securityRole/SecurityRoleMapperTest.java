package es.caib.invai.back.service.mapper.maintenance.security.securityRole;

import es.caib.invai.back.interna.maintenance.security.securityRole.DTO.SecurityRoleOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.security.securityRole.SecurityRoleEntity;
import es.caib.invai.back.service.model.maintenance.security.securityRole.SecurityRole;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link SecurityRoleMapper}, locking in the entity/model/DTO field mappings.
 */
class SecurityRoleMapperTest {

    private final SecurityRoleMapper mapper = Mappers.getMapper(SecurityRoleMapper.class);

    @Test
    void toModel_mapsAllFields() {
        SecurityRoleEntity entity = new SecurityRoleEntity();
        entity.setId(1L);
        entity.setRoleId(52L);
        entity.setName("OWS_ADMIN_ROLE");
        entity.setSystem("OWS");
        entity.setDescription("Administrator OWS role");

        SecurityRole model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals(52L, model.getRoleId());
        assertEquals("OWS_ADMIN_ROLE", model.getName());
        assertEquals("OWS", model.getSystem());
        assertEquals("Administrator OWS role", model.getDescription());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toResponse_mapsAllFields() {
        SecurityRole model = new SecurityRole(1L, 52L, "OWS_ADMIN_ROLE", "OWS", "Administrator OWS role");

        SecurityRoleOutputDTO dto = mapper.toResponse(model);

        assertEquals(1L, dto.getId());
        assertEquals(52L, dto.getRoleId());
        assertEquals("OWS_ADMIN_ROLE", dto.getName());
        assertEquals("OWS", dto.getSystem());
        assertEquals("Administrator OWS role", dto.getDescription());
    }

    @Test
    void toResponse_null_returnsNull() {
        assertNull(mapper.toResponse(null));
    }
}
