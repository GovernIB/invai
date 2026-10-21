package es.caib.invai.back.ejb.catalog.documentStatus;

import es.caib.invai.back.interna.catalog.documentStatus.DTO.DocumentStatusOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.documentStatus.DocumentStatusRepository;
import es.caib.invai.back.service.mapper.catalog.documentStatus.DocumentStatusMapper;
import es.caib.invai.back.service.model.catalog.documentStatus.DocumentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DocumentStatusServiceFacadeBean}, verifying that the read-only lookup
 * delegates to the mocked repository and mapper.
 */
@ExtendWith(MockitoExtension.class)
class DocumentStatusServiceFacadeBeanTest {

    @Mock
    private DocumentStatusMapper documentStatusMapper;

    @Mock
    private DocumentStatusRepository documentStatusRepository;

    @InjectMocks
    private DocumentStatusServiceFacadeBean facade;

    @Test
    void getAll_delegatesToRepositoryAndMapsList() {
        List<DocumentStatus> models = List.of(new DocumentStatus());
        List<DocumentStatusOutputDTO> mapped = List.of(new DocumentStatusOutputDTO());
        when(documentStatusRepository.findAll()).thenReturn(models);
        when(documentStatusMapper.toResponseList(models)).thenReturn(mapped);

        assertSame(mapped, facade.getAll());
    }
}
