package es.caib.invai.back.ejb.catalog.mimeType;

import es.caib.invai.back.interna.catalog.mimeType.DTO.MimeTypeOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.mimeType.MimeTypeRepository;
import es.caib.invai.back.service.mapper.catalog.mimeType.MimeTypeMapper;
import es.caib.invai.back.service.model.catalog.mimeType.MimeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MimeTypeServiceFacadeBean}, verifying that the read-only lookup
 * delegates to the mocked repository and mapper.
 */
@ExtendWith(MockitoExtension.class)
class MimeTypeServiceFacadeBeanTest {

    @Mock
    private MimeTypeMapper mimeTypeMapper;

    @Mock
    private MimeTypeRepository mimeTypeRepository;

    @InjectMocks
    private MimeTypeServiceFacadeBean facade;

    @Test
    void getAll_delegatesToRepositoryAndMapsList() {
        List<MimeType> models = List.of(new MimeType());
        List<MimeTypeOutputDTO> mapped = List.of(new MimeTypeOutputDTO());
        when(mimeTypeRepository.findAll()).thenReturn(models);
        when(mimeTypeMapper.toResponseList(models)).thenReturn(mapped);

        assertSame(mapped, facade.getAll());
    }
}
