package es.caib.invai.back.persistence.repository.maintenance.general.commission;

import es.caib.invai.back.persistence.model.maintenance.general.commission.CommissionEntity;
import es.caib.invai.back.service.model.maintenance.general.commission.CommissionType;
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
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link CommissionSpecification}, verifying that each {@link CommissionCriteria} field
 * builds the expected JPA Criteria predicate against a fully mocked {@link CriteriaBuilder}.
 */
@ExtendWith(MockitoExtension.class)
class CommissionSpecificationTest {

    @Mock
    private Root<CommissionEntity> root;

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
        Specification<CommissionEntity> spec = CommissionSpecification.filterByCriteria(null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).and(new Predicate[0]);
    }

    @Test
    void filterByCriteria_activeStatus_addsIsNullDeletedAtPredicate() {
        CommissionCriteria criteria = new CommissionCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId());

        CommissionSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).isNull(path);
        verify(cb, never()).isNotNull(any());
    }

    @Test
    void filterByCriteria_inactiveStatus_addsIsNotNullDeletedAtPredicate() {
        CommissionCriteria criteria = new CommissionCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId() + 1);

        CommissionSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).isNotNull(path);
        verify(cb, never()).isNull(any());
    }

    @Test
    void filterByCriteria_withName_addsLikePredicateOnLoweredName() {
        CommissionCriteria criteria = new CommissionCriteria();
        criteria.setName("Tecnica");

        CommissionSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%tecnica%"));
    }

    @Test
    void filterByCriteria_blankName_isIgnored() {
        CommissionCriteria criteria = new CommissionCriteria();
        criteria.setName("   ");

        CommissionSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void filterByCriteria_withNameEs_addsLikePredicate() {
        CommissionCriteria criteria = new CommissionCriteria();
        criteria.setNameEs("Tecnica");

        CommissionSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%tecnica%"));
    }

    @Test
    void filterByCriteria_withCommissionType_addsEqualPredicate() {
        CommissionCriteria criteria = new CommissionCriteria();
        criteria.setCommissionType(CommissionType.SUPERIOR);

        CommissionSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, CommissionType.SUPERIOR);
    }

    @Test
    void filterByCriteria_withApprovalDate_addsEqualPredicate() {
        CommissionCriteria criteria = new CommissionCriteria();
        LocalDate date = LocalDate.of(2026, 1, 15);
        criteria.setApprovalDate(date);

        CommissionSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, date);
    }

    @Test
    void filterByCriteria_withExpedientNumber_addsEqualPredicateOnTrimmedValue() {
        CommissionCriteria criteria = new CommissionCriteria();
        criteria.setExpedientNumber("  EXP-001  ");

        CommissionSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, "EXP-001");
    }

    @Test
    void filterByCriteria_blankExpedientNumber_isIgnored() {
        CommissionCriteria criteria = new CommissionCriteria();
        criteria.setExpedientNumber("   ");

        CommissionSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).equal(any(), eq("   "));
    }

    @Test
    void filterByCriteria_withSearch_orsNameNameEsAndExpedientNumberPredicates() {
        CommissionCriteria criteria = new CommissionCriteria();
        criteria.setSearch("tecnica");

        CommissionSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, org.mockito.Mockito.times(3)).like(eq(path), eq("%tecnica%"));
        verify(cb).or(predicate, predicate, predicate);
    }

    @Test
    void filterByCriteria_blankSearch_isIgnored() {
        CommissionCriteria criteria = new CommissionCriteria();
        criteria.setSearch("  ");

        CommissionSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).or(any(Predicate.class), any(Predicate.class), any(Predicate.class));
    }
}
