package es.caib.invai.back.ejb.catalog.standardAdaption;

import es.caib.invai.back.interna.catalog.standardAdaption.DTO.StandardAdaptionOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.standardAdaption.StandardAdaptionRepository;
import es.caib.invai.back.service.mapper.catalog.standardAdaption.StandardAdaptionMapper;
import es.caib.invai.back.service.model.catalog.standardAdaption.StandardAdaption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StandardAdaptionServiceFacadeBean}, verifying that the read-only lookup
 * dictionary delegates correctly to the mocked {@link StandardAdaptionRepository} and
 * {@link StandardAdaptionMapper} collaborators.
 */
@ExtendWith(MockitoExtension.class)
class StandardAdaptionServiceFacadeBeanTest {

    @Mock
    private StandardAdaptionMapper standardAdaptionMapper;

    @Mock
    private StandardAdaptionRepository standardAdaptionRepository;

    @InjectMocks
    private StandardAdaptionServiceFacadeBean standardAdaptionServiceFacadeBean;

    @Test
    void getAll_delegatesToRepositoryAndMapsList() {
        StandardAdaption standardAdaption = new StandardAdaption();
        standardAdaption.setId(1L);
        standardAdaption.setName("Total");
        standardAdaption.setNameEs("Total");

        List<StandardAdaption> models = List.of(standardAdaption);
        List<StandardAdaptionOutputDTO> mapped = List.of(new StandardAdaptionOutputDTO());

        when(standardAdaptionRepository.findAll()).thenReturn(models);
        when(standardAdaptionMapper.toResponseList(models)).thenReturn(mapped);

        List<StandardAdaptionOutputDTO> result = standardAdaptionServiceFacadeBean.getAll();

        assertEquals(mapped, result);
    }
}
