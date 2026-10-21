package es.caib.invai.back.service.mapper.maintenance.responsible.roleTransfer;

import es.caib.invai.back.interna.catalog.responsibleType.DTO.ResponsibleTypeOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleAssignmentOutputDTO;
import es.caib.invai.back.service.mapper.catalog.responsibleType.ResponsibleTypeMapperImpl;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import es.caib.invai.back.service.model.catalog.responsibleType.ResponsibleType;
import es.caib.invai.back.service.model.maintenance.responsible.roleTransfer.RoleAssignmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link RoleAssignmentMapperImpl}. Since no Spring context is bootstrapped,
 * the real {@link ResponsibleTypeMapperImpl} it {@code uses} is injected through {@link ReflectionTestUtils}.
 */
class RoleAssignmentMapperTest {

    private RoleAssignmentMapperImpl mapper;
    private AppResponsibleAuthorized anchor;

    @BeforeEach
    void setUp() {
        mapper = new RoleAssignmentMapperImpl();
        ReflectionTestUtils.setField(mapper, "responsibleTypeMapper", new ResponsibleTypeMapperImpl());

        Application application = new Application();
        application.setId(30L);
        application.setName("Application One");
        anchor = AppResponsibleAuthorized.builder().id(40L).application(application).build();
    }

    @Test
    void fromResponsible_mapsIdApplicationAndResponsibleType() {
        ResponsibleType responsibleType = new ResponsibleType();
        responsibleType.setId(6L);
        responsibleType.setName("Responsable de Servei");
        responsibleType.setNameEs("Responsable de Servicio");
        AppResponsible responsible = AppResponsible.builder()
                .id(1L).appResponsibleAuthorized(anchor).responsibleType(responsibleType).build();

        RoleAssignmentOutputDTO dto = mapper.fromResponsible(responsible);

        assertEquals(1L, dto.getId());
        assertEquals(RoleAssignmentType.RESPONSIBLE, dto.getType());
        assertEquals(30L, dto.getApplicationId());
        assertEquals("Application One", dto.getApplicationName());
        ResponsibleTypeOutputDTO type = dto.getResponsibleType();
        assertEquals(6L, type.getId());
        assertEquals("Responsable de Servei", type.getName());
        assertEquals("Responsable de Servicio", type.getNameEs());
        assertNull(dto.getAuthorizationTypes());
    }

    @Test
    void fromResponsible_withoutResponsibleTypeOrApplication_leavesThoseFieldsNull() {
        AppResponsible responsible = AppResponsible.builder().id(1L).appResponsibleAuthorized(
                AppResponsibleAuthorized.builder().id(40L).build()).build();

        RoleAssignmentOutputDTO dto = mapper.fromResponsible(responsible);

        assertEquals(1L, dto.getId());
        assertNull(dto.getApplicationId());
        assertNull(dto.getApplicationName());
        assertNull(dto.getResponsibleType());
    }

    @Test
    void fromAuthorized_mapsIdApplicationAndTheGivenAuthorizationTypes() {
        AppAuthorized authorized = AppAuthorized.builder().id(2L).appResponsibleAuthorized(anchor).build();
        List<AuthorizationTypeOutputDTO> types =
                List.of(new AuthorizationTypeOutputDTO(5L, "Firmar peticiones", "Signar peticions", null));

        RoleAssignmentOutputDTO dto = mapper.fromAuthorized(authorized, types);

        assertEquals(2L, dto.getId());
        assertEquals(RoleAssignmentType.AUTHORIZED, dto.getType());
        assertEquals(30L, dto.getApplicationId());
        assertEquals("Application One", dto.getApplicationName());
        assertEquals(types, dto.getAuthorizationTypes());
        assertNull(dto.getResponsibleType());
    }

    @Test
    void fromAuthorized_withNoTypes_keepsTheEmptyList() {
        AppAuthorized authorized = AppAuthorized.builder().id(2L).appResponsibleAuthorized(anchor).build();
        List<AuthorizationTypeOutputDTO> none = List.of();

        RoleAssignmentOutputDTO dto = mapper.fromAuthorized(authorized, none);

        assertEquals(none, dto.getAuthorizationTypes());
    }
}
