package es.caib.invai.back.interna.maintenance.security.securityRole;

import es.caib.invai.back.interna.maintenance.security.securityRole.DTO.SecurityRoleOutputDTO;
import es.caib.invai.back.service.facade.maintenance.security.securityRole.SecurityRoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SecurityRoleController}, verifying that its only endpoint (the Soffid
 * role search proxy) delegates to {@link SecurityRoleService} and returns HTTP 200.
 */
@ExtendWith(MockitoExtension.class)
class SecurityRoleControllerTest {

    @Mock
    private SecurityRoleService securityRoleService;

    private SecurityRoleController securityRoleController;

    @BeforeEach
    void setUp() {
        securityRoleController = new SecurityRoleController();
        ReflectionTestUtils.setField(securityRoleController, "securityRoleService", securityRoleService);
    }

    @Test
    void searchSoffid_returnsOkWithServiceResult() {
        Pageable pageable = Pageable.unpaged();
        Page<SecurityRoleOutputDTO> results = new PageImpl<>(List.of(new SecurityRoleOutputDTO()));
        when(securityRoleService.searchSoffid("OWS", pageable)).thenReturn(results);

        ResponseEntity<Page<SecurityRoleOutputDTO>> response = securityRoleController.searchSoffid("OWS", pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(results, response.getBody());
    }

    @Test
    void searchSoffid_nullName_stillDelegatesAsGetAll() {
        Pageable pageable = Pageable.unpaged();
        Page<SecurityRoleOutputDTO> results = new PageImpl<>(List.of());
        when(securityRoleService.searchSoffid(null, pageable)).thenReturn(results);

        ResponseEntity<Page<SecurityRoleOutputDTO>> response = securityRoleController.searchSoffid(null, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(results, response.getBody());
    }
}
