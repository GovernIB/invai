package es.caib.invai.back.persistence.repository.application.core;

import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.responsible.AppResponsibleEntity;
import es.caib.invai.back.persistence.model.application.system_database.database.AppDatabaseEntity;
import es.caib.invai.back.persistence.model.application.system_database.system.AppSystemEntity;
import jakarta.persistence.criteria.*;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaFunction;
import org.hibernate.query.criteria.JpaPredicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ApplicationSpecification}, verifying that each {@link ApplicationCriteria}
 * field builds the expected JPA Criteria predicate against a fully mocked {@link CriteriaBuilder}.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationSpecificationTest {

    @Mock
    private Root<ApplicationEntity> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private HibernateCriteriaBuilder cb;

    @Mock(extraInterfaces = JpaFunction.class)
    private Path<String> path;

    @Mock
    private JpaPredicate predicate;

    @Mock
    private Subquery<Long> subquery;

    @Mock
    private Root<AppResponsibleEntity> appResponsibleRoot;

    @Mock
    private Root<AppDatabaseEntity> appDatabaseRoot;

    @Mock
    private Root<AppSystemEntity> appSystemRoot;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        lenient().when(root.<String>get(anyString())).thenReturn(path);
        lenient().when(path.<String>get(anyString())).thenReturn(path);
        lenient().when(cb.lower(any())).thenReturn((JpaFunction<String>) path);
        lenient().when(cb.like(any(), anyString())).thenReturn(predicate);
        lenient().when(cb.ilike(any(), anyString())).thenReturn(predicate);
        lenient().when(cb.equal(any(), any())).thenReturn(predicate);
        lenient().when(cb.isNull(any())).thenReturn(predicate);
        lenient().when(cb.not(any())).thenReturn(predicate);
        lenient().when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(predicate);
        lenient().when(cb.disjunction()).thenReturn(predicate);
        lenient().when(cb.conjunction()).thenReturn(predicate);
        lenient().when(path.in(anyCollection())).thenReturn(predicate);

        lenient().when(query.subquery(Long.class)).thenReturn(subquery);
        lenient().when(subquery.from(AppResponsibleEntity.class)).thenReturn(appResponsibleRoot);
        lenient().when(subquery.from(AppDatabaseEntity.class)).thenReturn(appDatabaseRoot);
        lenient().when(subquery.from(AppSystemEntity.class)).thenReturn(appSystemRoot);
        lenient().when(subquery.select(any())).thenReturn(subquery);
        lenient().when(subquery.where(any(Predicate.class))).thenReturn(subquery);
        lenient().when(cb.exists(any())).thenReturn(predicate);
        lenient().when(appResponsibleRoot.<String>get(anyString())).thenReturn(path);
        lenient().when(appDatabaseRoot.<String>get(anyString())).thenReturn(path);
        lenient().when(appSystemRoot.<String>get(anyString())).thenReturn(path);
    }

    @Test
    void filterByCriteria_nullCriteria_buildsEmptyConjunction() {
        Specification<ApplicationEntity> spec = ApplicationSpecification.filterByCriteria(null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).and();
    }

    @Test
    void filterByCriteria_withPrefix_addsLikePredicate() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setPrefix("AP1");

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%ap1%"));
    }

    @Test
    void filterByCriteria_blankPrefix_isIgnored() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setPrefix("   ");

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void filterByCriteria_withApplicationName_addsLikePredicate() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setApplicationName("Invai");

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%invai%"));
    }

    @Test
    void filterByCriteria_blankApplicationName_isIgnored() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setApplicationName("");

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void filterByCriteria_withCategoryId_addsEqualPredicate() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setCategoryId(1L);

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 1L);
    }

    @Test
    void filterByCriteria_nullCategoryId_isIgnored() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setCategoryId(null);

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).equal(any(), any());
    }

    @Test
    void filterByCriteria_withSystemTypeId_addsEqualPredicate() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setSystemTypeId(2L);

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 2L);
    }

    @Test
    void filterByCriteria_withFieldId_addsEqualPredicate() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setFieldId(4L);

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 4L);
    }

    @Test
    void filterByCriteria_nullFieldId_isIgnored() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setFieldId(null);

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).equal(any(), any());
    }

    @Test
    void filterByCriteria_withCommissionId_addsEqualPredicate() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setCommissionId(3L);

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 3L);
    }

    @Test
    void filterByCriteria_withAdmUnitCode_addsEqualPredicate() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setAdmUnitCode("A04026919");

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, "A04026919");
    }

    @Test
    void filterByCriteria_blankAdmUnitCode_isIgnored() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setAdmUnitCode("   ");

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).equal(any(), any());
    }

    @Test
    void filterByCriteria_withStatusId_addsEqualPredicate() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setStatusId(1L);

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).equal(path, 1L);
    }

    @Test
    void filterByCriteria_nullStatusId_isIgnored() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setStatusId(null);

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).equal(any(), any());
    }

    @Test
    void filterByCriteria_withDescription_addsIlikePredicate() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setDescription("Portal");

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).ilike(eq(path), eq("%Portal%"));
    }

    @Test
    void filterByCriteria_blankDescription_isIgnored() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setDescription("   ");

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).ilike(any(), anyString());
    }

    // criteria.incomplete no longer builds a predicate itself - ApplicationServiceFacadeBean.getAll
    // pre-resolves the set of incomplete application IDs (via
    // ApplicationRepository.findIncompleteApplicationIds) onto incompleteApplicationIds before the
    // query runs, the same "resolve live data, then filter with .in(...)" pattern already used for
    // quickSearchAdmUnitCodes below. The Specification's only job is the trivial id-in-list check.

    @Test
    void filterByCriteria_withIncompleteTrueAndIds_addsIdInPredicate() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setIncomplete(Boolean.TRUE);
        criteria.setIncompleteApplicationIds(List.of(1L, 2L));

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(path).in(List.of(1L, 2L));
        verify(cb, never()).not(any());
    }

    @Test
    void filterByCriteria_withIncompleteFalseAndIds_negatesTheIdInPredicate() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setIncomplete(Boolean.FALSE);
        criteria.setIncompleteApplicationIds(List.of(1L, 2L));

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(path).in(List.of(1L, 2L));
        verify(cb).not(predicate);
    }

    @Test
    void filterByCriteria_withIncompleteTrueAndNoIncompleteApplications_matchesNothing() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setIncomplete(Boolean.TRUE);
        criteria.setIncompleteApplicationIds(List.of());

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).disjunction();
        verify(path, never()).in(anyCollection());
    }

    @Test
    void filterByCriteria_withIncompleteFalseAndNoIncompleteApplications_matchesEverything() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setIncomplete(Boolean.FALSE);
        criteria.setIncompleteApplicationIds(null);

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).conjunction();
        verify(path, never()).in(anyCollection());
    }

    @Test
    void filterByCriteria_nullIncomplete_isIgnored() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setIncomplete(null);

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(path, never()).in(anyCollection());
        verify(cb, never()).disjunction();
        verify(cb, never()).conjunction();
    }

    @Test
    void filterByCriteria_withResponsibleId_addsExistsPredicateOnAppResponsible() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setResponsibleId(5L);

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(subquery).from(AppResponsibleEntity.class);
        verify(cb).exists(subquery);
    }

    @Test
    void filterByCriteria_nullResponsibleId_isIgnored() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setResponsibleId(null);

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).exists(any());
    }

    @Test
    void filterByCriteria_withDatabaseId_addsExistsPredicateOnAppDatabase() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setDatabaseId(6L);

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(subquery).from(AppDatabaseEntity.class);
        verify(cb).exists(subquery);
    }

    @Test
    void filterByCriteria_nullDatabaseId_isIgnored() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setDatabaseId(null);

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).exists(any());
    }

    @Test
    void filterByCriteria_withServerId_orsExistsPredicatesOnAppSystemAndAppDatabase() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setServerId(7L);

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(subquery).from(AppSystemEntity.class);
        verify(subquery).from(AppDatabaseEntity.class);
        verify(cb, times(2)).exists(subquery);
        verify(cb).or(predicate, predicate);
    }

    @Test
    void filterByCriteria_nullServerId_isIgnored() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setServerId(null);

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).exists(any());
    }

    @Test
    void filterByCriteria_withEnvironmentId_orsExistsPredicatesOnAppSystemAndAppDatabase() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setEnvironmentId(8L);

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(subquery).from(AppSystemEntity.class);
        verify(subquery).from(AppDatabaseEntity.class);
        verify(cb, times(2)).exists(subquery);
        verify(cb).or(predicate, predicate);
    }

    @Test
    void filterByCriteria_nullEnvironmentId_isIgnored() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setEnvironmentId(null);

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).exists(any());
    }

    @Test
    void filterByCriteria_withQuickSearch_orsAllSearchableFieldPredicates() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setQuickSearch("lead");

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, times(12)).like(eq(path), eq("%lead%"));
        verify(cb).ilike(eq(path), eq("%lead%"));
        verify(cb).or(predicate, predicate, predicate, predicate, predicate,
                predicate, predicate, predicate, predicate, predicate,
                predicate, predicate, predicate, predicate);
    }

    @Test
    void filterByCriteria_blankQuickSearch_isIgnored() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setQuickSearch("   ");

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).or(any(Predicate[].class));
    }

    @Test
    void filterByCriteria_withQuickSearchAndNoResolvedAdmUnitCodes_addsDisjunctionInsteadOfInPredicate() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setQuickSearch("lead");

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).disjunction();
        verify(path, never()).in(anyCollection());
    }

    @Test
    void filterByCriteria_withQuickSearchAndResolvedAdmUnitCodes_addsInPredicateInsteadOfDisjunction() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setQuickSearch("lead");
        criteria.setQuickSearchAdmUnitCodes(List.of("A04026919"));

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(path).in(List.of("A04026919"));
        verify(cb, never()).disjunction();
    }

    @Test
    void filterByCriteria_withQuickSearchAndEmptyResolvedAdmUnitCodes_addsDisjunctionInsteadOfInPredicate() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setQuickSearch("lead");
        criteria.setQuickSearchAdmUnitCodes(List.of());

        ApplicationSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).disjunction();
        verify(path, never()).in(anyCollection());
    }
}
