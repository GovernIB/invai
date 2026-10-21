package es.caib.invai.back.persistence.repository.application.documentation.attachment;

import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentEntity;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentVersionEntity;
import es.caib.invai.back.persistence.model.catalog.appSection.LkupAppSectionEntity;
import es.caib.invai.back.persistence.model.catalog.documentStatus.LkupDocumentStatusEntity;
import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility factory class building the JPA {@link Specification} that lists the documents of an
 * application. It also builds the ordering, because some of the sortable columns (version and file
 * format) belong to the current version of each document and not to the document itself.
 *
 * @since 1.0.6
 */
@NoArgsConstructor
public final class AppDocumentationAttachmentSpecification {

    /**
     * Builds a JPA {@link Specification} selecting the active documents that fulfil the given
     * criteria, ordered as the given sort asks. The sort is applied here, so the pageable that
     * runs the query must not carry it.
     *
     * @param criteria the filter criteria to translate into predicates, may be {@code null}
     * @param sort     the requested order; the sortable keys are {@code title}, {@code section},
     *                 {@code mimeType}, {@code category}, {@code subcategory}, {@code versionLabel}
     *                 and {@code status}, anything else is ignored
     * @return a specification matching the documents fulfilling the given criteria
     */
    public static Specification<AppDocumentationAttachmentEntity> filterByCriteria(AppDocumentationAttachmentCriteria criteria, Sort sort) {
        return (root, query, cb) -> {
            boolean isCountQuery = Long.class.equals(query.getResultType());

            Join<AppDocumentationAttachmentEntity, LkupAppSectionEntity> section;
            Join<AppDocumentationAttachmentEntity, DocumentCategoryEntity> category;
            Join<DocumentCategoryEntity, DocumentCategoryEntity> parent;
            Join<AppDocumentationAttachmentEntity, LkupDocumentStatusEntity> status;
            if (isCountQuery) {
                section = root.join("section");
                category = root.join("documentCategory");
                parent = category.join("parent", JoinType.LEFT);
                status = root.join("status");
            } else {
                section = fetchAsJoin(root, "section", JoinType.INNER);
                category = fetchAsJoin(root, "documentCategory", JoinType.INNER);
                parent = fetchAsJoin(category, "parent", JoinType.LEFT);
                status = fetchAsJoin(root, "status", JoinType.INNER);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (Objects.nonNull(criteria)) {
                if (Objects.nonNull(criteria.getDeleted())) {
                    predicates.add(criteria.getDeleted() ? cb.isNotNull(root.get("deletedAt")) : cb.isNull(root.get("deletedAt")));
                }
                if (Objects.nonNull(criteria.getApplicationId())) {
                    predicates.add(cb.equal(root.get("application").get("id"), criteria.getApplicationId()));
                }
                if (StringUtils.isNotBlank(criteria.getSearch())) {
                    predicates.add(cb.like(cb.lower(root.get("title")), "%" + criteria.getSearch().trim().toLowerCase() + "%"));
                }
                if (Objects.nonNull(criteria.getSectionId())) {
                    predicates.add(cb.equal(section.get("id"), criteria.getSectionId()));
                }
                if (Objects.nonNull(criteria.getStatusId())) {
                    predicates.add(cb.equal(status.get("id"), criteria.getStatusId()));
                }
                if (Objects.nonNull(criteria.getCategoryId())) {
                    predicates.add(cb.or(
                            cb.equal(category.get("id"), criteria.getCategoryId()),
                            cb.equal(parent.get("id"), criteria.getCategoryId())
                    ));
                }
                if (Objects.nonNull(criteria.getMimeTypeId())) {
                    Subquery<Long> withMimeType = query.subquery(Long.class);
                    Root<AppDocumentationAttachmentVersionEntity> version = withMimeType.from(AppDocumentationAttachmentVersionEntity.class);
                    withMimeType.select(version.get("id")).where(
                            cb.equal(version.get("appDocumentationAttachment"), root),
                            cb.isTrue(version.get("current")),
                            cb.equal(version.get("mimeType").get("id"), criteria.getMimeTypeId())
                    );
                    predicates.add(cb.exists(withMimeType));
                }
            }

            if (!isCountQuery) {
                query.orderBy(buildOrders(sort, root, query, cb, section, category, parent, status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Translates the requested sort into the ordering of the query. The title and the id always
     * close the ordering, so pages are stable.
     *
     * @param sort     the requested order
     * @param root     the document being queried
     * @param query    the query the ordering belongs to, used to build the current version subqueries
     * @param cb       the criteria builder
     * @param section  the joined section of the document
     * @param category the joined stored category of the document
     * @param parent   the joined parent of the stored category, which may be missing
     * @param status   the joined state of the document
     * @return the ordering to apply to the query
     */
    private static List<Order> buildOrders(Sort sort, Root<AppDocumentationAttachmentEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb,
                                           Join<AppDocumentationAttachmentEntity, LkupAppSectionEntity> section,
                                           Join<AppDocumentationAttachmentEntity, DocumentCategoryEntity> category,
                                           Join<DocumentCategoryEntity, DocumentCategoryEntity> parent,
                                           Join<AppDocumentationAttachmentEntity, LkupDocumentStatusEntity> status) {
        List<Order> orders = new ArrayList<>();
        for (Sort.Order requested : sort) {
            Expression<?> expression = switch (requested.getProperty()) {
                case "title" -> root.get("title");
                case "section" -> section.get("name");
                case "status" -> status.get("name");
                case "category" -> cb.coalesce(parent.<String>get("name"), category.<String>get("name"));
                case "subcategory" -> cb.<String>selectCase().when(cb.isNull(parent.get("id")), "").otherwise(category.<String>get("name"));
                case "versionLabel" -> currentAttachmentValue(query, cb, root, "versionLabel");
                case "mimeType" -> currentAttachmentMimeTypeName(query, cb, root);
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
     * Builds a subquery returning a text column of the current, active version of the document.
     *
     * @param query     the query the subquery belongs to
     * @param cb        the criteria builder
     * @param root      the document the version belongs to
     * @param attribute name of the text attribute of the version to return
     * @return the subquery
     */
    private static Subquery<String> currentAttachmentValue(CriteriaQuery<?> query, CriteriaBuilder cb, Root<AppDocumentationAttachmentEntity> root, String attribute) {
        Subquery<String> subquery = query.subquery(String.class);
        Root<AppDocumentationAttachmentVersionEntity> version = subquery.from(AppDocumentationAttachmentVersionEntity.class);
        subquery.select(version.<String>get(attribute)).where(
                cb.equal(version.get("appDocumentationAttachment"), root),
                cb.isTrue(version.get("current"))
        );
        return subquery;
    }

    /**
     * Builds a subquery returning the display name of the file format of the current, active
     * version of the document.
     *
     * @param query the query the subquery belongs to
     * @param cb    the criteria builder
     * @param root  the document the version belongs to
     * @return the subquery
     */
    private static Subquery<String> currentAttachmentMimeTypeName(CriteriaQuery<?> query, CriteriaBuilder cb, Root<AppDocumentationAttachmentEntity> root) {
        Subquery<String> subquery = query.subquery(String.class);
        Root<AppDocumentationAttachmentVersionEntity> version = subquery.from(AppDocumentationAttachmentVersionEntity.class);
        subquery.select(version.get("mimeType").<String>get("name")).where(
                cb.equal(version.get("appDocumentationAttachment"), root),
                cb.isTrue(version.get("current"))
        );
        return subquery;
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
