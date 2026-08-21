package es.caib.invai.back.ejb.catalog.modality;

import es.caib.invai.back.interna.catalog.modality.DTO.ModalityOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.modality.ModalityRepository;
import es.caib.invai.back.service.mapper.catalog.modality.ModalityMapper;
import es.caib.invai.back.service.model.catalog.modality.Modality;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ModalityServiceFacadeBean}, verifying that the read-only lookup
 * dictionary delegates correctly to the mocked {@link ModalityRepository} and
 * {@link ModalityMapper} collaborators.
 */
@ExtendWith(MockitoExtension.class)
class ModalityServiceFacadeBeanTest {

    @Mock
    private ModalityMapper modalityMapper;

    @Mock
    private ModalityRepository modalityRepository;

    @InjectMocks
    private ModalityServiceFacadeBean modalityServiceFacadeBean;

    @Test
    void getAll_delegatesToRepositoryAndMapsList() {
        Modality modality = new Modality();
        modality.setId(1L);
        modality.setName("Desenvolupament intern");
        modality.setNameEs("Desarrollo interno");

        List<Modality> models = List.of(modality);
        List<ModalityOutputDTO> mapped = List.of(new ModalityOutputDTO());

        when(modalityRepository.findAll()).thenReturn(models);
        when(modalityMapper.toResponseList(models)).thenReturn(mapped);

        List<ModalityOutputDTO> result = modalityServiceFacadeBean.getAll();

        assertEquals(mapped, result);
    }
}
