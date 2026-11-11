package es.caib.invai.back.persistence.repository.application.documentation.link;

import es.caib.invai.back.persistence.model.application.documentation.link.AppLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository for {@link AppLinkEntity} records.
 *
 * @since 1.0.6
 */
@Repository
public interface AppLinkJPARepository extends JpaRepository<AppLinkEntity, Long>, JpaSpecificationExecutor<AppLinkEntity> {

    /**
     * Tells whether an active link of the application has the given title, category and address.
     *
     * @param applicationId identifier of the application
     * @param title         title of the link
     * @param categoryId    identifier of the category
     * @param url           address of the link
     * @return {@code true} if such an active link exists
     */
    boolean existsByApplicationIdAndTitleAndCategoryIdAndUrlAndDeletedAtIsNull(Long applicationId, String title, Long categoryId, String url);

    /**
     * Tells whether an active link other than the given one has, in the same application, the
     * given title, category and address.
     *
     * @param applicationId identifier of the application
     * @param title         title of the link
     * @param categoryId    identifier of the category
     * @param url           address of the link
     * @param id            identifier of the link to leave out
     * @return {@code true} if such an active link exists
     */
    boolean existsByApplicationIdAndTitleAndCategoryIdAndUrlAndIdNotAndDeletedAtIsNull(Long applicationId, String title, Long categoryId, String url, Long id);
}
