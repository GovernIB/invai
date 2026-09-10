package es.caib.invai.back.interna.integrations.dir3.admUnit;

import es.caib.invai.back.interna.integrations.dir3.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.back.service.facade.integrations.dir3.admUnit.AdmUnitService;
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
 * Unit tests for {@link AdmUnitController}, verifying that every endpoint delegates to
 * {@link AdmUnitService} and returns the expected HTTP status code. Administrative units are pure
 * external reference data mirrored live from DIR3CAIB — this controller is read-only.
 * <p>
 * {@link AdmUnitController} wires its collaborator via field injection (no constructor is
 * declared), so the mocked service is bound in via {@link ReflectionTestUtils}.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class AdmUnitControllerTest {

    @Mock
    private AdmUnitService admUnitService;

    private AdmUnitController admUnitController;

    @BeforeEach
    void setUp() {
        admUnitController = new AdmUnitController();
        ReflectionTestUtils.setField(admUnitController, "admUnitService", admUnitService);
    }

    @Test
    void getDepartments_returnsOkWithPagedServiceResult() {
        Pageable pageable = Pageable.unpaged();
        Page<AdmUnitOutputDTO> page = new PageImpl<>(List.of(new AdmUnitOutputDTO()));
        when(admUnitService.getDepartments(pageable)).thenReturn(page);

        ResponseEntity<Page<AdmUnitOutputDTO>> response = admUnitController.getDepartments(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void getAdmUnitsByDepartment_returnsOkWithPagedServiceResult() {
        Pageable pageable = Pageable.unpaged();
        Page<AdmUnitOutputDTO> page = new PageImpl<>(List.of(new AdmUnitOutputDTO()));
        when(admUnitService.getAdmUnitsByDepartment("A04026919", pageable)).thenReturn(page);

        ResponseEntity<Page<AdmUnitOutputDTO>> response = admUnitController.getAdmUnitsByDepartment("A04026919", pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }
}
