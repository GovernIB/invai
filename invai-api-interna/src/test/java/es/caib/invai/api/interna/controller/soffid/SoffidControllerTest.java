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
}
