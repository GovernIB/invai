package es.caib.invai.back.persistence.repository.maintenance.responsible.person;

import es.caib.invai.back.persistence.model.maintenance.responsible.person.PersonEntity;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
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
 * Unit tests for {@link PersonSpecification}, verifying that each {@link PersonCriteria} field
 * builds the expected JPA Criteria predicate against a fully mocked {@link CriteriaBuilder}.
 */
@ExtendWith(MockitoExtension.class)
class PersonSpecificationTest {

    @Mock
    private Root<PersonEntity> root;

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
        lenient().when(cb.concat(ArgumentMatchers.any(), anyString())).thenReturn(path);
        lenient().when(cb.concat(ArgumentMatchers.<Expression<String>>any(), ArgumentMatchers.<Expression<String>>any())).thenReturn(path);
        lenient().when(cb.like(any(), anyString())).thenReturn(predicate);
        lenient().when(cb.equal(any(), any())).thenReturn(predicate);
        lenient().when(cb.notEqual(any(), any())).thenReturn(predicate);
        lenient().when(cb.isNull(any())).thenReturn(predicate);
        lenient().when(cb.isNotNull(any())).thenReturn(predicate);
        lenient().when(cb.isFalse(any())).thenReturn(predicate);
        lenient().when(cb.or(any(Predicate.class), any(Predicate.class), any(Predicate.class), any(Predicate.class)))
                .thenReturn(predicate);
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(predicate);
    }

    @Test
    void filterByCriteria_nullCriteria_stillExcludesPersonalCaib() {
        Specification<PersonEntity> spec = PersonSpecification.filterByCriteria(null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).isFalse(any());
        verify(cb).and(predicate);
    }

    @Test
    void filterByCriteria_anyCriteria_alwaysExcludesPersonalCaib() {
        PersonCriteria criteria = new PersonCriteria();
        criteria.setFirstName("Joan");

        PersonSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).isFalse(any());
    }

    @Test
    void filterByCriteria_activeStatus_addsIsNullDeletedAtPredicate() {
        PersonCriteria criteria = new PersonCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId());

        PersonSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).isNull(path);
        verify(cb, never()).isNotNull(any());
    }

    @Test
    void filterByCriteria_inactiveStatus_addsIsNotNullDeletedAtPredicate() {
        PersonCriteria criteria = new PersonCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId() + 1);

        PersonSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).isNotNull(path);
        verify(cb, never()).isNull(any());
    }

    @Test
    void filterByCriteria_withFirstName_addsLikePredicateOnLoweredFirstName() {
        PersonCriteria criteria = new PersonCriteria();
        criteria.setFirstName("Joan");

        PersonSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%joan%"));
    }

    @Test
    void filterByCriteria_blankFirstName_isIgnored() {
        PersonCriteria criteria = new PersonCriteria();
        criteria.setFirstName("   ");

        PersonSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void filterByCriteria_withLastName_addsLikePredicateOnLoweredLastName() {
        PersonCriteria criteria = new PersonCriteria();
        criteria.setLastName("Puig");

        PersonSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%puig%"));
    }

    @Test
    void filterByCriteria_blankLastName_isIgnored() {
        PersonCriteria criteria = new PersonCriteria();
        criteria.setLastName("   ");

        PersonSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void filterByCriteria_withEmail_addsLikePredicate() {
        PersonCriteria criteria = new PersonCriteria();
        criteria.setEmail("joan@example.com");

        PersonSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%joan@example.com%"));
    }

    @Test
    void filterByCriteria_withCompanyId_addsEqualPredicate() {
        PersonCriteria criteria = new PersonCriteria();
        criteria.setCompanyId(7L);

        PersonSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).equal(eq(path), eq(7L));
    }

    @Test
    void filterByCriteria_withExcludeId_addsNotEqualPredicate() {
        PersonCriteria criteria = new PersonCriteria();
        criteria.setExcludeId(9L);

        PersonSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).notEqual(eq(path), eq(9L));
    }

    @Test
    void filterByCriteria_withSearch_orsFirstNameLastNameEmailAndFullNamePredicates() {
        PersonCriteria criteria = new PersonCriteria();
        criteria.setSearch("lead");

        PersonSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, times(4)).like(eq(path), eq("%lead%"));
        verify(cb).concat(root.get("firstName"), " ");
        verify(cb).or(predicate, predicate, predicate, predicate);
    }

    @Test
    void filterByCriteria_withMultiWordSearch_collapsesWhitespaceForFullNameMatch() {
        PersonCriteria criteria = new PersonCriteria();
        criteria.setSearch("  Miguel Angel   Muñoz  ");

        PersonSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, times(4)).like(eq(path), eq("%miguel angel muñoz%"));
    }

    @Test
    void filterByCriteria_blankSearch_isIgnored() {
        PersonCriteria criteria = new PersonCriteria();
        criteria.setSearch("  ");

        PersonSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).or(any(Predicate.class), any(Predicate.class), any(Predicate.class), any(Predicate.class));
    }
}
