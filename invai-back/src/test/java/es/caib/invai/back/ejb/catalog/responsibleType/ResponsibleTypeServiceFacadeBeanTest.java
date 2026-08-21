package es.caib.invai.back.ejb.catalog.responsibleType;

import es.caib.invai.back.interna.catalog.responsibleType.DTO.ResponsibleTypeOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.responsibleType.ResponsibleTypeRepository;
import es.caib.invai.back.service.mapper.catalog.responsibleType.ResponsibleTypeMapper;
import es.caib.invai.back.service.model.catalog.responsibleType.ResponsibleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ResponsibleTypeServiceFacadeBean}, verifying that the read-only lookup
 * dictionary delegates correctly to the mocked {@link ResponsibleTypeRepository} and
 * {@link ResponsibleTypeMapper} collaborators.
 */
@ExtendWith(MockitoExtension.class)
class ResponsibleTypeServiceFacadeBeanTest {

    @Mock
    private ResponsibleTypeMapper responsibleTypeMapper;

    @Mock
    private ResponsibleTypeRepository responsibleTypeRepository;

    @InjectMocks
    private ResponsibleTypeServiceFacadeBean responsibleTypeServiceFacadeBean;

    @Test
    void getAll_delegatesToRepositoryAndMapsList() {
        ResponsibleType responsibleType = new ResponsibleType();
        responsibleType.setId(1L);
        responsibleType.setName("Responsable funcional");
        responsibleType.setNameEs("Responsable funcional");

        List<ResponsibleType> models = List.of(responsibleType);
        List<ResponsibleTypeOutputDTO> mapped = List.of(new ResponsibleTypeOutputDTO());

        when(responsibleTypeRepository.findAll()).thenReturn(models);
        when(responsibleTypeMapper.toResponseList(models)).thenReturn(mapped);

        List<ResponsibleTypeOutputDTO> result = responsibleTypeServiceFacadeBean.getAll();

        assertEquals(mapped, result);
    }
}
