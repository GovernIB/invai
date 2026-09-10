package es.caib.invai.back.ejb.maintenance.systems.serverType;

import es.caib.invai.back.interna.maintenance.systems.serverType.DTO.ServerTypeOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.serverType.ServerTypeRepository;
import es.caib.invai.back.service.mapper.maintenance.systems.serverType.ServerTypeMapper;
import es.caib.invai.back.service.model.maintenance.systems.serverType.ServerType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ServerTypeServiceFacadeBean}, verifying that the read-only lookup
 * dictionary delegates correctly to the mocked {@link ServerTypeRepository} and
 * {@link ServerTypeMapper} collaborators.
 */
@ExtendWith(MockitoExtension.class)
class ServerTypeServiceFacadeBeanTest {

    @Mock
    private ServerTypeMapper serverTypeMapper;

    @Mock
    private ServerTypeRepository serverTypeRepository;

    @InjectMocks
    private ServerTypeServiceFacadeBean serverTypeServiceFacadeBean;

    @Test
    void getAll_delegatesToRepositoryAndMapsList() {
        ServerType serverType = new ServerType();
        serverType.setId(1L);
        serverType.setCode("DATABASE");
        serverType.setName("Base de dades");
        serverType.setNameEs("Base de datos");

        List<ServerType> models = List.of(serverType);
        List<ServerTypeOutputDTO> mapped = List.of(new ServerTypeOutputDTO());

        when(serverTypeRepository.findAll()).thenReturn(models);
        when(serverTypeMapper.toResponseList(models)).thenReturn(mapped);

        List<ServerTypeOutputDTO> result = serverTypeServiceFacadeBean.getAll();

        assertEquals(mapped, result);
    }
}
