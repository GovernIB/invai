package es.caib.invai.back.ejb.catalog.appSection;

import es.caib.invai.back.interna.catalog.appSection.DTO.AppSectionOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.appSection.AppSectionRepository;
import es.caib.invai.back.service.mapper.catalog.appSection.AppSectionMapper;
import es.caib.invai.back.service.model.catalog.appSection.AppSection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppSectionServiceFacadeBean}, verifying that the read-only lookup
 * delegates to the mocked repository and mapper.
 */
@ExtendWith(MockitoExtension.class)
class AppSectionServiceFacadeBeanTest {

    @Mock
    private AppSectionMapper appSectionMapper;

    @Mock
    private AppSectionRepository appSectionRepository;

    @InjectMocks
    private AppSectionServiceFacadeBean facade;

    @Test
    void getAll_delegatesToRepositoryAndMapsList() {
        List<AppSection> models = List.of(new AppSection());
        List<AppSectionOutputDTO> mapped = List.of(new AppSectionOutputDTO());
        when(appSectionRepository.findAll()).thenReturn(models);
        when(appSectionMapper.toResponseList(models)).thenReturn(mapped);

        assertSame(mapped, facade.getAll());
    }
}
