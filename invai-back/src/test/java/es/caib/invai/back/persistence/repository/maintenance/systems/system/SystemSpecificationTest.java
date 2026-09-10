package es.caib.invai.back.persistence.repository.maintenance.systems.system;

import es.caib.invai.back.persistence.model.maintenance.systems.system.SystemEntity;
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
 * Unit tests for {@link SystemSpecification}, verifying that each {@link SystemCriteria} field
 * builds the expected JPA Criteria predicate against a fully mocked {@link CriteriaBuilder}.
 */
@ExtendWith(MockitoExtension.class)
class SystemSpecificationTest {

    @Mock
    private Root<SystemEntity> root;

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
        lenient().when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(predicate);
    }

    @Test
    void filterByCriteria_nullCriteria_buildsEmptyConjunction() {
        Specification<SystemEntity> spec = SystemSpecification.filterByCriteria(null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).and();
    }

    @Test
    void filterByCriteria_activeStatus_addsIsNullDeletedAtPredicate() {
        SystemCriteria criteria = new SystemCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId());

        SystemSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).isNull(path);
        verify(cb, never()).isNotNull(any());
    }

    @Test
    void filterByCriteria_inactiveStatus_addsIsNotNullDeletedAtPredicate() {
        SystemCriteria criteria = new SystemCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId() + 1);

        SystemSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).isNotNull(path);
        verify(cb, never()).isNull(any());
    }

    @Test
    void filterByCriteria_withServerId_addsEqualPredicate() {
        SystemCriteria criteria = new SystemCriteria();
        criteria.setServerId(3L);

        SystemSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).equal(eq(path), eq(3L));
    }

    @Test
    void filterByCriteria_withInstance_addsLikePredicateOnLoweredInstance() {
        SystemCriteria criteria = new SystemCriteria();
        criteria.setInstance("Node1");

        SystemSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%node1%"));
    }

    @Test
    void filterByCriteria_blankInstance_isIgnored() {
        SystemCriteria criteria = new SystemCriteria();
        criteria.setInstance("   ");

        SystemSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void filterByCriteria_withVersion_addsLikePredicateOnLoweredVersion() {
        SystemCriteria criteria = new SystemCriteria();
        criteria.setVersion("2.1");

        SystemSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%2.1%"));
    }

    @Test
    void filterByCriteria_blankVersion_isIgnored() {
        SystemCriteria criteria = new SystemCriteria();
        criteria.setVersion("  ");

        SystemSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void filterByCriteria_withSearch_orsServerInstanceAndVersionPredicates() {
        SystemCriteria criteria = new SystemCriteria();
        criteria.setSearch("lead");

        SystemSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, times(3)).like(eq(path), eq("%lead%"));
        verify(cb).or(predicate, predicate, predicate);
    }

    @Test
    void filterByCriteria_blankSearch_isIgnored() {
        SystemCriteria criteria = new SystemCriteria();
        criteria.setSearch("  ");

        SystemSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).or(any(Predicate[].class));
        verify(cb, never()).like(any(), anyString());
    }
}
