package es.caib.invai.back.service.facade.application.responsibleAuthorized.responsible;

import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleDeleteDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleOutputDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting AppResponsible assignments.
 *
 * @since 1.0.3
 */
public interface AppResponsibleService {
    Page<AppResponsibleOutputDTO> getAll(Long appResponsibleAuthorizedId, AppResponsibleCriteria criteria, Pageable pageable);
    AppResponsibleOutputDTO create(AppResponsibleInputDTO inputDTO);
    AppResponsibleOutputDTO update(Long id, AppResponsibleInputDTO inputDTO);
    void delete(Long id, AppResponsibleDeleteDTO dto);
    AppResponsibleOutputDTO reactivate(Long id);
}
