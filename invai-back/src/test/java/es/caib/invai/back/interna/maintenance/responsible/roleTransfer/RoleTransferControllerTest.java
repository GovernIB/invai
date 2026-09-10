package es.caib.invai.back.interna.maintenance.responsible.roleTransfer;

import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleAssignmentOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleTransferInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleTransferItemDTO;
import es.caib.invai.back.service.facade.maintenance.responsible.roleTransfer.RoleTransferService;
import es.caib.invai.back.service.model.maintenance.responsible.roleTransfer.RoleAssignmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RoleTransferController}, verifying that every endpoint delegates to
 * {@link RoleTransferService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class RoleTransferControllerTest {

    @Mock
    private RoleTransferService roleTransferService;

    private RoleTransferController roleTransferController;

    @BeforeEach
    void setUp() {
        roleTransferController = new RoleTransferController(roleTransferService);
    }

    @Test
    void getAssignmentsByPerson_returnsOkWithServiceResult() {
        List<RoleAssignmentOutputDTO> assignments = List.of(RoleAssignmentOutputDTO.builder().id(1L).build());
        when(roleTransferService.getAssignmentsByPerson(10L)).thenReturn(assignments);

        ResponseEntity<List<RoleAssignmentOutputDTO>> response = roleTransferController.getAssignmentsByPerson(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(assignments, response.getBody());
    }

    @Test
    void transfer_returnsNoContentAndDelegatesToService() {
        RoleTransferInputDTO inputDTO = new RoleTransferInputDTO(
                List.of(new RoleTransferItemDTO(1L, RoleAssignmentType.RESPONSIBLE)), "target@example.com", false);

        ResponseEntity<Void> response = roleTransferController.transfer(inputDTO);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(roleTransferService).transfer(inputDTO);
    }
}
