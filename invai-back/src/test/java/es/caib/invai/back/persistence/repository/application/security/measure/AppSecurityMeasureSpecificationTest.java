package es.caib.invai.back.persistence.repository.application.security.measure;

import es.caib.invai.back.persistence.model.application.security.measure.AppSecurityMeasureEntity;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link AppSecurityMeasureSpecification}, verifying that each
 * {@link AppSecurityMeasureCriteria} field builds the expected JPA Criteria predicate against a
 * fully mocked {@link CriteriaBuilder}, always scoped by the mandatory parent security anchor
 * identifier. Locks in the current free-text search behavior which navigates the measure type's
 * name and the ENS requirement's name, both wrapped with {@code cb.lower(...)}.
 */
@ExtendWith(MockitoExtension.class)
class AppSecurityMeasureSpecificationTest {

    @Mock
    private Root<AppSecurityMeasureEntity> root;

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
        lenient().when(cb.lower(any())).thenReturn(path);
        lenient().when(cb.like(any(), anyString())).thenReturn(predicate);
        // NOTE: cb.equal(any(), any()) would resolve at compile time to the
        // equal(Expression<?>, Expression<?>) overload, which never matches the
        // production code's equal(Expression<?>, Object) calls against a Long id.
        // any(Object.class) forces resolution to the correct overload.
        lenient().when(cb.equal(any(), any(Object.class))).thenReturn(predicate);
        lenient().when(cb.isNull(any())).thenReturn(predicate);
        lenient().when(cb.isNotNull(any())).thenReturn(predicate);
        lenient().when(cb.or(any(), any())).thenReturn(predicate);
        lenient().when(cb.and(any())).thenReturn(predicate);
    }

    @Test
    void filterByCriteria_nullCriteria_scopesByAppSecurityOnly() {
        Specification<AppSecurityMeasureEntity> spec = AppSecurityMeasureSpecification.filterByCriteria(10L, null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(path, 10L);
        verify(cb).and(predicate);
    }

    @Test
    void filterByCriteria_activeStatus_addsIsNullDeletedAtPredicate() {
        AppSecurityMeasureCriteria criteria = new AppSecurityMeasureCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId());

        AppSecurityMeasureSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).isNull(path);
        verify(cb, never()).isNotNull(any());
    }

    @Test
    void filterByCriteria_inactiveStatus_addsIsNotNullDeletedAtPredicate() {
        AppSecurityMeasureCriteria criteria = new AppSecurityMeasureCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId() + 1);

        AppSecurityMeasureSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).isNotNull(path);
        verify(cb, never()).isNull(any());
    }

    @Test
    void filterByCriteria_withAppSecurityId_addsSecondEqualPredicate() {
        AppSecurityMeasureCriteria criteria = new AppSecurityMeasureCriteria();
        criteria.setAppSecurityId(99L);

        AppSecurityMeasureSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 10L);
        verify(cb).equal(path, 99L);
    }

    @Test
    void filterByCriteria_withTypeId_addsEqualPredicate() {
        AppSecurityMeasureCriteria criteria = new AppSecurityMeasureCriteria();
        criteria.setTypeId(55L);

        AppSecurityMeasureSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 10L);
        verify(cb).equal(path, 55L);
    }

    @Test
    void filterByCriteria_withEnsRequirementId_addsEqualPredicate() {
        AppSecurityMeasureCriteria criteria = new AppSecurityMeasureCriteria();
        criteria.setEnsRequirementId(66L);

        AppSecurityMeasureSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 10L);
        verify(cb).equal(path, 66L);
    }

    @Test
    void filterByCriteria_withSearch_orsTypeAndEnsRequirementNamePredicatesOnLoweredPaths() {
        AppSecurityMeasureCriteria criteria = new AppSecurityMeasureCriteria();
        criteria.setSearch("Encrypt");

        AppSecurityMeasureSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        // 2 free-text predicates: measure type name and ENS requirement name,
        // each wrapped in cb.lower(...) before the like comparison.
        verify(cb, times(2)).lower(path);
        verify(cb, times(2)).like(eq(path), eq("%encrypt%"));
        verify(cb).or(predicate, predicate);
    }

    @Test
    void filterByCriteria_blankSearch_isIgnored() {
        AppSecurityMeasureCriteria criteria = new AppSecurityMeasureCriteria();
        criteria.setSearch("   ");

        AppSecurityMeasureSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }
}
