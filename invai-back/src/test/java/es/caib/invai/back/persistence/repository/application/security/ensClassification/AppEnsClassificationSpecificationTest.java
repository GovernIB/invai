package es.caib.invai.back.persistence.repository.application.security.ensClassification;

import es.caib.invai.back.persistence.model.application.security.ensClassification.AppEnsClassificationEntity;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link AppEnsClassificationSpecification}, verifying that each
 * {@link AppEnsClassificationCriteria} field builds the expected JPA Criteria predicate against
 * a fully mocked {@link CriteriaBuilder}, always scoped by the mandatory parent security anchor
 * identifier. This entity has no free-text search field, unlike most sibling specifications.
 */
@ExtendWith(MockitoExtension.class)
class AppEnsClassificationSpecificationTest {

    @Mock
    private Root<AppEnsClassificationEntity> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<String> path;

    @Mock
    private Predicate predicate;

    @BeforeEach
    void setUp() {
        lenient().when(root.<String>get(anyString())).thenReturn(path);
        lenient().when(path.<String>get(anyString())).thenReturn(path);
        // NOTE: cb.equal(any(), any()) would resolve at compile time to the
        // equal(Expression<?>, Expression<?>) overload, which never matches the
        // production code's equal(Expression<?>, Object) calls against a Long id.
        // any(Object.class) forces resolution to the correct overload.
        lenient().when(cb.equal(any(), any(Object.class))).thenReturn(predicate);
        lenient().when(cb.isNull(any())).thenReturn(predicate);
        lenient().when(cb.isNotNull(any())).thenReturn(predicate);
        lenient().when(cb.and(any())).thenReturn(predicate);
    }

    @Test
    void filterByCriteria_nullCriteria_scopesByAppSecurityOnly() {
        Specification<AppEnsClassificationEntity> spec = AppEnsClassificationSpecification.filterByCriteria(10L, null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(path, 10L);
        verify(cb).and(predicate);
    }

    @Test
    void filterByCriteria_activeStatus_addsIsNullDeletedAtPredicate() {
        AppEnsClassificationCriteria criteria = new AppEnsClassificationCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId());

        AppEnsClassificationSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).isNull(path);
        verify(cb, never()).isNotNull(any());
    }

    @Test
    void filterByCriteria_inactiveStatus_addsIsNotNullDeletedAtPredicate() {
        AppEnsClassificationCriteria criteria = new AppEnsClassificationCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId() + 1);

        AppEnsClassificationSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).isNotNull(path);
        verify(cb, never()).isNull(any());
    }

    @Test
    void filterByCriteria_withAppSecurityId_addsSecondEqualPredicate() {
        AppEnsClassificationCriteria criteria = new AppEnsClassificationCriteria();
        criteria.setAppSecurityId(99L);

        AppEnsClassificationSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 10L);
        verify(cb).equal(path, 99L);
    }

    @Test
    void filterByCriteria_withIdentityProviderId_addsEqualPredicate() {
        AppEnsClassificationCriteria criteria = new AppEnsClassificationCriteria();
        criteria.setIdentityProviderId(55L);

        AppEnsClassificationSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 10L);
        verify(cb).equal(path, 55L);
    }

    @Test
    void filterByCriteria_withEnsSubjectId_addsEqualPredicate() {
        AppEnsClassificationCriteria criteria = new AppEnsClassificationCriteria();
        criteria.setEnsSubjectId(56L);

        AppEnsClassificationSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 10L);
        verify(cb).equal(path, 56L);
    }

    @Test
    void filterByCriteria_withPersonalDataProcessingId_addsEqualPredicate() {
        AppEnsClassificationCriteria criteria = new AppEnsClassificationCriteria();
        criteria.setPersonalDataProcessingId(57L);

        AppEnsClassificationSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 10L);
        verify(cb).equal(path, 57L);
    }
}
