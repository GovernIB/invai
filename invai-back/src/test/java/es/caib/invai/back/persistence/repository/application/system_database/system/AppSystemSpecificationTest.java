package es.caib.invai.back.persistence.repository.application.system_database.system;

import es.caib.invai.back.persistence.model.application.system_database.system.AppSystemEntity;
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
 * Unit tests for {@link AppSystemSpecification}, verifying that each {@link AppSystemCriteria}
 * field builds the expected JPA Criteria predicate against a fully mocked {@link CriteriaBuilder},
 * always scoped by the mandatory parent information system database identifier.
 */
@ExtendWith(MockitoExtension.class)
class AppSystemSpecificationTest {

    @Mock
    private Root<AppSystemEntity> root;

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
        lenient().when(cb.or(any(Predicate.class), any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
        lenient().when(cb.and((Predicate[]) any())).thenReturn(predicate);
    }

    @Test
    void filterByCriteria_nullCriteria_scopesByInformationSystemDbOnly() {
        Specification<AppSystemEntity> spec = AppSystemSpecification.filterByCriteria(10L, null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(eq(path), eq(10L));
        verify(cb).and(new Predicate[]{predicate});
    }

    @Test
    void filterByCriteria_activeStatus_addsIsNullDeletedAtPredicate() {
        AppSystemCriteria criteria = new AppSystemCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId());

        AppSystemSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).isNull(path);
        verify(cb, never()).isNotNull(any());
    }

    @Test
    void filterByCriteria_inactiveStatus_addsIsNotNullDeletedAtPredicate() {
        AppSystemCriteria criteria = new AppSystemCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId() + 1);

        AppSystemSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).isNotNull(path);
        verify(cb, never()).isNull(any());
    }

    @Test
    void filterByCriteria_withInformationSystemDbId_addsSecondEqualPredicate() {
        AppSystemCriteria criteria = new AppSystemCriteria();
        criteria.setInformationSystemDbId(99L);

        AppSystemSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).equal(eq(path), eq(10L));
        verify(cb).equal(eq(path), eq(99L));
    }

    @Test
    void filterByCriteria_withSystemId_addsEqualPredicate() {
        AppSystemCriteria criteria = new AppSystemCriteria();
        criteria.setSystemId(55L);

        AppSystemSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).equal(eq(path), eq(10L));
        verify(cb).equal(eq(path), eq(55L));
    }

    @Test
    void filterByCriteria_withSearch_orsServerInstanceAndVersionPredicates() {
        AppSystemCriteria criteria = new AppSystemCriteria();
        criteria.setSearch("prod");

        AppSystemSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb, times(3)).like(eq(path), eq("%prod%"));
        verify(cb).or(predicate, predicate, predicate);
    }

    @Test
    void filterByCriteria_blankSearch_isIgnored() {
        AppSystemCriteria criteria = new AppSystemCriteria();
        criteria.setSearch("   ");

        AppSystemSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }
}
