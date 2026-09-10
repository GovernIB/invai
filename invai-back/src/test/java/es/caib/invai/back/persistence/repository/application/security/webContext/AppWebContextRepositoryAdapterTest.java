package es.caib.invai.back.persistence.repository.application.security.webContext;

import es.caib.invai.back.persistence.model.application.security.webContext.AppWebContextAudEntity;
import es.caib.invai.back.persistence.model.application.security.webContext.AppWebContextEntity;
import es.caib.invai.back.persistence.model.application.security.core.AppSecurityEntity;
import es.caib.invai.back.persistence.model.maintenance.security.webContext.WebContextEntity;
import es.caib.invai.back.persistence.model.maintenance.general.field.FieldEntity;
import es.caib.invai.back.service.mapper.application.security.webContext.AppWebContextMapper;
import es.caib.invai.back.service.model.application.security.webContext.AppWebContext;
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
 * Unit tests for {@link AppWebContextRepositoryAdapter}, verifying entity/model delegation to the
 * {@link AppWebContextJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class AppWebContextRepositoryAdapterTest {

    @Mock
    private AppWebContextJPARepository appWebContextJPARepository;

    @Mock
    private AppWebContextAudJPARepository appWebContextAudJPARepository;

    @Mock
    private AppWebContextMapper appWebContextMapper;

    private AppWebContextRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AppWebContextRepositoryAdapter buildAdapter() {
        AppWebContextRepositoryAdapter a = new AppWebContextRepositoryAdapter();
        ReflectionTestUtils.setField(a, "appWebContextJPARepository", appWebContextJPARepository);
        ReflectionTestUtils.setField(a, "appWebContextAudJPARepository", appWebContextAudJPARepository);
        ReflectionTestUtils.setField(a, "appWebContextMapper", appWebContextMapper);
        return a;
    }

    private AppWebContextEntity entityWithRelations(Long id) {
        AppWebContextEntity entity = new AppWebContextEntity();
        entity.setId(id);
        AppSecurityEntity appSecurity = new AppSecurityEntity();
        appSecurity.setId(10L);
        entity.setAppSecurity(appSecurity);
        WebContextEntity webContext = new WebContextEntity();
        webContext.setId(20L);
        entity.setWebContext(webContext);
        FieldEntity field = new FieldEntity();
        field.setId(30L);
        entity.setField(field);
        return entity;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        AppWebContextEntity entity = entityWithRelations(1L);
        AppWebContext model = new AppWebContext();
        when(appWebContextJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appWebContextMapper.toModel(entity)).thenReturn(model);

        AppWebContext result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(appWebContextJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        AppWebContextCriteria criteria = new AppWebContextCriteria();
        Pageable pageable = Pageable.unpaged();
        AppWebContextEntity entity = entityWithRelations(1L);
        AppWebContext model = new AppWebContext();
        Page<AppWebContextEntity> entityPage = new PageImpl<>(List.of(entity));
        when(appWebContextJPARepository.findAll(ArgumentMatchers.<Specification<AppWebContextEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(appWebContextMapper.toModel(entity)).thenReturn(model);

        Page<AppWebContext> result = adapter.findAll(10L, criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        AppWebContext model = new AppWebContext();
        AppWebContextEntity toSave = new AppWebContextEntity();
        AppWebContextEntity saved = entityWithRelations(5L);
        AppWebContext response = new AppWebContext();
        when(appWebContextMapper.toEntity(model)).thenReturn(toSave);
        when(appWebContextJPARepository.save(toSave)).thenReturn(saved);
        when(appWebContextMapper.toModel(saved)).thenReturn(response);

        AppWebContext result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<AppWebContextAudEntity> captor = ArgumentCaptor.forClass(AppWebContextAudEntity.class);
        verify(appWebContextAudJPARepository).save(captor.capture());
        AppWebContextAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getAppWebContextId());
        assertEquals(10L, aud.getAppSecurityId());
        assertEquals(20L, aud.getWebContextId());
        assertEquals(30L, aud.getFieldId());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        AppWebContext model = new AppWebContext();
        AppWebContextEntity toSave = new AppWebContextEntity();
        AppWebContextEntity saved = entityWithRelations(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(appWebContextMapper.toEntity(model)).thenReturn(toSave);
        when(appWebContextJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<AppWebContextAudEntity> captor = ArgumentCaptor.forClass(AppWebContextAudEntity.class);
        verify(appWebContextAudJPARepository).save(captor.capture());
        AppWebContextAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        AppWebContext model = new AppWebContext();
        AppWebContextEntity toSave = new AppWebContextEntity();
        AppWebContextEntity saved = entityWithRelations(7L);
        AppWebContext response = new AppWebContext();
        when(appWebContextMapper.toEntity(model)).thenReturn(toSave);
        when(appWebContextJPARepository.save(toSave)).thenReturn(saved);
        when(appWebContextMapper.toModel(saved)).thenReturn(response);

        AppWebContext result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<AppWebContextAudEntity> captor = ArgumentCaptor.forClass(AppWebContextAudEntity.class);
        verify(appWebContextAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        AppWebContext model = new AppWebContext();
        model.setId(8L);
        AppWebContextEntity toSave = new AppWebContextEntity();
        AppWebContextEntity saved = entityWithRelations(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(appWebContextMapper.toEntity(model)).thenReturn(toSave);
        when(appWebContextJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<AppWebContextAudEntity> captor = ArgumentCaptor.forClass(AppWebContextAudEntity.class);
        verify(appWebContextAudJPARepository).save(captor.capture());
        AppWebContextAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
