package es.caib.invai.back.persistence.repository.catalog.securityLevel;

import es.caib.invai.back.persistence.model.catalog.securityLevel.LkupSecurityLevelEntity;
import es.caib.invai.back.service.mapper.catalog.securityLevel.SecurityLevelMapper;
import es.caib.invai.back.service.model.catalog.securityLevel.SecurityLevel;
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
 * Unit tests for {@link SecurityLevelRepositoryAdapter}, a simple read-only lookup adapter
 * with no Criteria/Specification support and no create/update/delete/audit behavior.
 */
@ExtendWith(MockitoExtension.class)
class SecurityLevelRepositoryAdapterTest {

    @Mock
    private SecurityLevelJPARepository securityLevelJPARepository;

    @Mock
    private SecurityLevelMapper securityLevelMapper;

    private SecurityLevelRepositoryAdapter adapter;

    private SecurityLevelRepositoryAdapter buildAdapter() {
        SecurityLevelRepositoryAdapter a = new SecurityLevelRepositoryAdapter();
        ReflectionTestUtils.setField(a, "securityLevelJPARepository", securityLevelJPARepository);
        ReflectionTestUtils.setField(a, "securityLevelMapper", securityLevelMapper);
        return a;
    }

    @Test
    void findAll_delegatesToJPARepositorySortedByNameAndMapsList() {
        adapter = buildAdapter();
        LkupSecurityLevelEntity entity = new LkupSecurityLevelEntity();
        List<LkupSecurityLevelEntity> entities = List.of(entity);
        List<SecurityLevel> models = List.of(new SecurityLevel());
        when(securityLevelJPARepository.findAll(Sort.by("name"))).thenReturn(entities);
        when(securityLevelMapper.toModelList(entities)).thenReturn(models);

        List<SecurityLevel> result = adapter.findAll();

        assertSame(models, result);
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(securityLevelJPARepository).findAll(sortCaptor.capture());
        assertNotNull(sortCaptor.getValue().getOrderFor("name"));
        assertEquals(Sort.Direction.ASC, Objects.requireNonNull(sortCaptor.getValue().getOrderFor("name")).getDirection());
    }

    @Test
    void findAll_emptyRepository_returnsMappedEmptyList() {
        adapter = buildAdapter();
        when(securityLevelJPARepository.findAll(Sort.by("name"))).thenReturn(List.of());
        when(securityLevelMapper.toModelList(List.of())).thenReturn(List.of());

        List<SecurityLevel> result = adapter.findAll();

        assertEquals(0, result.size());
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        LkupSecurityLevelEntity entity = new LkupSecurityLevelEntity();
        SecurityLevel model = new SecurityLevel();
        when(securityLevelJPARepository.findById(6L)).thenReturn(Optional.of(entity));
        when(securityLevelMapper.toModel(entity)).thenReturn(model);

        SecurityLevel result = adapter.findById(6L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(securityLevelJPARepository.findById(6L)).thenReturn(Optional.empty());

        SecurityLevel result = adapter.findById(6L);

        assertNull(result);
    }
}
