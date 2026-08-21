package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.type;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.type.AppAuthorizedTypeLinkEntity;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.type.AppAuthorizedTypeLinkMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.type.AppAuthorizedTypeLink;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppAuthorizedTypeLinkRepositoryAdapter}, a deliberately lightweight join
 * adapter with only create/delete/findAll behavior and no audit trail or soft-delete.
 */
@ExtendWith(MockitoExtension.class)
class AppAuthorizedTypeLinkRepositoryAdapterTest {

    @Mock
    private AppAuthorizedTypeLinkJPARepository appAuthorizedTypeLinkJPARepository;

    @Mock
    private AppAuthorizedTypeLinkMapper appAuthorizedTypeLinkMapper;

    private AppAuthorizedTypeLinkRepositoryAdapter buildAdapter() {
        AppAuthorizedTypeLinkRepositoryAdapter adapter = new AppAuthorizedTypeLinkRepositoryAdapter();
        ReflectionTestUtils.setField(adapter, "appAuthorizedTypeLinkJPARepository", appAuthorizedTypeLinkJPARepository);
        ReflectionTestUtils.setField(adapter, "appAuthorizedTypeLinkMapper", appAuthorizedTypeLinkMapper);
        return adapter;
    }

    @Test
    void create_mapsSavesAndReturnsModel() {
        AppAuthorizedTypeLinkRepositoryAdapter adapter = buildAdapter();
        AppAuthorizedTypeLink model = AppAuthorizedTypeLink.builder()
                .appAuthorizedId(10L)
                .authorizationTypeId(20L)
                .build();
        AppAuthorizedTypeLinkEntity entityToSave = new AppAuthorizedTypeLinkEntity();
        AppAuthorizedTypeLinkEntity savedEntity = new AppAuthorizedTypeLinkEntity();
        AppAuthorizedTypeLink savedModel = AppAuthorizedTypeLink.builder()
                .id(1L)
                .appAuthorizedId(10L)
                .authorizationTypeId(20L)
                .build();

        when(appAuthorizedTypeLinkMapper.toEntity(model)).thenReturn(entityToSave);
        when(appAuthorizedTypeLinkJPARepository.save(entityToSave)).thenReturn(savedEntity);
        when(appAuthorizedTypeLinkMapper.toModel(savedEntity)).thenReturn(savedModel);

        AppAuthorizedTypeLink result = adapter.create(model);

        assertSame(savedModel, result);
    }

    @Test
    void delete_callsDeleteByIdWithModelsId() {
        AppAuthorizedTypeLinkRepositoryAdapter adapter = buildAdapter();
        AppAuthorizedTypeLink model = AppAuthorizedTypeLink.builder().id(7L).build();

        adapter.delete(model);

        verify(appAuthorizedTypeLinkJPARepository).deleteById(7L);
        verify(appAuthorizedTypeLinkJPARepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void findAllByAppAuthorizedId_mapsEveryEntry() {
        AppAuthorizedTypeLinkRepositoryAdapter adapter = buildAdapter();
        AppAuthorizedTypeLinkEntity entity1 = new AppAuthorizedTypeLinkEntity();
        AppAuthorizedTypeLinkEntity entity2 = new AppAuthorizedTypeLinkEntity();
        AppAuthorizedTypeLink model1 = AppAuthorizedTypeLink.builder().id(1L).build();
        AppAuthorizedTypeLink model2 = AppAuthorizedTypeLink.builder().id(2L).build();

        when(appAuthorizedTypeLinkJPARepository.findAllByAppAuthorizedId(99L))
                .thenReturn(List.of(entity1, entity2));
        when(appAuthorizedTypeLinkMapper.toModel(entity1)).thenReturn(model1);
        when(appAuthorizedTypeLinkMapper.toModel(entity2)).thenReturn(model2);

        List<AppAuthorizedTypeLink> result = adapter.findAllByAppAuthorizedId(99L);

        assertEquals(List.of(model1, model2), result);
    }

    @Test
    void findAllByAppAuthorizedId_emptyRepository_returnsEmptyList() {
        AppAuthorizedTypeLinkRepositoryAdapter adapter = buildAdapter();
        when(appAuthorizedTypeLinkJPARepository.findAllByAppAuthorizedId(5L)).thenReturn(List.of());

        List<AppAuthorizedTypeLink> result = adapter.findAllByAppAuthorizedId(5L);

        assertEquals(0, result.size());
    }
}
