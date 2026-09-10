package es.caib.invai.back.ejb.maintenance.security.securityRole;

import es.caib.invai.back.interna.maintenance.security.securityRole.DTO.SecurityRoleOutputDTO;
import es.caib.invai.back.rest.soffid.SoffidClient;
import es.caib.invai.back.rest.soffid.SoffidRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SecurityRoleServiceFacadeBean}, currently limited to the Soffid role
 * search proxy (no local CRUD is exposed yet).
 */
@ExtendWith(MockitoExtension.class)
class SecurityRoleServiceFacadeBeanTest {

    @Mock
    private SoffidClient soffidClient;

    @InjectMocks
    private SecurityRoleServiceFacadeBean securityRoleServiceFacadeBean;

    @Test
    void searchSoffid_delegatesToSoffidClientAndMapsResults() {
        SoffidRole soffidRole = new SoffidRole();
        soffidRole.setId(52L);
        soffidRole.setName("OWS_ADMIN_ROLE");
        soffidRole.setDescription("Administrator OWS role");
        soffidRole.setSystem("OWS");
        Pageable pageable = PageRequest.of(0, 20);
        when(soffidClient.searchRoles("OWS", pageable)).thenReturn(new PageImpl<>(List.of(soffidRole), pageable, 1));

        Page<SecurityRoleOutputDTO> result = securityRoleServiceFacadeBean.searchSoffid("OWS", pageable);

        assertEquals(1, result.getTotalElements());
        SecurityRoleOutputDTO first = result.getContent().get(0);
        assertNull(first.getId());
        assertEquals(52L, first.getRoleId());
        assertEquals("OWS_ADMIN_ROLE", first.getName());
        assertEquals("OWS", first.getSystem());
        assertEquals("Administrator OWS role", first.getDescription());
    }

    @Test
    void searchSoffid_blankName_stillDelegatesAsUnfilteredListing() {
        Pageable pageable = PageRequest.of(0, 20);
        when(soffidClient.searchRoles(null, pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<SecurityRoleOutputDTO> result = securityRoleServiceFacadeBean.searchSoffid(null, pageable);

        assertTrue(result.getContent().isEmpty());
        verify(soffidClient).searchRoles(null, pageable);
    }
}
