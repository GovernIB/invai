package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.type;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.authorized.AppAuthorizedEntity;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.type.AppAuthorizedTypeLinkAudEntity;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.type.AppAuthorizedTypeLinkEntity;
import es.caib.invai.back.persistence.model.maintenance.responsible.authorizationType.AuthorizationTypeEntity;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.type.AppAuthorizedTypeLinkMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.type.AppAuthorizedTypeLink;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppAuthorizedTypeLinkRepositoryAdapter}, verifying entity/model delegation
 * to {@link AppAuthorizedTypeLinkJPARepository} and the audit trail record written on every
 * mutation (since 1.0.5 - this join was hard-delete/no-audit before).
 */
@ExtendWith(MockitoExtension.class)
class AppAuthorizedTypeLinkRepositoryAdapterTest {

    @Mock
    private AppAuthorizedTypeLinkJPARepository appAuthorizedTypeLinkJPARepository;

    @Mock
    private AppAuthorizedTypeLinkAudJPARepository appAuthorizedTypeLinkAudJPARepository;

    @Mock
    private AppAuthorizedTypeLinkMapper appAuthorizedTypeLinkMapper;

    private AppAuthorizedTypeLinkRepositoryAdapter buildAdapter() {
        AppAuthorizedTypeLinkRepositoryAdapter adapter = new AppAuthorizedTypeLinkRepositoryAdapter();
        ReflectionTestUtils.setField(adapter, "appAuthorizedTypeLinkJPARepository", appAuthorizedTypeLinkJPARepository);
        ReflectionTestUtils.setField(adapter, "appAuthorizedTypeLinkAudJPARepository", appAuthorizedTypeLinkAudJPARepository);
        ReflectionTestUtils.setField(adapter, "appAuthorizedTypeLinkMapper", appAuthorizedTypeLinkMapper);
        return adapter;
    }

    private AppAuthorizedTypeLinkEntity entityWithRelations(Long id) {
        AppAuthorizedTypeLinkEntity entity = new AppAuthorizedTypeLinkEntity();
        entity.setId(id);
        AppAuthorizedEntity appAuthorized = new AppAuthorizedEntity();
        appAuthorized.setId(10L);
        entity.setAppAuthorized(appAuthorized);
        AuthorizationTypeEntity authorizationType = new AuthorizationTypeEntity();
        authorizationType.setId(20L);
        entity.setAuthorizationType(authorizationType);
        return entity;
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        AppAuthorizedTypeLinkRepositoryAdapter adapter = buildAdapter();
        AppAuthorizedTypeLink model = AppAuthorizedTypeLink.builder().appAuthorizedId(10L).authorizationTypeId(20L).build();
        AppAuthorizedTypeLinkEntity toSave = new AppAuthorizedTypeLinkEntity();
        AppAuthorizedTypeLinkEntity saved = entityWithRelations(5L);
        AppAuthorizedTypeLink response = AppAuthorizedTypeLink.builder().id(5L).build();
        when(appAuthorizedTypeLinkMapper.toEntity(model)).thenReturn(toSave);
        when(appAuthorizedTypeLinkJPARepository.save(toSave)).thenReturn(saved);
        when(appAuthorizedTypeLinkMapper.toModel(saved)).thenReturn(response);

        AppAuthorizedTypeLink result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<AppAuthorizedTypeLinkAudEntity> captor = ArgumentCaptor.forClass(AppAuthorizedTypeLinkAudEntity.class);
        verify(appAuthorizedTypeLinkAudJPARepository).save(captor.capture());
        AppAuthorizedTypeLinkAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getAppAuthorizedTypeLinkId());
        assertEquals(10L, aud.getAppAuthorizedId());
        assertEquals(20L, aud.getAuthorizationTypeId());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getAuditDate());
    }

    @Test
    void delete_savesEntityAndWritesDeleteAuditRecord() {
        AppAuthorizedTypeLinkRepositoryAdapter adapter = buildAdapter();
        AppAuthorizedTypeLink model = AppAuthorizedTypeLink.builder().id(7L).build();
        AppAuthorizedTypeLinkEntity toSave = new AppAuthorizedTypeLinkEntity();
        AppAuthorizedTypeLinkEntity saved = entityWithRelations(7L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(appAuthorizedTypeLinkMapper.toEntity(model)).thenReturn(toSave);
        when(appAuthorizedTypeLinkJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        ArgumentCaptor<AppAuthorizedTypeLinkAudEntity> captor = ArgumentCaptor.forClass(AppAuthorizedTypeLinkAudEntity.class);
        verify(appAuthorizedTypeLinkAudJPARepository).save(captor.capture());
        AppAuthorizedTypeLinkAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }

    @Test
    void findAllActiveByAppAuthorizedId_mapsEveryEntry() {
        AppAuthorizedTypeLinkRepositoryAdapter adapter = buildAdapter();
        AppAuthorizedTypeLinkEntity entity1 = new AppAuthorizedTypeLinkEntity();
        AppAuthorizedTypeLinkEntity entity2 = new AppAuthorizedTypeLinkEntity();
        AppAuthorizedTypeLink model1 = AppAuthorizedTypeLink.builder().id(1L).build();
        AppAuthorizedTypeLink model2 = AppAuthorizedTypeLink.builder().id(2L).build();

        when(appAuthorizedTypeLinkJPARepository.findAllByAppAuthorizedIdAndDeletedAtIsNull(99L))
                .thenReturn(List.of(entity1, entity2));
        when(appAuthorizedTypeLinkMapper.toModel(entity1)).thenReturn(model1);
        when(appAuthorizedTypeLinkMapper.toModel(entity2)).thenReturn(model2);

        List<AppAuthorizedTypeLink> result = adapter.findAllActiveByAppAuthorizedId(99L);

        assertEquals(List.of(model1, model2), result);
    }

    @Test
    void findAllActiveByAppAuthorizedId_emptyRepository_returnsEmptyList() {
        AppAuthorizedTypeLinkRepositoryAdapter adapter = buildAdapter();
        when(appAuthorizedTypeLinkJPARepository.findAllByAppAuthorizedIdAndDeletedAtIsNull(5L)).thenReturn(List.of());

        List<AppAuthorizedTypeLink> result = adapter.findAllActiveByAppAuthorizedId(5L);

        assertEquals(0, result.size());
    }
}
