package es.caib.invai.back.interna.application.dir3;

import es.caib.invai.back.interna.application.dir3.DTO.ApplicationDir3MismatchOutputDTO;
import es.caib.invai.back.service.model.application.dir3.Dir3MismatchAuditState;
import es.caib.invai.back.interna.application.dir3.DTO.Dir3MismatchAuditStatusOutputDTO;
import es.caib.invai.back.service.facade.application.dir3.Dir3CheckService;
import es.caib.invai.back.service.facade.application.dir3.Dir3BackgroundAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link Dir3AuditController}, verifying that every endpoint delegates to its
 * facade and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class Dir3AuditControllerTest {

    @Mock
    private Dir3CheckService dir3CheckService;

    @Mock
    private Dir3BackgroundAuditService dir3BackgroundAuditService;

    private Dir3AuditController dir3AuditController;

    @BeforeEach
    void setUp() {
        dir3AuditController = new Dir3AuditController(dir3CheckService, dir3BackgroundAuditService);
    }

    @Test
    void checkDir3Mismatch_returnsOkWithServiceResult() {
        ApplicationDir3MismatchOutputDTO result =
                ApplicationDir3MismatchOutputDTO.builder().id(1L).name("App One").dir3Mismatch(false).build();
        when(dir3CheckService.checkDir3Mismatch(1L)).thenReturn(result);

        ResponseEntity<ApplicationDir3MismatchOutputDTO> response = dir3AuditController.checkDir3Mismatch(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(result, response.getBody());
    }

    @Test
    void startDir3MismatchAudit_returnsAcceptedWithTheAuditStatus() {
        Dir3MismatchAuditStatusOutputDTO status = Dir3MismatchAuditStatusOutputDTO.builder()
                .state(Dir3MismatchAuditState.RUNNING).build();
        when(dir3BackgroundAuditService.start(anyString())).thenReturn(status);

        ResponseEntity<Dir3MismatchAuditStatusOutputDTO> response = dir3AuditController.startDir3MismatchAudit();

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertSame(status, response.getBody());
    }

    @Test
    void getDir3MismatchAuditStatus_returnsOkWithTheAuditStatus() {
        Dir3MismatchAuditStatusOutputDTO status = Dir3MismatchAuditStatusOutputDTO.builder()
                .state(Dir3MismatchAuditState.COMPLETED).build();
        when(dir3BackgroundAuditService.getStatus()).thenReturn(status);

        ResponseEntity<Dir3MismatchAuditStatusOutputDTO> response = dir3AuditController.getDir3MismatchAuditStatus();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(status, response.getBody());
    }
}
