package es.caib.invai.back.persistence.repository.catalog.modality;

import es.caib.invai.back.persistence.model.catalog.modality.LkupModalityEntity;
import es.caib.invai.back.service.mapper.catalog.modality.ModalityMapper;
import es.caib.invai.back.service.model.catalog.modality.Modality;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ModalityRepositoryAdapter}, a simple read-only lookup adapter with no
 * Criteria/Specification support and no create/update/delete/audit behavior.
 */
@ExtendWith(MockitoExtension.class)
class ModalityRepositoryAdapterTest {

    @Mock
    private ModalityJPARepository modalityJPARepository;

    @Mock
    private ModalityMapper modalityMapper;

    private ModalityRepositoryAdapter adapter;

    private ModalityRepositoryAdapter buildAdapter() {
        ModalityRepositoryAdapter a = new ModalityRepositoryAdapter();
        ReflectionTestUtils.setField(a, "modalityJPARepository", modalityJPARepository);
        ReflectionTestUtils.setField(a, "modalityMapper", modalityMapper);
        return a;
    }

    @Test
    void findAll_delegatesToJPARepositorySortedByNameAndMapsList() {
        adapter = buildAdapter();
        LkupModalityEntity entity = new LkupModalityEntity();
        List<LkupModalityEntity> entities = List.of(entity);
        List<Modality> models = List.of(new Modality());
        when(modalityJPARepository.findAll(Sort.by("name"))).thenReturn(entities);
        when(modalityMapper.toModelList(entities)).thenReturn(models);

        List<Modality> result = adapter.findAll();

        assertSame(models, result);
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(modalityJPARepository).findAll(sortCaptor.capture());
        assertTrue(sortCaptor.getValue().getOrderFor("name") != null);
        assertEquals(Sort.Direction.ASC, sortCaptor.getValue().getOrderFor("name").getDirection());
    }

    @Test
    void findAll_emptyRepository_returnsMappedEmptyList() {
        adapter = buildAdapter();
        when(modalityJPARepository.findAll(Sort.by("name"))).thenReturn(List.of());
        when(modalityMapper.toModelList(List.of())).thenReturn(List.of());

        List<Modality> result = adapter.findAll();

        assertEquals(0, result.size());
    }
}
