package es.caib.invai.api.interna.controller.soffid;

import es.caib.invai.api.interna.rest.soffid.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SoffidController}.
 */
@ExtendWith(MockitoExtension.class)
class SoffidControllerTest {

    @Mock
    private SoffidClient soffidClient;

    @Test
    void searchUsers_flattensPageIntoContentAndTotalElements() {
        SoffidUser user = new SoffidUser();
        user.setUserName("u00004");
        Page<SoffidUser> page = new PageImpl<>(List.of(user), PageRequest.of(0, 20), 1);
        when(soffidClient.searchUsers(eq("Joan"), any())).thenReturn(page);

        SoffidController controller = new SoffidController(soffidClient);
        SoffidPageResponse<SoffidUser> result = controller.searchUsers("Joan", 0, 20);

        assertEquals(1, result.getTotalElements());
        assertEquals("u00004", result.getContent().get(0).getUserName());
    }

    @Test
    void searchByEmail_matchFound_returnsOkWithUser() {
        SoffidUser user = new SoffidUser();
        user.setEmailAddress("joan.puig@caib.es");
        when(soffidClient.searchByEmail("joan.puig@caib.es")).thenReturn(user);

        SoffidController controller = new SoffidController(soffidClient);
        ResponseEntity<SoffidUser> result = controller.searchByEmail("joan.puig@caib.es");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        assertEquals("joan.puig@caib.es", result.getBody().getEmailAddress());
    }

    @Test
    void searchByEmail_noMatch_returnsNoContent() {
        when(soffidClient.searchByEmail("nobody@caib.es")).thenReturn(null);

        SoffidController controller = new SoffidController(soffidClient);
        ResponseEntity<SoffidUser> result = controller.searchByEmail("nobody@caib.es");

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    }

    @Test
    void searchRoles_flattensPageIntoContentAndTotalElements() {
        SoffidRole role = new SoffidRole();
        role.setName("AD role");
        Page<SoffidRole> page = new PageImpl<>(List.of(role), PageRequest.of(0, 20), 1);
        when(soffidClient.searchRoles(eq("AD"), any())).thenReturn(page);

        SoffidController controller = new SoffidController(soffidClient);
        SoffidPageResponse<SoffidRole> result = controller.searchRoles("AD", 0, 20);

        assertEquals(1, result.getTotalElements());
        assertEquals("AD role", result.getContent().get(0).getName());
    }

    @Test
    void resolveGroupDir3_found_returnsOkWithDir3Code() {
        when(soffidClient.resolveGroupDir3("sgaip")).thenReturn("A04027054");

        SoffidController controller = new SoffidController(soffidClient);
        ResponseEntity<Map<String, String>> result = controller.resolveGroupDir3("sgaip");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        assertEquals("A04027054", result.getBody().get("dir3Code"));
    }

    @Test
    void resolveGroupDir3_notFound_returnsNoContent() {
        when(soffidClient.resolveGroupDir3("nonexistent")).thenReturn(null);

        SoffidController controller = new SoffidController(soffidClient);
        ResponseEntity<Map<String, String>> result = controller.resolveGroupDir3("nonexistent");

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    }

    @Test
    void getUserRoles_returnsRolesFromClient() {
        SoffidRole role = new SoffidRole();
        role.setId(100L);
        role.setName("AD role");
        when(soffidClient.getUserRoles("u00629")).thenReturn(List.of(role));

        SoffidController controller = new SoffidController(soffidClient);
        List<SoffidRole> result = controller.getUserRoles("u00629");

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
    }

    @Test
    void getRolesByIds_returnsRolesFromClient() {
        SoffidRole role = new SoffidRole();
        role.setId(26L);
        role.setName("SNMPAGENT");
        when(soffidClient.getRolesByIds(List.of(26L, 33L))).thenReturn(List.of(role));

        SoffidController controller = new SoffidController(soffidClient);
        List<SoffidRole> result = controller.getRolesByIds(List.of(26L, 33L));

        assertEquals(1, result.size());
        assertEquals(26L, result.get(0).getId());
    }
}
