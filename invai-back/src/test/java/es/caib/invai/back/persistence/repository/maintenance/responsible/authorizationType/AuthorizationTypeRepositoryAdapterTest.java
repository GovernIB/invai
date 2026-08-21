package es.caib.invai.back.persistence.repository.maintenance.responsible.authorizationType;

import es.caib.invai.back.persistence.model.maintenance.responsible.authorizationType.AuthorizationTypeAudEntity;
import es.caib.invai.back.persistence.model.maintenance.responsible.authorizationType.AuthorizationTypeEntity;
import es.caib.invai.back.service.mapper.maintenance.responsible.authorizationType.AuthorizationTypeMapper;
import es.caib.invai.back.service.model.maintenance.responsible.authorizationType.AuthorizationType;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentMatchers;

/**
 * Unit tests for {@link AuthorizationTypeRepositoryAdapter}, verifying entity/model delegation to the
 * {@link AuthorizationTypeJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationTypeRepositoryAdapterTest {

    @Mock
    private AuthorizationTypeJPARepository authorizationTypeJPARepository;

    @Mock
    private AuthorizationTypeAudJPARepository authorizationTypeAudJPARepository;

    @Mock
    private AuthorizationTypeMapper authorizationTypeMapper;

    private AuthorizationTypeRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AuthorizationTypeRepositoryAdapter buildAdapter() {
        AuthorizationTypeRepositoryAdapter a = new AuthorizationTypeRepositoryAdapter();
        ReflectionTestUtils.setField(a, "authorizationTypeJPARepository", authorizationTypeJPARepository);
        ReflectionTestUtils.setField(a, "authorizationTypeAudJPARepository", authorizationTypeAudJPARepository);
        ReflectionTestUtils.setField(a, "authorizationTypeMapper", authorizationTypeMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        AuthorizationTypeEntity entity = new AuthorizationTypeEntity();
        entity.setId(1L);
        AuthorizationType model = new AuthorizationType();
        when(authorizationTypeJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(authorizationTypeMapper.toModel(entity)).thenReturn(model);

        AuthorizationType result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(authorizationTypeJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        AuthorizationTypeCriteria criteria = new AuthorizationTypeCriteria();
        Pageable pageable = Pageable.unpaged();
        AuthorizationTypeEntity entity = new AuthorizationTypeEntity();
        AuthorizationType model = new AuthorizationType();
        Page<AuthorizationTypeEntity> entityPage = new PageImpl<>(List.of(entity));
        when(authorizationTypeJPARepository.findAll(ArgumentMatchers.<Specification<AuthorizationTypeEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(authorizationTypeMapper.toModel(entity)).thenReturn(model);

        Page<AuthorizationType> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByNameAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(authorizationTypeJPARepository.existsByNameAndDeletedAtIsNull("Firmar peticiones")).thenReturn(true);

        assertEquals(true, adapter.existsByNameAndDeletedAtIsNull("Firmar peticiones"));
    }

    @Test
    void existsByNameAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(authorizationTypeJPARepository.existsByNameAndIdNotAndDeletedAtIsNull("Firmar peticiones", 1L)).thenReturn(true);

        assertEquals(true, adapter.existsByNameAndIdNotAndDeletedAtIsNull("Firmar peticiones", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        AuthorizationType model = new AuthorizationType();
        AuthorizationTypeEntity toSave = new AuthorizationTypeEntity();
        AuthorizationTypeEntity saved = new AuthorizationTypeEntity();
        saved.setId(5L);
        saved.setName("Firmar peticiones");
        saved.setNameEs("Firmar peticiones");
        AuthorizationType response = new AuthorizationType();
        when(authorizationTypeMapper.toEntity(model)).thenReturn(toSave);
        when(authorizationTypeJPARepository.save(toSave)).thenReturn(saved);
        when(authorizationTypeMapper.toModel(saved)).thenReturn(response);

        AuthorizationType result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<AuthorizationTypeAudEntity> captor = ArgumentCaptor.forClass(AuthorizationTypeAudEntity.class);
        verify(authorizationTypeAudJPARepository).save(captor.capture());
        AuthorizationTypeAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getAuthorizationTypeId());
        assertEquals("Firmar peticiones", aud.getName());
        assertEquals("Firmar peticiones", aud.getNameEs());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        AuthorizationType model = new AuthorizationType();
        AuthorizationTypeEntity toSave = new AuthorizationTypeEntity();
        AuthorizationTypeEntity saved = new AuthorizationTypeEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(authorizationTypeMapper.toEntity(model)).thenReturn(toSave);
        when(authorizationTypeJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AuthorizationTypeAudEntity> captor = ArgumentCaptor.forClass(AuthorizationTypeAudEntity.class);
        verify(authorizationTypeAudJPARepository).save(captor.capture());
        AuthorizationTypeAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        AuthorizationType model = new AuthorizationType();
        AuthorizationTypeEntity toSave = new AuthorizationTypeEntity();
        AuthorizationTypeEntity saved = new AuthorizationTypeEntity();
        saved.setId(7L);
        AuthorizationType response = new AuthorizationType();
        when(authorizationTypeMapper.toEntity(model)).thenReturn(toSave);
        when(authorizationTypeJPARepository.save(toSave)).thenReturn(saved);
        when(authorizationTypeMapper.toModel(saved)).thenReturn(response);

        AuthorizationType result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<AuthorizationTypeAudEntity> captor = ArgumentCaptor.forClass(AuthorizationTypeAudEntity.class);
        verify(authorizationTypeAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        AuthorizationType model = new AuthorizationType();
        model.setId(8L);
        AuthorizationTypeEntity toSave = new AuthorizationTypeEntity();
        AuthorizationTypeEntity saved = new AuthorizationTypeEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(authorizationTypeMapper.toEntity(model)).thenReturn(toSave);
        when(authorizationTypeJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<AuthorizationTypeAudEntity> captor = ArgumentCaptor.forClass(AuthorizationTypeAudEntity.class);
        verify(authorizationTypeAudJPARepository).save(captor.capture());
        AuthorizationTypeAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
