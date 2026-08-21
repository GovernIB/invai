package es.caib.invai.back.persistence.repository.maintenance.responsible.company;

import es.caib.invai.back.persistence.model.maintenance.responsible.company.CompanyEntity;
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
 * Unit tests for {@link CompanySpecification}, verifying that each {@link CompanyCriteria} field
 * builds the expected JPA Criteria predicate against a fully mocked {@link CriteriaBuilder}.
 */
@ExtendWith(MockitoExtension.class)
class CompanySpecificationTest {

    @Mock
    private Root<CompanyEntity> root;

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
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(predicate);
    }

    @Test
    void filterByCriteria_nullCriteria_buildsEmptyConjunction() {
        Specification<CompanyEntity> spec = CompanySpecification.filterByCriteria(null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).and(new Predicate[0]);
    }

    @Test
    void filterByCriteria_activeStatus_addsIsNullDeletedAtPredicate() {
        CompanyCriteria criteria = new CompanyCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId());

        CompanySpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).isNull(path);
        verify(cb, never()).isNotNull(any());
    }

    @Test
    void filterByCriteria_inactiveStatus_addsIsNotNullDeletedAtPredicate() {
        CompanyCriteria criteria = new CompanyCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId() + 1);

        CompanySpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).isNotNull(path);
        verify(cb, never()).isNull(any());
    }

    @Test
    void filterByCriteria_withName_addsLikePredicateOnLoweredName() {
        CompanyCriteria criteria = new CompanyCriteria();
        criteria.setName("Plexus");

        CompanySpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%plexus%"));
    }

    @Test
    void filterByCriteria_blankName_isIgnored() {
        CompanyCriteria criteria = new CompanyCriteria();
        criteria.setName("   ");

        CompanySpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void filterByCriteria_withSearch_addsLikePredicateOnLoweredName() {
        CompanyCriteria criteria = new CompanyCriteria();
        criteria.setSearch("tech");

        CompanySpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%tech%"));
    }

    @Test
    void filterByCriteria_blankSearch_isIgnored() {
        CompanyCriteria criteria = new CompanyCriteria();
        criteria.setSearch("  ");

        CompanySpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }
}
