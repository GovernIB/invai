package es.caib.invai.back.persistence.repository.application.security.risk;

import es.caib.invai.back.persistence.model.application.security.risk.AppSecurityRiskEntity;
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
 * Unit tests for {@link AppSecurityRiskSpecification}, verifying that each {@link AppSecurityRiskCriteria}
 * field builds the expected JPA Criteria predicate against a fully mocked {@link CriteriaBuilder},
 * always scoped by the mandatory parent security anchor identifier. Locks in the current free-text
 * search behavior which navigates the risk's field name and level name, both wrapped with
 * {@code cb.lower(...)}.
 */
@ExtendWith(MockitoExtension.class)
class AppSecurityRiskSpecificationTest {

    @Mock
    private Root<AppSecurityRiskEntity> root;

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
        Specification<AppSecurityRiskEntity> spec = AppSecurityRiskSpecification.filterByCriteria(10L, null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(path, 10L);
        verify(cb).and(predicate);
    }

    @Test
    void filterByCriteria_activeStatus_addsIsNullDeletedAtPredicate() {
        AppSecurityRiskCriteria criteria = new AppSecurityRiskCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId());

        AppSecurityRiskSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).isNull(path);
        verify(cb, never()).isNotNull(any());
    }

    @Test
    void filterByCriteria_inactiveStatus_addsIsNotNullDeletedAtPredicate() {
        AppSecurityRiskCriteria criteria = new AppSecurityRiskCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId() + 1);

        AppSecurityRiskSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).isNotNull(path);
        verify(cb, never()).isNull(any());
    }

    @Test
    void filterByCriteria_withAppSecurityId_addsSecondEqualPredicate() {
        AppSecurityRiskCriteria criteria = new AppSecurityRiskCriteria();
        criteria.setAppSecurityId(99L);

        AppSecurityRiskSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 10L);
        verify(cb).equal(path, 99L);
    }

    @Test
    void filterByCriteria_withLevelId_addsEqualPredicate() {
        AppSecurityRiskCriteria criteria = new AppSecurityRiskCriteria();
        criteria.setLevelId(55L);

        AppSecurityRiskSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 10L);
        verify(cb).equal(path, 55L);
    }

    @Test
    void filterByCriteria_withFieldId_addsEqualPredicate() {
        AppSecurityRiskCriteria criteria = new AppSecurityRiskCriteria();
        criteria.setFieldId(66L);

        AppSecurityRiskSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 10L);
        verify(cb).equal(path, 66L);
    }

    @Test
    void filterByCriteria_withSearch_orsFieldAndLevelNamePredicatesOnLoweredPaths() {
        AppSecurityRiskCriteria criteria = new AppSecurityRiskCriteria();
        criteria.setSearch("Prod");

        AppSecurityRiskSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        // 2 free-text predicates: field name and level name, each wrapped in cb.lower(...)
        // before the like comparison.
        verify(cb, times(2)).lower(path);
        verify(cb, times(2)).like(eq(path), eq("%prod%"));
        verify(cb).or(predicate, predicate);
    }

    @Test
    void filterByCriteria_blankSearch_isIgnored() {
        AppSecurityRiskCriteria criteria = new AppSecurityRiskCriteria();
        criteria.setSearch("   ");

        AppSecurityRiskSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }
}
