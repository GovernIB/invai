package es.caib.invai.back.persistence.repository.maintenance.security.webContext;

import es.caib.invai.back.persistence.model.maintenance.security.webContext.WebContextAudEntity;
import es.caib.invai.back.persistence.model.maintenance.security.webContext.WebContextEntity;
import es.caib.invai.back.service.mapper.maintenance.security.webContext.WebContextMapper;
import es.caib.invai.back.service.model.maintenance.security.webContext.WebContext;
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
 * Unit tests for {@link WebContextRepositoryAdapter}, verifying entity/model delegation to the
 * {@link WebContextJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class WebContextRepositoryAdapterTest {

    @Mock
    private WebContextJPARepository webContextJPARepository;

    @Mock
    private WebContextAudJPARepository webContextAudJPARepository;

    @Mock
    private WebContextMapper webContextMapper;

    private WebContextRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private WebContextRepositoryAdapter buildAdapter() {
        WebContextRepositoryAdapter a = new WebContextRepositoryAdapter();
        ReflectionTestUtils.setField(a, "webContextJPARepository", webContextJPARepository);
        ReflectionTestUtils.setField(a, "webContextAudJPARepository", webContextAudJPARepository);
        ReflectionTestUtils.setField(a, "webContextMapper", webContextMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        WebContextEntity entity = new WebContextEntity();
        entity.setId(1L);
        WebContext model = new WebContext();
        when(webContextJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(webContextMapper.toModel(entity)).thenReturn(model);

        WebContext result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(webContextJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        WebContextCriteria criteria = new WebContextCriteria();
        Pageable pageable = Pageable.unpaged();
        WebContextEntity entity = new WebContextEntity();
        WebContext model = new WebContext();
        Page<WebContextEntity> entityPage = new PageImpl<>(List.of(entity));
        when(webContextJPARepository.findAll(ArgumentMatchers.<Specification<WebContextEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(webContextMapper.toModel(entity)).thenReturn(model);

        Page<WebContext> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByNameAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(webContextJPARepository.existsByNameAndDeletedAtIsNull("Firmar peticiones")).thenReturn(true);

        assertTrue(adapter.existsByNameAndDeletedAtIsNull("Firmar peticiones"));
    }

    @Test
    void existsByNameAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(webContextJPARepository.existsByNameAndIdNotAndDeletedAtIsNull("Firmar peticiones", 1L)).thenReturn(true);

        assertTrue(adapter.existsByNameAndIdNotAndDeletedAtIsNull("Firmar peticiones", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        WebContext model = new WebContext();
        WebContextEntity toSave = new WebContextEntity();
        WebContextEntity saved = new WebContextEntity();
        saved.setId(5L);
        saved.setName("Firmar peticiones");
        saved.setNameEs("Firmar peticiones");
        WebContext response = new WebContext();
        when(webContextMapper.toEntity(model)).thenReturn(toSave);
        when(webContextJPARepository.save(toSave)).thenReturn(saved);
        when(webContextMapper.toModel(saved)).thenReturn(response);

        WebContext result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<WebContextAudEntity> captor = ArgumentCaptor.forClass(WebContextAudEntity.class);
        verify(webContextAudJPARepository).save(captor.capture());
        WebContextAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getWebContextId());
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
        WebContext model = new WebContext();
        WebContextEntity toSave = new WebContextEntity();
        WebContextEntity saved = new WebContextEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(webContextMapper.toEntity(model)).thenReturn(toSave);
        when(webContextJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<WebContextAudEntity> captor = ArgumentCaptor.forClass(WebContextAudEntity.class);
        verify(webContextAudJPARepository).save(captor.capture());
        WebContextAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        WebContext model = new WebContext();
        WebContextEntity toSave = new WebContextEntity();
        WebContextEntity saved = new WebContextEntity();
        saved.setId(7L);
        WebContext response = new WebContext();
        when(webContextMapper.toEntity(model)).thenReturn(toSave);
        when(webContextJPARepository.save(toSave)).thenReturn(saved);
        when(webContextMapper.toModel(saved)).thenReturn(response);

        WebContext result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<WebContextAudEntity> captor = ArgumentCaptor.forClass(WebContextAudEntity.class);
        verify(webContextAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        WebContext model = new WebContext();
        model.setId(8L);
        WebContextEntity toSave = new WebContextEntity();
        WebContextEntity saved = new WebContextEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(webContextMapper.toEntity(model)).thenReturn(toSave);
        when(webContextJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<WebContextAudEntity> captor = ArgumentCaptor.forClass(WebContextAudEntity.class);
        verify(webContextAudJPARepository).save(captor.capture());
        WebContextAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
