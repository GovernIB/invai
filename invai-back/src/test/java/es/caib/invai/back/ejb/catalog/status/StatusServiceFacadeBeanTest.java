package es.caib.invai.back.ejb.catalog.status;

import es.caib.invai.back.interna.catalog.status.DTO.StatusOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.status.StatusRepository;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapper;
import es.caib.invai.back.service.model.catalog.status.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StatusServiceFacadeBean}, verifying that the read-only lookup
 * dictionary delegates correctly to the mocked {@link StatusRepository} and
 * {@link StatusMapper} collaborators.
 */
@ExtendWith(MockitoExtension.class)
class StatusServiceFacadeBeanTest {

    @Mock
    private StatusMapper statusMapper;

    @Mock
    private StatusRepository statusRepository;

    @InjectMocks
    private StatusServiceFacadeBean statusServiceFacadeBean;

    @Test
    void getAll_delegatesToRepositoryAndMapsList() {
        Status status = new Status();
        status.setId(1L);
        status.setName("Actiu");
        status.setNameEs("Activo");

        List<Status> models = List.of(status);
        List<StatusOutputDTO> mapped = List.of(new StatusOutputDTO());

        when(statusRepository.findAll()).thenReturn(models);
        when(statusMapper.toResponseList(models)).thenReturn(mapped);

        List<StatusOutputDTO> result = statusServiceFacadeBean.getAll();

        assertEquals(mapped, result);
    }
}
