package es.caib.invai.back.persistence.repository.maintenance.systems.database;

import es.caib.invai.back.persistence.model.maintenance.systems.database.DatabaseEntity;
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
 * Unit tests for {@link DatabaseSpecification}, verifying that each {@link DatabaseCriteria} field
 * builds the expected JPA Criteria predicate against a fully mocked {@link CriteriaBuilder}.
 */
@ExtendWith(MockitoExtension.class)
class DatabaseSpecificationTest {

    @Mock
    private Root<DatabaseEntity> root;

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
        lenient().when(cb.or(any(Predicate.class), any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(predicate);
    }

    @Test
    void filterByCriteria_nullCriteria_buildsEmptyConjunction() {
        Specification<DatabaseEntity> spec = DatabaseSpecification.filterByCriteria(null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).and();
    }

    @Test
    void filterByCriteria_activeStatus_addsIsNullDeletedAtPredicate() {
        DatabaseCriteria criteria = new DatabaseCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId());

        DatabaseSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).isNull(path);
        verify(cb, never()).isNotNull(any());
    }

    @Test
    void filterByCriteria_inactiveStatus_addsIsNotNullDeletedAtPredicate() {
        DatabaseCriteria criteria = new DatabaseCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId() + 1);

        DatabaseSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).isNotNull(path);
        verify(cb, never()).isNull(any());
    }

    @Test
    void filterByCriteria_withServerId_addsEqualPredicate() {
        DatabaseCriteria criteria = new DatabaseCriteria();
        criteria.setServerId(7L);

        DatabaseSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 7L);
    }

    @Test
    void filterByCriteria_withService_addsLikePredicateOnLoweredService() {
        DatabaseCriteria criteria = new DatabaseCriteria();
        criteria.setService("invai");

        DatabaseSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%invai%"));
    }

    @Test
    void filterByCriteria_blankService_isIgnored() {
        DatabaseCriteria criteria = new DatabaseCriteria();
        criteria.setService("   ");

        DatabaseSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void filterByCriteria_withDatabaseTypeId_addsEqualPredicate() {
        DatabaseCriteria criteria = new DatabaseCriteria();
        criteria.setDatabaseTypeId(9L);

        DatabaseSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 9L);
    }

    @Test
    void filterByCriteria_withSearch_orsServerServiceAndDescriptionPredicates() {
        DatabaseCriteria criteria = new DatabaseCriteria();
        criteria.setSearch("prod");

        DatabaseSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, times(3)).like(eq(path), eq("%prod%"));
        verify(cb).or(predicate, predicate, predicate);
    }

    @Test
    void filterByCriteria_blankSearch_isIgnored() {
        DatabaseCriteria criteria = new DatabaseCriteria();
        criteria.setSearch("   ");

        DatabaseSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).or(any(Predicate.class), any(Predicate.class), any(Predicate.class));
    }
}
