package es.caib.invai.back.persistence.repository.application.development.provider;

import es.caib.invai.back.persistence.model.application.development.provider.AppProviderEntity;
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
 * Unit tests for {@link AppProviderSpecification}, verifying that each {@link AppProviderCriteria}
 * field builds the expected JPA Criteria predicate against a fully mocked {@link CriteriaBuilder}.
 * Unlike the Role maintenance module, the parent application identifier is a mandatory scoping
 * constraint always present in the generated predicate tree.
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
class AppProviderSpecificationTest {

    @Mock
    private Root<AppProviderEntity> root;

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
        // Explicitly typed as Object (not the bare, doubly-generic any()) so this stub binds to
        // the CriteriaBuilder#equal(Expression<?>, Object) overload - the one the production code
        // actually calls (always comparing a path against a Long id/search value). Left ambiguous,
        // any()/any() resolves at compile time to the *other*, more-specific equal(Expression<?>,
        // Expression<?>) overload, which production code never calls, so the stub would silently
        // never apply and every cb.equal(...) call would return null.
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
        Specification<AppProviderEntity> spec = AppProviderSpecification.filterByCriteria(10L, null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(path, 10L);
        verify(cb).and(new Predicate[]{predicate});
    }

    @Test
    void filterByCriteria_activeStatus_addsIsNullDeletedAtPredicate() {
        AppProviderCriteria criteria = new AppProviderCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId());

        AppProviderSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).isNull(path);
        verify(cb, never()).isNotNull(any());
    }

    @Test
    void filterByCriteria_inactiveStatus_addsIsNotNullDeletedAtPredicate() {
        AppProviderCriteria criteria = new AppProviderCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId() + 1);

        AppProviderSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).isNotNull(path);
        verify(cb, never()).isNull(any());
    }

    @Test
    void filterByCriteria_withAppDevelopmentId_addsEqualPredicate() {
        AppProviderCriteria criteria = new AppProviderCriteria();
        criteria.setAppDevelopmentId(7L);

        AppProviderSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 7L);
    }

    @Test
    void filterByCriteria_withNumericSearch_addsEqualIdPredicate() {
        AppProviderCriteria criteria = new AppProviderCriteria();
        criteria.setSearch("123");

        AppProviderSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 123L);
        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void filterByCriteria_withTextSearch_addsLikeCompanyNamePredicate() {
        AppProviderCriteria criteria = new AppProviderCriteria();
        criteria.setSearch("Acme");

        AppProviderSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%acme%"));
    }

    @Test
    void filterByCriteria_blankSearch_isIgnored() {
        AppProviderCriteria criteria = new AppProviderCriteria();
        criteria.setSearch("   ");

        AppProviderSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
        verify(cb, never()).or(anyPredicate());
    }

    @Test
    void filterByCriteria_fullCriteria_combinesAllPredicates() {
        AppProviderCriteria criteria = new AppProviderCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId());
        criteria.setAppDevelopmentId(7L);
        criteria.setSearch("Acme");

        Predicate result = AppProviderSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb, times(2)).equal(any(), any(Object.class));
        verify(cb).isNull(path);
        verify(cb).or(anyPredicate());
    }
}
