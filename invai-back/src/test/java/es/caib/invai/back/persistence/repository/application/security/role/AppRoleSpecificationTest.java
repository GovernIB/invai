package es.caib.invai.back.persistence.repository.application.security.role;

import es.caib.invai.back.persistence.model.application.security.role.AppRoleEntity;
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
 * Unit tests for {@link AppRoleSpecification}, verifying that each {@link AppRoleCriteria}
 * field builds the expected JPA Criteria predicate against a fully mocked {@link CriteriaBuilder},
 * always scoped by the mandatory parent security anchor identifier. Locks in the
 * current free-text search behavior which navigates the security role's name and description,
 * all wrapped with {@code cb.lower(...)}.
 */
@ExtendWith(MockitoExtension.class)
class AppRoleSpecificationTest {

    @Mock
    private Root<AppRoleEntity> root;

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
        lenient().when(cb.or(any())).thenReturn(predicate);
        lenient().when(cb.and(any())).thenReturn(predicate);
    }

    @Test
    void filterByCriteria_nullCriteria_scopesByAppSecurityOnly() {
        Specification<AppRoleEntity> spec = AppRoleSpecification.filterByCriteria(10L, null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(path, 10L);
        verify(cb).and(predicate);
    }

    @Test
    void filterByCriteria_activeStatus_addsIsNullDeletedAtPredicate() {
        AppRoleCriteria criteria = new AppRoleCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId());

        AppRoleSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).isNull(path);
        verify(cb, never()).isNotNull(any());
    }

    @Test
    void filterByCriteria_inactiveStatus_addsIsNotNullDeletedAtPredicate() {
        AppRoleCriteria criteria = new AppRoleCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId() + 1);

        AppRoleSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).isNotNull(path);
        verify(cb, never()).isNull(any());
    }

    @Test
    void filterByCriteria_withAppSecurityId_addsSecondEqualPredicate() {
        AppRoleCriteria criteria = new AppRoleCriteria();
        criteria.setAppSecurityId(99L);

        AppRoleSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 10L);
        verify(cb).equal(path, 99L);
    }

    @Test
    void filterByCriteria_withSecurityRoleId_addsEqualPredicate() {
        AppRoleCriteria criteria = new AppRoleCriteria();
        criteria.setSecurityRoleId(55L);

        AppRoleSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 10L);
        verify(cb).equal(path, 55L);
    }

    @Test
    void filterByCriteria_withSecurityRoleName_addsLikePredicateOnLoweredSecurityRoleName() {
        AppRoleCriteria criteria = new AppRoleCriteria();
        criteria.setSecurityRoleName("Admin");

        AppRoleSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).lower(path);
        verify(cb).like(path, "%admin%");
    }

    @Test
    void filterByCriteria_blankSecurityRoleName_isIgnored() {
        AppRoleCriteria criteria = new AppRoleCriteria();
        criteria.setSecurityRoleName("   ");

        AppRoleSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void filterByCriteria_withSystem_addsLikePredicateOnLoweredSystem() {
        AppRoleCriteria criteria = new AppRoleCriteria();
        criteria.setSystem("OWS");

        AppRoleSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb).lower(path);
        verify(cb).like(path, "%ows%");
    }

    @Test
    void filterByCriteria_blankSystem_isIgnored() {
        AppRoleCriteria criteria = new AppRoleCriteria();
        criteria.setSystem("   ");

        AppRoleSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void filterByCriteria_withSearch_orsSecurityRoleNameAndDescriptionPredicatesOnLoweredPaths() {
        AppRoleCriteria criteria = new AppRoleCriteria();
        criteria.setSearch("Admin");

        AppRoleSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        // 2 free-text predicates: securityRole.name and securityRole.description,
        // each wrapped in cb.lower(...) before the like comparison.
        verify(cb, times(2)).lower(path);
        verify(cb, times(2)).like(eq(path), eq("%admin%"));
        // Production code passes a 2-element Predicate[] into the varargs or(Predicate...)
        // overload. Calling verify(cb).or(predicate, predicate) instead resolves at compile
        // time to the distinct 2-arg or(Predicate, Predicate) overload, which is never invoked.
        verify(cb).or(new Predicate[]{predicate, predicate});
    }

    @Test
    void filterByCriteria_blankSearch_isIgnored() {
        AppRoleCriteria criteria = new AppRoleCriteria();
        criteria.setSearch("   ");

        AppRoleSpecification.filterByCriteria(10L, criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }
}
