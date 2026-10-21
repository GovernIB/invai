package es.caib.invai.back.persistence.repository.catalog.validationStatus;

import es.caib.invai.back.persistence.model.catalog.validationStatus.LkupValidationStatusEntity;
import es.caib.invai.back.service.mapper.catalog.validationStatus.ValidationStatusMapper;
import es.caib.invai.back.service.model.catalog.validationStatus.ValidationStatus;
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
 * Unit tests for {@link ValidationStatusRepositoryAdapter}, a simple read-only lookup adapter with no
 * Criteria/Specification support and no create/update/delete/audit behavior.
 */
@ExtendWith(MockitoExtension.class)
class ValidationStatusRepositoryAdapterTest {

    @Mock
    private ValidationStatusJPARepository validationStatusJPARepository;

    @Mock
    private ValidationStatusMapper validationStatusMapper;

    private ValidationStatusRepositoryAdapter adapter;

    private ValidationStatusRepositoryAdapter buildAdapter() {
        ValidationStatusRepositoryAdapter a = new ValidationStatusRepositoryAdapter();
        ReflectionTestUtils.setField(a, "validationStatusJPARepository", validationStatusJPARepository);
        ReflectionTestUtils.setField(a, "validationStatusMapper", validationStatusMapper);
        return a;
    }

    @Test
    void findAll_delegatesToJPARepositorySortedByNameAndMapsList() {
        adapter = buildAdapter();
        LkupValidationStatusEntity entity = new LkupValidationStatusEntity();
        List<LkupValidationStatusEntity> entities = List.of(entity);
        List<ValidationStatus> models = List.of(new ValidationStatus());
        when(validationStatusJPARepository.findAll(Sort.by("name"))).thenReturn(entities);
        when(validationStatusMapper.toModelList(entities)).thenReturn(models);

        List<ValidationStatus> result = adapter.findAll();

        assertSame(models, result);
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(validationStatusJPARepository).findAll(sortCaptor.capture());
        assertNotNull(sortCaptor.getValue().getOrderFor("name"));
        assertEquals(Sort.Direction.ASC, Objects.requireNonNull(sortCaptor.getValue().getOrderFor("name")).getDirection());
    }

    @Test
    void findAll_emptyRepository_returnsMappedEmptyList() {
        adapter = buildAdapter();
        when(validationStatusJPARepository.findAll(Sort.by("name"))).thenReturn(List.of());
        when(validationStatusMapper.toModelList(List.of())).thenReturn(List.of());

        List<ValidationStatus> result = adapter.findAll();

        assertEquals(0, result.size());
    }
}
