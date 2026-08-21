package es.caib.invai.back.persistence.repository.application.development.technology;

import es.caib.invai.back.persistence.model.application.development.technology.AppTechnologyEntity;
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
 * Unit tests for {@link AppTechnologySpecification}, verifying that each {@link
 * AppTechnologyCriteria} field builds the expected JPA Criteria predicate against a fully mocked
 * {@link CriteriaBuilder}. The parent application identifier is a mandatory scoping constraint
 * always present in the generated predicate tree.
 *
 * <p>The free-text search predicate was recently fixed to filter on {@code
 * root.get("technology").get("name")} instead of the previously broken {@code
 * root.get("technology")} (which compared the like pattern against the whole association rather
 * than its name), so {@link #filterByCriteria_withTextSearch_addsLikeTechnologyNamePredicate()}
 * pins down the corrected, nested-path behavior.
 *
 * <p>Note: {@code cb.and(...)}/{@code cb.or(...)} are stubbed/verified with {@link
 * #anyPredicate()}, a helper that binds {@code ArgumentMatchers.any()}'s type parameter to the
 * varargs' <em>component</em> type ({@code Predicate}, not {@code Predicate[]}). Mockito then
 * applies the matcher per-element, matching an invocation with any number of {@code Predicate}
 * arguments. Using {@code any(Predicate[].class)} instead only reliably matches a zero-length
 * invocation in this project's Mockito version and desynchronizes the matcher stack for
 * subsequent stubs/verifications once a non-empty array is actually passed.
 *
 * <p>Note: {@link javax.persistence.criteria.CriteriaBuilder#equal} is overloaded as both
 * {@code equal(Expression<?>, Expression<?>)} and {@code equal(Expression<?>, Object)}. The
 * production code always calls the latter (comparing a path against a plain {@code Long}), so
 * every {@code any(), any()} pairing used to stub/verify it here is written as {@code any(),
 * any(Object.class)} to force the compiler to bind to that same overload - an unqualified
 * {@code any(), any()} instead resolves to the more-specific {@code Expression, Expression}
 * overload, which production code never calls, silently leaving {@code equal(...)} unstubbed.
 */
@ExtendWith(MockitoExtension.class)
class AppTechnologySpecificationTest {

    @Mock
    private Root<AppTechnologyEntity> root;

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
        lenient().when(cb.equal(any(), any(Object.class))).thenReturn(predicate);
        lenient().when(cb.isNull(any())).thenReturn(predicate);
        lenient().when(cb.isNotNull(any())).thenReturn(predicate);
        lenient().when(cb.or(anyPredicate())).thenReturn(predicate);
        lenient().when(cb.and(anyPredicate())).thenReturn(predicate);
        // Mockito's varargs matching binds by exact argument count when the vararg slot is filled
        // with component-typed any() matchers (see the class-level note above), so the 4-predicate
        // combination exercised by filterByCriteria_fullCriteria_combinesAllPredicates needs its own
        // explicit 4-arg stub - the single-arg one above only matches 1-argument invocations.
        lenient().when(cb.and(anyPredicate(), anyPredicate(), anyPredicate(), anyPredicate())).thenReturn(predicate);
    }

    private static Predicate anyPredicate() {
        return any();
    }

    @Test
    void filterByCriteria_nullCriteria_scopesByApplicationIdOnly() {
        Specification<AppTechnologyEntity> spec = AppTechnologySpecification.filterByCriteria(10L, null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(path, 10L);
        verify(cb).and(new Predicate[]{predicate});
    }

    @Test
    void filterByCriteria_activeStatus_addsIsNullDeletedAtPredicate() {
        AppTechnologyCriteria criteria = new AppTechnologyCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId());

        AppTechnologySpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).isNull(path);
        verify(cb, never()).isNotNull(any());
    }

    @Test
    void filterByCriteria_inactiveStatus_addsIsNotNullDeletedAtPredicate() {
        AppTechnologyCriteria criteria = new AppTechnologyCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId() + 1);

        AppTechnologySpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).isNotNull(path);
        verify(cb, never()).isNull(any());
    }

    @Test
    void filterByCriteria_withAppDevelopmentId_addsEqualPredicate() {
        AppTechnologyCriteria criteria = new AppTechnologyCriteria();
        criteria.setAppDevelopmentId(7L);

        AppTechnologySpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 7L);
    }

    @Test
    void filterByCriteria_withNumericSearch_addsEqualIdPredicate() {
        AppTechnologyCriteria criteria = new AppTechnologyCriteria();
        criteria.setSearch("123");

        AppTechnologySpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 123L);
        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void filterByCriteria_withTextSearch_addsLikeTechnologyNamePredicate() {
        AppTechnologyCriteria criteria = new AppTechnologyCriteria();
        criteria.setSearch("Java");

        AppTechnologySpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%java%"));
    }

    @Test
    void filterByCriteria_blankSearch_isIgnored() {
        AppTechnologyCriteria criteria = new AppTechnologyCriteria();
        criteria.setSearch("   ");

        AppTechnologySpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
        verify(cb, never()).or(anyPredicate());
    }

    @Test
    void filterByCriteria_fullCriteria_combinesAllPredicates() {
        AppTechnologyCriteria criteria = new AppTechnologyCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId());
        criteria.setAppDevelopmentId(7L);
        criteria.setSearch("Java");

        Predicate result = AppTechnologySpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb, times(2)).equal(any(), any(Object.class));
        verify(cb).isNull(path);
        verify(cb).or(anyPredicate());
    }
}
