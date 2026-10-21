package es.caib.invai.back.persistence.repository.application.documentation.link;

import es.caib.invai.back.persistence.model.application.documentation.link.AppLinkEntity;
import es.caib.invai.back.persistence.model.catalog.appSection.LkupAppSectionEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Utility factory class building the JPA {@link Specification} that lists the links of an
 * application. It also builds the ordering, because the section is a column of the listing and
 * belongs to another table.
 *
 * @since 1.0.6
 */
@NoArgsConstructor
public final class AppLinkSpecification {

    /**
     * Builds a JPA {@link Specification} selecting the links that fulfil the given criteria,
     * ordered as the given sort asks. The sort is applied here, so the pageable that runs the query
     * must not carry it.
     *
     * @param criteria the filter criteria to translate into predicates, may be {@code null}
     * @param sort     the requested order; the sortable keys are {@code title}, {@code description},
     *                 {@code section} and {@code url}, anything else is ignored
     * @return a specification matching the links fulfilling the given criteria
     */
    public static Specification<AppLinkEntity> filterByCriteria(AppDocumentationLinkCriteria criteria, Sort sort) {
        return (root, query, cb) -> {
            boolean isCountQuery = Long.class.equals(query.getResultType());

            Join<AppLinkEntity, LkupAppSectionEntity> section = isCountQuery
                    ? root.join("section")
                    : fetchAsJoin(root, "section", JoinType.INNER);

            List<Predicate> predicates = new ArrayList<>();

            if (Objects.nonNull(criteria)) {
                if (Objects.nonNull(criteria.getDeleted())) {
                    predicates.add(criteria.getDeleted() ? cb.isNotNull(root.get("deletedAt")) : cb.isNull(root.get("deletedAt")));
                }
                if (Objects.nonNull(criteria.getApplicationId())) {
                    predicates.add(cb.equal(root.get("application").get("id"), criteria.getApplicationId()));
                }
                if (StringUtils.isNotBlank(criteria.getTitle())) {
                    predicates.add(contains(cb, root.get("title"), criteria.getTitle()));
                }
                if (StringUtils.isNotBlank(criteria.getDescription())) {
                    predicates.add(contains(cb, root.get("description"), criteria.getDescription()));
                }
                if (Objects.nonNull(criteria.getSectionId())) {
                    predicates.add(cb.equal(section.get("id"), criteria.getSectionId()));
                }
                if (StringUtils.isNotBlank(criteria.getUrl())) {
                    predicates.add(contains(cb, root.get("url"), criteria.getUrl()));
                }
                if (StringUtils.isNotBlank(criteria.getQuickSearch())) {
                    predicates.add(cb.or(
                            contains(cb, root.get("title"), criteria.getQuickSearch()),
                            contains(cb, root.get("description"), criteria.getQuickSearch()),
                            contains(cb, root.get("url"), criteria.getQuickSearch()),
                            contains(cb, section.get("name"), criteria.getQuickSearch()),
                            contains(cb, section.get("nameEs"), criteria.getQuickSearch())
                    ));
                }
            }

            if (!isCountQuery) {
                query.orderBy(buildOrders(sort, root, cb, section));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Builds a partial, case-insensitive match of a text attribute.
     *
     * @param cb         the criteria builder
     * @param expression the text attribute to match
     * @param text       the text to look for
     * @return the predicate
     */
    private static Predicate contains(CriteriaBuilder cb, Expression<String> expression, String text) {
        return cb.like(cb.lower(expression), "%" + text.trim().toLowerCase(Locale.ROOT) + "%");
    }

    /**
     * Translates the requested sort into the ordering of the query. The title and the id always
     * close the ordering, so pages are stable.
     *
     * @param sort    the requested order
     * @param root    the link being queried
     * @param cb      the criteria builder
     * @param section the joined section of the link
     * @return the ordering to apply to the query
     */
    private static List<Order> buildOrders(Sort sort, Root<AppLinkEntity> root, CriteriaBuilder cb,
                                           Join<AppLinkEntity, LkupAppSectionEntity> section) {
        List<Order> orders = new ArrayList<>();
        for (Sort.Order requested : sort) {
            Expression<?> expression = switch (requested.getProperty()) {
                case "title" -> root.get("title");
                case "description" -> root.get("description");
                case "section" -> section.get("name");
                case "url" -> root.get("url");
                default -> null;
            };
            if (Objects.nonNull(expression)) {
                orders.add(requested.isAscending() ? cb.asc(expression) : cb.desc(expression));
            }
        }
        orders.add(cb.asc(root.get("title")));
        orders.add(cb.asc(root.get("id")));
        return orders;
    }

    /**
     * Fetches an association of the given path and returns it as a join, so the same join can be
     * used to filter and to sort.
     *
     * @param from      the path the association hangs from
     * @param attribute name of the association
     * @param joinType  the type of join to fetch with
     * @param <X>       type of the path the association hangs from
     * @param <Y>       type of the associated entity
     * @return the fetched association seen as a join
     */
    @SuppressWarnings("unchecked")
    private static <X, Y> Join<X, Y> fetchAsJoin(From<?, X> from, String attribute, JoinType joinType) {
        return (Join<X, Y>) from.fetch(attribute, joinType);
    }
}
