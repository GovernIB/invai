package es.caib.invai.back.persistence.repository.catalog.responsibleType;

import es.caib.invai.back.persistence.model.catalog.responsibleType.LkupResponsibleTypeEntity;
import es.caib.invai.back.service.mapper.catalog.responsibleType.ResponsibleTypeMapper;
import es.caib.invai.back.service.model.catalog.responsibleType.ResponsibleType;
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
 * Unit tests for {@link ResponsibleTypeRepositoryAdapter}, a simple read-only lookup adapter
 * with no Criteria/Specification support and no create/update/delete/audit behavior.
 */
@ExtendWith(MockitoExtension.class)
class ResponsibleTypeRepositoryAdapterTest {

    @Mock
    private ResponsibleTypeJPARepository responsibleTypeJPARepository;

    @Mock
    private ResponsibleTypeMapper responsibleTypeMapper;

    private ResponsibleTypeRepositoryAdapter adapter;

    private ResponsibleTypeRepositoryAdapter buildAdapter() {
        ResponsibleTypeRepositoryAdapter a = new ResponsibleTypeRepositoryAdapter();
        ReflectionTestUtils.setField(a, "responsibleTypeJPARepository", responsibleTypeJPARepository);
        ReflectionTestUtils.setField(a, "responsibleTypeMapper", responsibleTypeMapper);
        return a;
    }

    @Test
    void findAll_delegatesToJPARepositorySortedByNameAndMapsList() {
        adapter = buildAdapter();
        LkupResponsibleTypeEntity entity = new LkupResponsibleTypeEntity();
        List<LkupResponsibleTypeEntity> entities = List.of(entity);
        List<ResponsibleType> models = List.of(new ResponsibleType());
        when(responsibleTypeJPARepository.findAll(Sort.by("name"))).thenReturn(entities);
        when(responsibleTypeMapper.toModelList(entities)).thenReturn(models);

        List<ResponsibleType> result = adapter.findAll();

        assertSame(models, result);
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(responsibleTypeJPARepository).findAll(sortCaptor.capture());
        assertNotNull(sortCaptor.getValue().getOrderFor("name"));
        assertEquals(Sort.Direction.ASC, Objects.requireNonNull(sortCaptor.getValue().getOrderFor("name")).getDirection());
    }

    @Test
    void findAll_emptyRepository_returnsMappedEmptyList() {
        adapter = buildAdapter();
        when(responsibleTypeJPARepository.findAll(Sort.by("name"))).thenReturn(List.of());
        when(responsibleTypeMapper.toModelList(List.of())).thenReturn(List.of());

        List<ResponsibleType> result = adapter.findAll();

        assertEquals(0, result.size());
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        LkupResponsibleTypeEntity entity = new LkupResponsibleTypeEntity();
        ResponsibleType model = new ResponsibleType();
        when(responsibleTypeJPARepository.findById(6L)).thenReturn(Optional.of(entity));
        when(responsibleTypeMapper.toModel(entity)).thenReturn(model);

        ResponsibleType result = adapter.findById(6L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(responsibleTypeJPARepository.findById(6L)).thenReturn(Optional.empty());

        ResponsibleType result = adapter.findById(6L);

        assertNull(result);
    }
}
