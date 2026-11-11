package es.caib.invai.back.persistence.repository.application.documentation.link;

import es.caib.invai.back.persistence.model.application.documentation.link.AppLinkEntity;
import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.Collection;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the restriction by allowed categories of {@link AppLinkSpecification}, the one that
 * keeps what belongs to the categories a user can't read out of the listing.
 */
@ExtendWith(MockitoExtension.class)
class AppLinkSpecificationTest {

    @Mock
    private Root<AppLinkEntity> root;
    @Mock
    private CriteriaQuery<?> query;
    @Mock
    private CriteriaBuilder cb;
    @Mock
    private Join<AppLinkEntity, DocumentCategoryEntity> category;
    @Mock
    private Path<Object> categoryId;
    @Mock
    private Predicate predicate;

    @BeforeEach
    void setUp() {
        doReturn(Long.class).when(query).getResultType();
        doReturn(category).when(root).join("category");
        lenient().doReturn(categoryId).when(category).get("id");
        lenient().doReturn(predicate).when(categoryId).in(anyCollection());
        lenient().when(cb.disjunction()).thenReturn(predicate);
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(predicate);
    }

    private void build(Collection<Long> allowedCategoryIds) {
        AppLinkSpecification.filterByCriteria(new AppDocumentationLinkCriteria(), allowedCategoryIds, Sort.unsorted()).toPredicate(root, query, cb);
    }

    @Test
    void filterByCriteria_withoutAllowedCategories_doesNotRestrictByCategory() {
        build(null);

        verify(categoryId, never()).in(anyCollection());
        verify(cb, never()).disjunction();
    }

    @Test
    void filterByCriteria_withAllowedCategories_keepsOnlyThoseCategories() {
        Set<Long> allowed = Set.of(5L, 7L);

        build(allowed);

        verify(categoryId).in(allowed);
    }

    @Test
    void filterByCriteria_withNoAllowedCategory_matchesNothing() {
        build(Set.of());

        verify(cb).disjunction();
        verify(categoryId, never()).in(anyCollection());
    }
}
