package es.caib.invai.back.persistence.repository.catalog.ensSubject;

import es.caib.invai.back.persistence.model.catalog.ensSubject.LkupEnsSubjectEntity;
import es.caib.invai.back.service.mapper.catalog.ensSubject.EnsSubjectMapper;
import es.caib.invai.back.service.model.catalog.ensSubject.EnsSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EnsSubjectRepositoryAdapter}, a simple read-only lookup adapter
 * with no Criteria/Specification support and no create/update/delete/audit behavior.
 */
@ExtendWith(MockitoExtension.class)
class EnsSubjectRepositoryAdapterTest {

    @Mock
    private EnsSubjectJPARepository ensSubjectJPARepository;

    @Mock
    private EnsSubjectMapper ensSubjectMapper;

    private EnsSubjectRepositoryAdapter adapter;

    private EnsSubjectRepositoryAdapter buildAdapter() {
        EnsSubjectRepositoryAdapter a = new EnsSubjectRepositoryAdapter();
        ReflectionTestUtils.setField(a, "ensSubjectJPARepository", ensSubjectJPARepository);
        ReflectionTestUtils.setField(a, "ensSubjectMapper", ensSubjectMapper);
        return a;
    }

    @Test
    void findAll_delegatesToJPARepositorySortedByNameAndMapsList() {
        adapter = buildAdapter();
        LkupEnsSubjectEntity entity = new LkupEnsSubjectEntity();
        List<LkupEnsSubjectEntity> entities = List.of(entity);
        List<EnsSubject> models = List.of(new EnsSubject());
        when(ensSubjectJPARepository.findAll(Sort.by("name"))).thenReturn(entities);
        when(ensSubjectMapper.toModelList(entities)).thenReturn(models);

        List<EnsSubject> result = adapter.findAll();

        assertSame(models, result);
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(ensSubjectJPARepository).findAll(sortCaptor.capture());
        assertNotNull(sortCaptor.getValue().getOrderFor("name"));
        assertEquals(Sort.Direction.ASC, Objects.requireNonNull(sortCaptor.getValue().getOrderFor("name")).getDirection());
    }

    @Test
    void findAll_emptyRepository_returnsMappedEmptyList() {
        adapter = buildAdapter();
        when(ensSubjectJPARepository.findAll(Sort.by("name"))).thenReturn(List.of());
        when(ensSubjectMapper.toModelList(List.of())).thenReturn(List.of());

        List<EnsSubject> result = adapter.findAll();

        assertEquals(0, result.size());
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        LkupEnsSubjectEntity entity = new LkupEnsSubjectEntity();
        EnsSubject model = new EnsSubject();
        when(ensSubjectJPARepository.findById(6L)).thenReturn(Optional.of(entity));
        when(ensSubjectMapper.toModel(entity)).thenReturn(model);

        EnsSubject result = adapter.findById(6L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(ensSubjectJPARepository.findById(6L)).thenReturn(Optional.empty());

        EnsSubject result = adapter.findById(6L);

        assertNull(result);
    }
}
