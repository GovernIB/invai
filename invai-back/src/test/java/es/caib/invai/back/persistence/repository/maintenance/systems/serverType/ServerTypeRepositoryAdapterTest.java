package es.caib.invai.back.persistence.repository.maintenance.systems.serverType;

import es.caib.invai.back.persistence.model.maintenance.systems.serverType.LkupServerTypeEntity;
import es.caib.invai.back.service.mapper.maintenance.systems.serverType.ServerTypeMapper;
import es.caib.invai.back.service.model.maintenance.systems.serverType.ServerType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ServerTypeRepositoryAdapter}, a simple read-only lookup adapter with no
 * Criteria/Specification support and no create/update/delete/audit behavior.
 */
@ExtendWith(MockitoExtension.class)
class ServerTypeRepositoryAdapterTest {

    @Mock
    private ServerTypeJPARepository serverTypeJPARepository;

    @Mock
    private ServerTypeMapper serverTypeMapper;

    private ServerTypeRepositoryAdapter adapter;

    private ServerTypeRepositoryAdapter buildAdapter() {
        ServerTypeRepositoryAdapter a = new ServerTypeRepositoryAdapter();
        ReflectionTestUtils.setField(a, "serverTypeJPARepository", serverTypeJPARepository);
        ReflectionTestUtils.setField(a, "serverTypeMapper", serverTypeMapper);
        return a;
    }

    @Test
    void findAll_delegatesToJPARepositorySortedByNameAndMapsList() {
        adapter = buildAdapter();
        LkupServerTypeEntity entity = new LkupServerTypeEntity();
        List<LkupServerTypeEntity> entities = List.of(entity);
        List<ServerType> models = List.of(new ServerType());
        when(serverTypeJPARepository.findAll(Sort.by("name"))).thenReturn(entities);
        when(serverTypeMapper.toModelList(entities)).thenReturn(models);

        List<ServerType> result = adapter.findAll();

        assertSame(models, result);
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(serverTypeJPARepository).findAll(sortCaptor.capture());
        assertNotNull(sortCaptor.getValue().getOrderFor("name"));
        assertEquals(Sort.Direction.ASC, Objects.requireNonNull(sortCaptor.getValue().getOrderFor("name")).getDirection());
    }

    @Test
    void findAll_emptyRepository_returnsMappedEmptyList() {
        adapter = buildAdapter();
        when(serverTypeJPARepository.findAll(Sort.by("name"))).thenReturn(List.of());
        when(serverTypeMapper.toModelList(List.of())).thenReturn(List.of());

        List<ServerType> result = adapter.findAll();

        assertEquals(0, result.size());
    }
}
