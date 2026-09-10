package es.caib.invai.back.persistence.repository.catalog.status;

import es.caib.invai.back.persistence.model.catalog.status.LkupStatusEntity;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapper;
import es.caib.invai.back.service.model.catalog.status.Status;
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
 * Unit tests for {@link StatusRepositoryAdapter}, a simple read-only lookup adapter with no
 * Criteria/Specification support and no create/update/delete/audit behavior.
 */
@ExtendWith(MockitoExtension.class)
class StatusRepositoryAdapterTest {

    @Mock
    private StatusJPARepository statusJPARepository;

    @Mock
    private StatusMapper statusMapper;

    private StatusRepositoryAdapter adapter;

    private StatusRepositoryAdapter buildAdapter() {
        StatusRepositoryAdapter a = new StatusRepositoryAdapter();
        ReflectionTestUtils.setField(a, "statusJPARepository", statusJPARepository);
        ReflectionTestUtils.setField(a, "statusMapper", statusMapper);
        return a;
    }

    @Test
    void findAll_delegatesToJPARepositorySortedByNameAndMapsList() {
        adapter = buildAdapter();
        LkupStatusEntity entity = new LkupStatusEntity();
        List<LkupStatusEntity> entities = List.of(entity);
        List<Status> models = List.of(new Status());
        when(statusJPARepository.findAll(Sort.by("name"))).thenReturn(entities);
        when(statusMapper.toModelList(entities)).thenReturn(models);

        List<Status> result = adapter.findAll();

        assertSame(models, result);
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(statusJPARepository).findAll(sortCaptor.capture());
        assertNotNull(sortCaptor.getValue().getOrderFor("name"));
        assertEquals(Sort.Direction.ASC, Objects.requireNonNull(sortCaptor.getValue().getOrderFor("name")).getDirection());
    }

    @Test
    void findAll_emptyRepository_returnsMappedEmptyList() {
        adapter = buildAdapter();
        when(statusJPARepository.findAll(Sort.by("name"))).thenReturn(List.of());
        when(statusMapper.toModelList(List.of())).thenReturn(List.of());

        List<Status> result = adapter.findAll();

        assertEquals(0, result.size());
    }
}
