package es.caib.invai.back.persistence.repository.maintenance.development.role;

import es.caib.invai.back.persistence.model.maintenance.development.role.RoleEntity;
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
 * Unit tests for {@link RoleSpecification}, verifying that each {@link RoleCriteria} field
 * builds the expected JPA Criteria predicate against a fully mocked {@link CriteriaBuilder}.
 */
@ExtendWith(MockitoExtension.class)
class RoleSpecificationTest {

    @Mock
    private Root<RoleEntity> root;

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
        lenient().when(cb.equal(any(), any())).thenReturn(predicate);
        lenient().when(cb.isNull(any())).thenReturn(predicate);
        lenient().when(cb.isNotNull(any())).thenReturn(predicate);
        lenient().when(cb.or(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(predicate);
    }

    @Test
    void filterByCriteria_nullCriteria_buildsEmptyConjunction() {
        Specification<RoleEntity> spec = RoleSpecification.filterByCriteria(null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).and(new Predicate[0]);
    }

    @Test
    void filterByCriteria_activeStatus_addsIsNullDeletedAtPredicate() {
        RoleCriteria criteria = new RoleCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId());

        RoleSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).isNull(path);
        verify(cb, never()).isNotNull(any());
    }

    @Test
    void filterByCriteria_inactiveStatus_addsIsNotNullDeletedAtPredicate() {
        RoleCriteria criteria = new RoleCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId() + 1);

        RoleSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).isNotNull(path);
        verify(cb, never()).isNull(any());
    }

    @Test
    void filterByCriteria_withName_addsLikePredicateOnLoweredName() {
        RoleCriteria criteria = new RoleCriteria();
        criteria.setName("Dev");

        RoleSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%dev%"));
    }

    @Test
    void filterByCriteria_blankName_isIgnored() {
        RoleCriteria criteria = new RoleCriteria();
        criteria.setName("   ");

        RoleSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void filterByCriteria_withNameEs_addsLikePredicate() {
        RoleCriteria criteria = new RoleCriteria();
        criteria.setNameEs("Responsable");

        RoleSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%responsable%"));
    }

    @Test
    void filterByCriteria_withSearch_orsNameAndNameEsPredicates() {
        RoleCriteria criteria = new RoleCriteria();
        criteria.setSearch("lead");

        RoleSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, org.mockito.Mockito.times(2)).like(eq(path), eq("%lead%"));
        verify(cb).or(predicate, predicate);
    }

    @Test
    void filterByCriteria_blankSearch_isIgnored() {
        RoleCriteria criteria = new RoleCriteria();
        criteria.setSearch("  ");

        RoleSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).or(any(Predicate.class), any(Predicate.class));
    }
}
