package es.caib.invai.back.ejb.catalog.validationStatus;

import es.caib.invai.back.interna.catalog.validationStatus.DTO.ValidationStatusOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.validationStatus.ValidationStatusRepository;
import es.caib.invai.back.service.mapper.catalog.validationStatus.ValidationStatusMapper;
import es.caib.invai.back.service.model.catalog.validationStatus.ValidationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ValidationStatusServiceFacadeBean}, verifying that the read-only lookup
 * dictionary delegates correctly to the mocked {@link ValidationStatusRepository} and
 * {@link ValidationStatusMapper} collaborators.
 */
@ExtendWith(MockitoExtension.class)
class ValidationStatusServiceFacadeBeanTest {

    @Mock
    private ValidationStatusMapper validationStatusMapper;

    @Mock
    private ValidationStatusRepository validationStatusRepository;

    @InjectMocks
    private ValidationStatusServiceFacadeBean validationStatusServiceFacadeBean;

    @Test
    void getAll_delegatesToRepositoryAndMapsList() {
        ValidationStatus status = new ValidationStatus();
        status.setId(1L);
        status.setCode("VALIDATED");
        status.setName("Validat");
        status.setNameEs("Validado");

        List<ValidationStatus> models = List.of(status);
        List<ValidationStatusOutputDTO> mapped = List.of(new ValidationStatusOutputDTO());

        when(validationStatusRepository.findAll()).thenReturn(models);
        when(validationStatusMapper.toResponseList(models)).thenReturn(mapped);

        List<ValidationStatusOutputDTO> result = validationStatusServiceFacadeBean.getAll();

        assertEquals(mapped, result);
    }
}
