package es.caib.invai.back.persistence.repository.catalog.standardAdaption;

import es.caib.invai.back.persistence.model.catalog.standardAdaption.LkupStandardAdaptionEntity;
import es.caib.invai.back.service.mapper.catalog.standardAdaption.StandardAdaptionMapper;
import es.caib.invai.back.service.model.catalog.standardAdaption.StandardAdaption;
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
 * Unit tests for {@link StandardAdaptionRepositoryAdapter}, a simple read-only lookup adapter
 * with no Criteria/Specification support and no create/update/delete/audit behavior.
 */
@ExtendWith(MockitoExtension.class)
class StandardAdaptionRepositoryAdapterTest {

    @Mock
    private StandardAdaptionJPARepository standardAdaptionJPARepository;

    @Mock
    private StandardAdaptionMapper standardAdaptionMapper;

    private StandardAdaptionRepositoryAdapter adapter;

    private StandardAdaptionRepositoryAdapter buildAdapter() {
        StandardAdaptionRepositoryAdapter a = new StandardAdaptionRepositoryAdapter();
        ReflectionTestUtils.setField(a, "standardAdaptionJPARepository", standardAdaptionJPARepository);
        ReflectionTestUtils.setField(a, "standardAdaptionMapper", standardAdaptionMapper);
        return a;
    }

    @Test
    void findAll_delegatesToJPARepositorySortedByNameAndMapsList() {
        adapter = buildAdapter();
        LkupStandardAdaptionEntity entity = new LkupStandardAdaptionEntity();
        List<LkupStandardAdaptionEntity> entities = List.of(entity);
        List<StandardAdaption> models = List.of(new StandardAdaption());
        when(standardAdaptionJPARepository.findAll(Sort.by("name"))).thenReturn(entities);
        when(standardAdaptionMapper.toModelList(entities)).thenReturn(models);

        List<StandardAdaption> result = adapter.findAll();

        assertSame(models, result);
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(standardAdaptionJPARepository).findAll(sortCaptor.capture());
        assertTrue(sortCaptor.getValue().getOrderFor("name") != null);
        assertEquals(Sort.Direction.ASC, sortCaptor.getValue().getOrderFor("name").getDirection());
    }

    @Test
    void findAll_emptyRepository_returnsMappedEmptyList() {
        adapter = buildAdapter();
        when(standardAdaptionJPARepository.findAll(Sort.by("name"))).thenReturn(List.of());
        when(standardAdaptionMapper.toModelList(List.of())).thenReturn(List.of());

        List<StandardAdaption> result = adapter.findAll();

        assertEquals(0, result.size());
    }
}
