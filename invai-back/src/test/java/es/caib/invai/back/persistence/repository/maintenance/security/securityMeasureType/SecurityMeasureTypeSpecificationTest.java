package es.caib.invai.back.persistence.repository.maintenance.security.securityMeasureType;

import es.caib.invai.back.persistence.model.maintenance.security.securityMeasureType.SecurityMeasureTypeEntity;
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
 * Unit tests for {@link SecurityMeasureTypeSpecification}, verifying that each {@link SecurityMeasureTypeCriteria}
 * field builds the expected JPA Criteria predicate against a fully mocked {@link CriteriaBuilder}.
 */
@ExtendWith(MockitoExtension.class)
class SecurityMeasureTypeSpecificationTest {

    @Mock
    private Root<SecurityMeasureTypeEntity> root;

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
        lenient().when(cb.isNull(any())).thenReturn(predicate);
        lenient().when(cb.isNotNull(any())).thenReturn(predicate);
        lenient().when(cb.or(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(predicate);
    }

    @Test
    void filterByCriteria_nullCriteria_buildsEmptyConjunction() {
        Specification<SecurityMeasureTypeEntity> spec = SecurityMeasureTypeSpecification.filterByCriteria(null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).and();
    }

    @Test
    void filterByCriteria_activeStatus_addsIsNullDeletedAtPredicate() {
        SecurityMeasureTypeCriteria criteria = new SecurityMeasureTypeCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId());

        SecurityMeasureTypeSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).isNull(path);
        verify(cb, never()).isNotNull(any());
    }

    @Test
    void filterByCriteria_inactiveStatus_addsIsNotNullDeletedAtPredicate() {
        SecurityMeasureTypeCriteria criteria = new SecurityMeasureTypeCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId() + 1);

        SecurityMeasureTypeSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).isNotNull(path);
        verify(cb, never()).isNull(any());
    }

    @Test
    void filterByCriteria_withName_addsLikePredicateOnLoweredName() {
        SecurityMeasureTypeCriteria criteria = new SecurityMeasureTypeCriteria();
        criteria.setName("Organitzativa");

        SecurityMeasureTypeSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%organitzativa%"));
    }

    @Test
    void filterByCriteria_blankName_isIgnored() {
        SecurityMeasureTypeCriteria criteria = new SecurityMeasureTypeCriteria();
        criteria.setName("   ");

        SecurityMeasureTypeSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void filterByCriteria_withNameEs_addsLikePredicateOnLoweredNameEs() {
        SecurityMeasureTypeCriteria criteria = new SecurityMeasureTypeCriteria();
        criteria.setNameEs("Organizativa");

        SecurityMeasureTypeSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%organizativa%"));
    }

    @Test
    void filterByCriteria_blankNameEs_isIgnored() {
        SecurityMeasureTypeCriteria criteria = new SecurityMeasureTypeCriteria();
        criteria.setNameEs("   ");

        SecurityMeasureTypeSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void filterByCriteria_withSearch_addsOrLikePredicateOnLoweredNameAndNameEs() {
        SecurityMeasureTypeCriteria criteria = new SecurityMeasureTypeCriteria();
        criteria.setSearch("tecnica");

        SecurityMeasureTypeSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, times(2)).like(eq(path), eq("%tecnica%"));
        verify(cb).or(predicate, predicate);
    }

    @Test
    void filterByCriteria_blankSearch_isIgnored() {
        SecurityMeasureTypeCriteria criteria = new SecurityMeasureTypeCriteria();
        criteria.setSearch("  ");

        SecurityMeasureTypeSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
        verify(cb, never()).or(any(Predicate.class), any(Predicate.class));
    }
}
