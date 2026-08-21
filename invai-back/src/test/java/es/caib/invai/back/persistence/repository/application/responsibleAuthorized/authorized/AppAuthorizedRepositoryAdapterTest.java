package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.authorized.AppAuthorizedEntity;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.authorized.AppAuthorizedMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the person-keyed lookup method added to {@link AppAuthorizedRepositoryAdapter}
 * in support of the create-time swap-out business rule, with the
 * {@link AppAuthorizedJPARepository} and {@link AppAuthorizedMapper} collaborators mocked.
 */
@ExtendWith(MockitoExtension.class)
class AppAuthorizedRepositoryAdapterTest {

    @Mock
    private AppAuthorizedJPARepository appAuthorizedJPARepository;

    @Mock
    private AppAuthorizedMapper appAuthorizedMapper;

    private AppAuthorizedRepositoryAdapter buildAdapter() {
        AppAuthorizedRepositoryAdapter adapter = new AppAuthorizedRepositoryAdapter();
        ReflectionTestUtils.setField(adapter, "appAuthorizedJPARepository", appAuthorizedJPARepository);
        ReflectionTestUtils.setField(adapter, "appAuthorizedMapper", appAuthorizedMapper);
        return adapter;
    }

    @Test
    void findActiveByAppResponsibleAuthorizedAndPerson_found_returnsMappedModel() {
        AppAuthorizedRepositoryAdapter adapter = buildAdapter();
        AppAuthorizedEntity entity = new AppAuthorizedEntity();
        entity.setId(1L);
        AppAuthorized model = new AppAuthorized();
        when(appAuthorizedJPARepository.findByAppResponsibleAuthorizedIdAndPersonIdAndDeletedAtIsNull(10L, 7L))
                .thenReturn(Optional.of(entity));
        when(appAuthorizedMapper.toModel(entity)).thenReturn(model);

        assertSame(model, adapter.findActiveByAppResponsibleAuthorizedAndPerson(10L, 7L));
    }

    @Test
    void findActiveByAppResponsibleAuthorizedAndPerson_notFound_returnsNull() {
        AppAuthorizedRepositoryAdapter adapter = buildAdapter();
        when(appAuthorizedJPARepository.findByAppResponsibleAuthorizedIdAndPersonIdAndDeletedAtIsNull(10L, 7L))
                .thenReturn(Optional.empty());

        assertNull(adapter.findActiveByAppResponsibleAuthorizedAndPerson(10L, 7L));
    }
}
