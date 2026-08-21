package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible;

import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Core business domain outbound port boundary interface declaring relational persistence
 * mechanisms for the {@link AppResponsible} entity.
 *
 * @since 1.0.3
 */
public interface AppResponsibleRepository {
    AppResponsible create(AppResponsible appResponsible);
    AppResponsible update(AppResponsible appResponsible, Long id);
    void delete(AppResponsible appResponsible);
    AppResponsible findById(Long id);
    Page<AppResponsible> findAll(Long appResponsibleAuthorizedId, AppResponsibleCriteria criteria, Pageable pageable);
    boolean existsByAppResponsibleAuthorizedAndResponsibleTypeAndIdNot(Long appResponsibleAuthorizedId, Long responsibleTypeId, Long id);

    /** Finds every active assignment currently held by the given person, across all applications. */
    List<AppResponsible> findAllActiveByPersonId(Long personId);

    /** Finds the currently active assignment (if any) holding the given responsible type on the given anchor. */
    AppResponsible findActiveByAppResponsibleAuthorizedAndResponsibleType(Long appResponsibleAuthorizedId, Long responsibleTypeId);
}
