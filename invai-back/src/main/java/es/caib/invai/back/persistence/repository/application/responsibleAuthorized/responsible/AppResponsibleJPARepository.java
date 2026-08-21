package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.responsible.AppResponsibleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data Native Bridge Interface infrastructure for the {@link AppResponsibleEntity}.
 *
 * @since 1.0.3
 */
@Repository
public interface AppResponsibleJPARepository extends JpaRepository<AppResponsibleEntity, Long>, JpaSpecificationExecutor<AppResponsibleEntity> {
    Optional<AppResponsibleEntity> findById(Long id);
    boolean existsByAppResponsibleAuthorizedIdAndResponsibleTypeIdAndDeletedAtIsNullAndIdNot(Long appResponsibleAuthorizedId, Long responsibleTypeId, Long id);

    /** Finds the currently active assignment (if any) holding the given responsible type on the given anchor. */
    Optional<AppResponsibleEntity> findByAppResponsibleAuthorizedIdAndResponsibleTypeIdAndDeletedAtIsNull(Long appResponsibleAuthorizedId, Long responsibleTypeId);

    /** Finds every active (not soft-deleted) assignment currently held by the given person, across all applications. */
    List<AppResponsibleEntity> findAllByPersonIdAndDeletedAtIsNull(Long personId);
}
