package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.responsible.AppResponsibleEntity;
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
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link AppResponsibleSpecification}, verifying that each {@link AppResponsibleCriteria}
 * field builds the expected JPA Criteria predicate against a fully mocked {@link CriteriaBuilder}.
 * The parent {@code appResponsibleAuthorizedId} is a mandatory scoping constraint always present in
 * the generated predicate tree, so every combination is exercised on top of that first predicate.
 *
 * <p>Note: {@code cb.and(...)} is stubbed/verified per exact argument count using {@link #anyPredicate()},
 * a helper that binds {@code ArgumentMatchers.any()}'s type parameter to the varargs' <em>component</em>
 * type ({@code Predicate}, not {@code Predicate[]}) - mirroring {@code AppProviderSpecificationTest}. Using
 * {@code any(Predicate[].class)} instead only reliably matches a zero-length invocation in this project's
 * Mockito version.
 *
 * <p>Note: {@link CriteriaBuilder#equal} is overloaded as both
 * {@code equal(Expression<?>, Expression<?>)} and {@code equal(Expression<?>, Object)}. Production code
 * always calls the latter (comparing a path against a plain {@code Long}), so every stub/verification is
 * written as {@code any(), any(Object.class)} to bind to that same overload.
 */
@ExtendWith(MockitoExtension.class)
class AppResponsibleSpecificationTest {

    @Mock
    private Root<AppResponsibleEntity> root;

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
        lenient().when(cb.or(predicate, predicate, predicate)).thenReturn(predicate);
        lenient().when(cb.and(anyPredicate())).thenReturn(predicate);
        lenient().when(cb.and(anyPredicate(), anyPredicate())).thenReturn(predicate);
        lenient().when(cb.and(anyPredicate(), anyPredicate(), anyPredicate(), anyPredicate(), anyPredicate())).thenReturn(predicate);
    }

    private static Predicate anyPredicate() {
        return any();
    }

    @Test
    void filterByCriteria_nullCriteria_scopesByAppResponsibleAuthorizedIdOnly() {
        Specification<AppResponsibleEntity> spec = AppResponsibleSpecification.filterByCriteria(10L, null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(path, 10L);
        verify(cb).and(predicate);
    }

    @Test
    void filterByCriteria_activeStatus_addsIsNullDeletedAtPredicate() {
        AppResponsibleCriteria criteria = AppResponsibleCriteria.builder().statusId(StatusEnum.ACTIVE.getId()).build();

        AppResponsibleSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).isNull(path);
        verify(cb, never()).isNotNull(any());
    }

    @Test
    void filterByCriteria_inactiveStatus_addsIsNotNullDeletedAtPredicate() {
        AppResponsibleCriteria criteria = AppResponsibleCriteria.builder().statusId(StatusEnum.ACTIVE.getId() + 1).build();

        AppResponsibleSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).isNotNull(path);
        verify(cb, never()).isNull(any());
    }

    @Test
    void filterByCriteria_withPersonId_addsEqualPredicate() {
        AppResponsibleCriteria criteria = AppResponsibleCriteria.builder().personId(7L).build();

        AppResponsibleSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb, org.mockito.Mockito.times(2)).equal(any(), any(Object.class));
    }

    @Test
    void filterByCriteria_withResponsibleTypeId_addsEqualPredicate() {
        AppResponsibleCriteria criteria = AppResponsibleCriteria.builder().responsibleTypeId(8L).build();

        AppResponsibleSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb, org.mockito.Mockito.times(2)).equal(any(), any(Object.class));
    }

    @Test
    void filterByCriteria_withSearch_addsOrLikePredicateOnLoweredPersonNameAndEmail() {
        AppResponsibleCriteria criteria = AppResponsibleCriteria.builder().search("Fuster").build();

        AppResponsibleSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb, org.mockito.Mockito.times(3)).like(eq(path), eq("%fuster%"));
        verify(cb).or(predicate, predicate, predicate);
    }

    @Test
    void filterByCriteria_blankSearch_isIgnored() {
        AppResponsibleCriteria criteria = AppResponsibleCriteria.builder().search("   ").build();

        AppResponsibleSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
        verify(cb, never()).or(predicate, predicate, predicate);
        verify(cb).and(predicate);
    }

    @Test
    void filterByCriteria_fullCriteria_combinesAllPredicates() {
        AppResponsibleCriteria criteria = AppResponsibleCriteria.builder()
                .statusId(StatusEnum.ACTIVE.getId())
                .personId(7L)
                .responsibleTypeId(8L)
                .search("Fuster")
                .build();

        Predicate result = AppResponsibleSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).isNull(path);
        verify(cb).or(predicate, predicate, predicate);
        verify(cb).and(predicate, predicate, predicate, predicate, predicate);
    }
}
