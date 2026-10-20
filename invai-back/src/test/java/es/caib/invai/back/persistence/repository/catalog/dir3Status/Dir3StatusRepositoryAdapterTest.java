package es.caib.invai.back.persistence.repository.catalog.dir3Status;

import es.caib.invai.back.persistence.model.catalog.dir3Status.LkupDir3StatusEntity;
import es.caib.invai.back.service.mapper.catalog.dir3Status.Dir3StatusMapper;
import es.caib.invai.back.service.model.catalog.dir3Status.Dir3Status;
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
 * Unit tests for {@link Dir3StatusRepositoryAdapter}, a simple read-only lookup adapter with no
 * Criteria/Specification support and no create/update/delete/audit behavior.
 */
@ExtendWith(MockitoExtension.class)
class Dir3StatusRepositoryAdapterTest {

    @Mock
    private Dir3StatusJPARepository dir3StatusJPARepository;

    @Mock
    private Dir3StatusMapper dir3StatusMapper;

    private Dir3StatusRepositoryAdapter adapter;

    private Dir3StatusRepositoryAdapter buildAdapter() {
        Dir3StatusRepositoryAdapter a = new Dir3StatusRepositoryAdapter();
        ReflectionTestUtils.setField(a, "dir3StatusJPARepository", dir3StatusJPARepository);
        ReflectionTestUtils.setField(a, "dir3StatusMapper", dir3StatusMapper);
        return a;
    }

    @Test
    void findAll_delegatesToJPARepositorySortedByNameAndMapsList() {
        adapter = buildAdapter();
        LkupDir3StatusEntity entity = new LkupDir3StatusEntity();
        List<LkupDir3StatusEntity> entities = List.of(entity);
        List<Dir3Status> models = List.of(new Dir3Status());
        when(dir3StatusJPARepository.findAll(Sort.by("name"))).thenReturn(entities);
        when(dir3StatusMapper.toModelList(entities)).thenReturn(models);

        List<Dir3Status> result = adapter.findAll();

        assertSame(models, result);
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(dir3StatusJPARepository).findAll(sortCaptor.capture());
        assertNotNull(sortCaptor.getValue().getOrderFor("name"));
        assertEquals(Sort.Direction.ASC, Objects.requireNonNull(sortCaptor.getValue().getOrderFor("name")).getDirection());
    }

    @Test
    void findAll_emptyRepository_returnsMappedEmptyList() {
        adapter = buildAdapter();
        when(dir3StatusJPARepository.findAll(Sort.by("name"))).thenReturn(List.of());
        when(dir3StatusMapper.toModelList(List.of())).thenReturn(List.of());

        List<Dir3Status> result = adapter.findAll();

        assertEquals(0, result.size());
    }
}
