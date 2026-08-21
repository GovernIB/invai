package es.caib.invai.back.persistence.repository.maintenance.systems.server;

import es.caib.invai.back.persistence.model.maintenance.systems.server.ServerEntity;
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
 * Unit tests for {@link ServerSpecification}, verifying that each {@link ServerCriteria} field
 * builds the expected JPA Criteria predicate against a fully mocked {@link CriteriaBuilder}.
 */
@ExtendWith(MockitoExtension.class)
class ServerSpecificationTest {

    @Mock
    private Root<ServerEntity> root;

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
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(predicate);
    }

    @Test
    void filterByCriteria_nullCriteria_buildsEmptyConjunction() {
        Specification<ServerEntity> spec = ServerSpecification.filterByCriteria(null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).and(new Predicate[0]);
    }

    @Test
    void filterByCriteria_activeStatus_addsIsNullDeletedAtPredicate() {
        ServerCriteria criteria = new ServerCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId());

        ServerSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).isNull(path);
        verify(cb, never()).isNotNull(any());
    }

    @Test
    void filterByCriteria_inactiveStatus_addsIsNotNullDeletedAtPredicate() {
        ServerCriteria criteria = new ServerCriteria();
        criteria.setStatusId(StatusEnum.ACTIVE.getId() + 1);

        ServerSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).isNotNull(path);
        verify(cb, never()).isNull(any());
    }

    @Test
    void filterByCriteria_withName_addsLikePredicateOnLoweredName() {
        ServerCriteria criteria = new ServerCriteria();
        criteria.setName("Prod");

        ServerSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%prod%"));
    }

    @Test
    void filterByCriteria_blankName_isIgnored() {
        ServerCriteria criteria = new ServerCriteria();
        criteria.setName("   ");

        ServerSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void filterByCriteria_withEnvironmentId_addsEqualPredicate() {
        ServerCriteria criteria = new ServerCriteria();
        criteria.setEnvironmentId(5L);

        ServerSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).equal(eq(path), eq(5L));
    }

    @Test
    void filterByCriteria_withServerTypeId_addsEqualPredicate() {
        ServerCriteria criteria = new ServerCriteria();
        criteria.setServerTypeId(7L);

        ServerSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).equal(eq(path), eq(7L));
    }

    @Test
    void filterByCriteria_withServerTypeCode_addsEqualPredicate() {
        ServerCriteria criteria = new ServerCriteria();
        criteria.setServerTypeCode("DATABASE");

        ServerSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).equal(eq(path), eq("DATABASE"));
    }

    @Test
    void filterByCriteria_blankServerTypeCode_isIgnored() {
        ServerCriteria criteria = new ServerCriteria();
        criteria.setServerTypeCode("   ");

        ServerSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).equal(any(), anyString());
    }

    @Test
    void filterByCriteria_withSearch_addsLikePredicateOnLoweredName() {
        ServerCriteria criteria = new ServerCriteria();
        criteria.setSearch("web");

        ServerSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb).like(eq(path), eq("%web%"));
    }

    @Test
    void filterByCriteria_blankSearch_isIgnored() {
        ServerCriteria criteria = new ServerCriteria();
        criteria.setSearch("  ");

        ServerSpecification.filterByCriteria(criteria).toPredicate(root, query, cb);

        verify(cb, never()).like(any(), anyString());
    }
}
