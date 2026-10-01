package es.caib.invai.back.ejb.catalog.dir3Status;

import es.caib.invai.back.interna.catalog.dir3Status.DTO.Dir3StatusOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.dir3Status.Dir3StatusRepository;
import es.caib.invai.back.service.mapper.catalog.dir3Status.Dir3StatusMapper;
import es.caib.invai.back.service.model.catalog.dir3Status.Dir3Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link Dir3StatusServiceFacadeBean}, verifying that the read-only lookup
 * dictionary delegates correctly to the mocked {@link Dir3StatusRepository} and
 * {@link Dir3StatusMapper} collaborators.
 */
@ExtendWith(MockitoExtension.class)
class Dir3StatusServiceFacadeBeanTest {

    @Mock
    private Dir3StatusMapper dir3StatusMapper;

    @Mock
    private Dir3StatusRepository dir3StatusRepository;

    @InjectMocks
    private Dir3StatusServiceFacadeBean dir3StatusServiceFacadeBean;

    @Test
    void getAll_delegatesToRepositoryAndMapsList() {
        Dir3Status status = new Dir3Status();
        status.setId(1L);
        status.setCode("VALIDATED");
        status.setName("Validat");
        status.setNameEs("Validado");

        List<Dir3Status> models = List.of(status);
        List<Dir3StatusOutputDTO> mapped = List.of(new Dir3StatusOutputDTO());

        when(dir3StatusRepository.findAll()).thenReturn(models);
        when(dir3StatusMapper.toResponseList(models)).thenReturn(mapped);

        List<Dir3StatusOutputDTO> result = dir3StatusServiceFacadeBean.getAll();

        assertEquals(mapped, result);
    }
}
