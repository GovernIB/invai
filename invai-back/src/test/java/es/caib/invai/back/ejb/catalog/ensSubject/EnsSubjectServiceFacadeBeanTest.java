package es.caib.invai.back.ejb.catalog.ensSubject;

import es.caib.invai.back.interna.catalog.ensSubject.DTO.EnsSubjectOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.ensSubject.EnsSubjectRepository;
import es.caib.invai.back.service.mapper.catalog.ensSubject.EnsSubjectMapper;
import es.caib.invai.back.service.model.catalog.ensSubject.EnsSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EnsSubjectServiceFacadeBean}, verifying that the read-only lookup
 * dictionary delegates correctly to the mocked {@link EnsSubjectRepository} and
 * {@link EnsSubjectMapper} collaborators.
 */
@ExtendWith(MockitoExtension.class)
class EnsSubjectServiceFacadeBeanTest {

    @Mock
    private EnsSubjectMapper ensSubjectMapper;

    @Mock
    private EnsSubjectRepository ensSubjectRepository;

    @InjectMocks
    private EnsSubjectServiceFacadeBean ensSubjectServiceFacadeBean;

    @Test
    void getAll_delegatesToRepositoryAndMapsList() {
        EnsSubject ensSubject = new EnsSubject();
        ensSubject.setId(1L);
        ensSubject.setName("Si");
        ensSubject.setNameEs("Si");

        List<EnsSubject> models = List.of(ensSubject);
        List<EnsSubjectOutputDTO> mapped = List.of(new EnsSubjectOutputDTO());

        when(ensSubjectRepository.findAll()).thenReturn(models);
        when(ensSubjectMapper.toResponseList(models)).thenReturn(mapped);

        List<EnsSubjectOutputDTO> result = ensSubjectServiceFacadeBean.getAll();

        assertEquals(mapped, result);
    }
}
