package es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory;

import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryEntity;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link DocumentCategorySpecification}, verifying that each
 * {@link DocumentCategoryCriteria} field builds the expected JPA Criteria predicate against a
 * fully mocked {@link CriteriaBuilder}.
 */
@ExtendWith(MockitoExtension.class)
class DocumentCategorySpecificationTest {

    @Mock
    private Root<DocumentCategoryEntity> root;

    @Mock
    private Root<DocumentCategoryEntity> child;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private Subquery<Long> subquery;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<String> path;

    @Mock
    private Predicate predicate;

    @BeforeEach
    void setUp() {
        lenient().when(root.<String>get(anyString())).thenReturn(path);
        lenient().when(child.<String>get(anyString())).thenReturn(path);
        lenient().when(cb.lower(any())).thenReturn(path);
        lenient().when(cb.like(any(), anyString())).thenReturn(predicate);
        lenient().when(cb.isNull(any())).thenReturn(predicate);
        lenient().when(cb.isNotNull(any())).thenReturn(predicate);
        lenient().when(cb.equal(any(Path.class), any(Root.class))).thenReturn(predicate);
        lenient().when(cb.or(any(), any())).thenReturn(predicate);
        lenient().when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        lenient().when(cb.exists(any())).thenReturn(predicate);
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(predicate);
        lenient().<Subquery<Long>>when(query.subquery(Long.class)).thenReturn(subquery);
        lenient().when(subquery.from(DocumentCategoryEntity.class)).thenReturn(child);
        lenient().when(subquery.select(any())).thenReturn(subquery);
        lenient().when(subquery.where(any(Predicate[].class))).thenReturn(subquery);
    }

    private Predicate build(DocumentCategoryCriteria criteria) {
        Specification<DocumentCategoryEntity> spec = DocumentCategorySpecification.filterByCriteria(criteria);
        Predicate result = spec.toPredicate(root, query, cb);
        assertNotNull(result);
        return result;
    }

    @Test
    void filterByCriteria_nullCriteria_selectsOnlyTheCategories() {
        build(null);

        verify(root).get("parent");
        verify(cb, times(1)).isNull(path);
        verify(cb, never()).isNotNull(any());
        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void filterByCriteria_emptyCriteria_selectsOnlyTheCategories() {
        build(new DocumentCategoryCriteria());

        verify(root).get("parent");
        verify(cb, never()).like(any(), anyString());
        verify(query, never()).subquery(any());
    }

    @Test
    void filterByCriteria_activeStatus_addsDeletedAtIsNull() {
        build(new DocumentCategoryCriteria(null, StatusEnum.ACTIVE.getId(), null));

        verify(root).get("deletedAt");
        verify(cb, times(2)).isNull(path);
        verify(cb, never()).isNotNull(any());
    }

    @Test
    void filterByCriteria_inactiveStatus_addsDeletedAtIsNotNull() {
        build(new DocumentCategoryCriteria(null, StatusEnum.INACTIVE.getId(), null));

        verify(root).get("deletedAt");
        verify(cb).isNotNull(path);
    }

    @Test
    void filterByCriteria_name_matchesTheCategoryOrAnyOfItsSubcategories() {
        build(new DocumentCategoryCriteria("TÈC", null, null));

        verify(query).subquery(Long.class);
        verify(subquery).from(DocumentCategoryEntity.class);
        verify(cb).equal(any(), eq(root));
        verify(root).get("name");
        verify(root).get("nameEs");
        verify(child).get("name");
        verify(child).get("nameEs");
        verify(cb, times(4)).like(path, "%tèc%");
        verify(cb).exists(subquery);
    }

    @Test
    void filterByCriteria_nameAndSearch_eachBuildsItsOwnSubcategoriesCheck() {
        build(new DocumentCategoryCriteria("tèc", null, "api"));

        verify(query, times(2)).subquery(Long.class);
        verify(cb, times(2)).exists(subquery);
        verify(cb, times(4)).like(path, "%tèc%");
        verify(cb, times(4)).like(path, "%api%");
    }

    @Test
    void filterByCriteria_blankName_isIgnored() {
        build(new DocumentCategoryCriteria("   ", null, null));

        verify(cb, never()).like(any(), anyString());
    }

    @Test
    void filterByCriteria_search_matchesTheCategoryOrAnyOfItsSubcategories() {
        build(new DocumentCategoryCriteria(null, null, "API"));

        verify(query).subquery(Long.class);
        verify(subquery).from(DocumentCategoryEntity.class);
        verify(cb).equal(any(), eq(root));
        verify(cb, times(4)).like(path, "%api%");
        verify(cb).exists(subquery);
    }

    @Test
    void filterByCriteria_blankSearch_isIgnored() {
        build(new DocumentCategoryCriteria(null, null, " "));

        verify(query, never()).subquery(any());
        verify(cb, never()).exists(any());
    }
}
