package es.caib.invai.back.persistence.repository.maintenance.security.identityProvider;

import es.caib.invai.back.persistence.model.maintenance.security.identityProvider.IdentityProviderEntity;
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
 * Unit tests for {@link IdentityProviderSpecification}, verifying that each {@link IdentityProviderCriteria}
 * field builds the expected JPA Criteria predicate against a fully mocked {@link CriteriaBuilder}.
 */
@ExtendWith(MockitoExtension.class)
class IdentityProviderSpecificationTest {

    @Mock
    private Root<IdentityProviderEntity> root;

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
        Specification<IdentityProviderEntity> spec = IdentityProviderSpecification.filterByCriteria(null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).and();
    }

    @Test
    void filterByCriteria_activeStatus_addsIsNullDeletedAtPredicate() {
        IdentityProviderCriteria criteria = new IdentityProviderCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId());

        IdentityProviderSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).isNull(path);
        verify(cb, never()).isNotNull(any());
    }

    @Test
    void filterByCriteria_inactiveStatus_addsIsNotNullDeletedAtPredicate() {
        IdentityProviderCriteria criteria = new IdentityProviderCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId() + 1);

        IdentityProviderSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).isNotNull(path);
        verify(cb, never()).isNull(any());
    }

    @Test
    void filterByCriteria_withName_addsLikePredicateOnLoweredName() {
        IdentityProviderCriteria criteria = new IdentityProviderCriteria();
        criteria.setName("Firmar");

        IdentityProviderSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%firmar%"));
    }

    @Test
    void filterByCriteria_blankName_isIgnored() {
        IdentityProviderCriteria criteria = new IdentityProviderCriteria();
        criteria.setName("   ");

        IdentityProviderSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void filterByCriteria_withSearch_addsLikePredicateOnLoweredName() {
        IdentityProviderCriteria criteria = new IdentityProviderCriteria();
        criteria.setSearch("clave");

        IdentityProviderSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, times(1)).like(eq(path), eq("%clave%"));
    }

    @Test
    void filterByCriteria_blankSearch_isIgnored() {
        IdentityProviderCriteria criteria = new IdentityProviderCriteria();
        criteria.setSearch("  ");

        IdentityProviderSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }
}
