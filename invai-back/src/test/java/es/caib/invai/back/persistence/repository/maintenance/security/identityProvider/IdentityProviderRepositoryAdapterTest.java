package es.caib.invai.back.persistence.repository.maintenance.security.identityProvider;

import es.caib.invai.back.persistence.model.maintenance.security.identityProvider.IdentityProviderAudEntity;
import es.caib.invai.back.persistence.model.maintenance.security.identityProvider.IdentityProviderEntity;
import es.caib.invai.back.service.mapper.maintenance.security.identityProvider.IdentityProviderMapper;
import es.caib.invai.back.service.model.maintenance.security.identityProvider.IdentityProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentMatchers;

/**
 * Unit tests for {@link IdentityProviderRepositoryAdapter}, verifying entity/model delegation to the
 * {@link IdentityProviderJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class IdentityProviderRepositoryAdapterTest {

    @Mock
    private IdentityProviderJPARepository identityProviderJPARepository;

    @Mock
    private IdentityProviderAudJPARepository identityProviderAudJPARepository;

    @Mock
    private IdentityProviderMapper identityProviderMapper;

    private IdentityProviderRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private IdentityProviderRepositoryAdapter buildAdapter() {
        IdentityProviderRepositoryAdapter a = new IdentityProviderRepositoryAdapter();
        ReflectionTestUtils.setField(a, "identityProviderJPARepository", identityProviderJPARepository);
        ReflectionTestUtils.setField(a, "identityProviderAudJPARepository", identityProviderAudJPARepository);
        ReflectionTestUtils.setField(a, "identityProviderMapper", identityProviderMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        IdentityProviderEntity entity = new IdentityProviderEntity();
        entity.setId(1L);
        IdentityProvider model = new IdentityProvider();
        when(identityProviderJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(identityProviderMapper.toModel(entity)).thenReturn(model);

        IdentityProvider result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(identityProviderJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        IdentityProviderCriteria criteria = new IdentityProviderCriteria();
        Pageable pageable = Pageable.unpaged();
        IdentityProviderEntity entity = new IdentityProviderEntity();
        IdentityProvider model = new IdentityProvider();
        Page<IdentityProviderEntity> entityPage = new PageImpl<>(List.of(entity));
        when(identityProviderJPARepository.findAll(ArgumentMatchers.<Specification<IdentityProviderEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(identityProviderMapper.toModel(entity)).thenReturn(model);

        Page<IdentityProvider> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByNameAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(identityProviderJPARepository.existsByNameAndDeletedAtIsNull("Cl@ve")).thenReturn(true);

        assertTrue(adapter.existsByNameAndDeletedAtIsNull("Cl@ve"));
    }

    @Test
    void existsByNameAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(identityProviderJPARepository.existsByNameAndIdNotAndDeletedAtIsNull("Cl@ve", 1L)).thenReturn(true);

        assertTrue(adapter.existsByNameAndIdNotAndDeletedAtIsNull("Cl@ve", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        IdentityProvider model = new IdentityProvider();
        IdentityProviderEntity toSave = new IdentityProviderEntity();
        IdentityProviderEntity saved = new IdentityProviderEntity();
        saved.setId(5L);
        saved.setName("Cl@ve");
        IdentityProvider response = new IdentityProvider();
        when(identityProviderMapper.toEntity(model)).thenReturn(toSave);
        when(identityProviderJPARepository.save(toSave)).thenReturn(saved);
        when(identityProviderMapper.toModel(saved)).thenReturn(response);

        IdentityProvider result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<IdentityProviderAudEntity> captor = ArgumentCaptor.forClass(IdentityProviderAudEntity.class);
        verify(identityProviderAudJPARepository).save(captor.capture());
        IdentityProviderAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getIdentityProviderId());
        assertEquals("Cl@ve", aud.getName());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        IdentityProvider model = new IdentityProvider();
        IdentityProviderEntity toSave = new IdentityProviderEntity();
        IdentityProviderEntity saved = new IdentityProviderEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(identityProviderMapper.toEntity(model)).thenReturn(toSave);
        when(identityProviderJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<IdentityProviderAudEntity> captor = ArgumentCaptor.forClass(IdentityProviderAudEntity.class);
        verify(identityProviderAudJPARepository).save(captor.capture());
        IdentityProviderAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        IdentityProvider model = new IdentityProvider();
        IdentityProviderEntity toSave = new IdentityProviderEntity();
        IdentityProviderEntity saved = new IdentityProviderEntity();
        saved.setId(7L);
        IdentityProvider response = new IdentityProvider();
        when(identityProviderMapper.toEntity(model)).thenReturn(toSave);
        when(identityProviderJPARepository.save(toSave)).thenReturn(saved);
        when(identityProviderMapper.toModel(saved)).thenReturn(response);

        IdentityProvider result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<IdentityProviderAudEntity> captor = ArgumentCaptor.forClass(IdentityProviderAudEntity.class);
        verify(identityProviderAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        IdentityProvider model = new IdentityProvider();
        model.setId(8L);
        IdentityProviderEntity toSave = new IdentityProviderEntity();
        IdentityProviderEntity saved = new IdentityProviderEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(identityProviderMapper.toEntity(model)).thenReturn(toSave);
        when(identityProviderJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<IdentityProviderAudEntity> captor = ArgumentCaptor.forClass(IdentityProviderAudEntity.class);
        verify(identityProviderAudJPARepository).save(captor.capture());
        IdentityProviderAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
