package es.caib.invai.back.ejb.application.security.webContext;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.development.webContext.DTO.AppWebContextOutputDTO;
import es.caib.invai.back.interna.application.security.webContext.DTO.AppWebContextValidateInputDTO;
import es.caib.invai.back.persistence.repository.application.security.webContext.AppWebContextCriteria;
import es.caib.invai.back.persistence.repository.application.security.webContext.AppWebContextRepository;
import es.caib.invai.back.service.facade.application.security.webContext.AppWebContextService;
import es.caib.invai.back.service.mapper.application.security.webContext.AppWebContextMapper;
import es.caib.invai.back.service.model.application.security.webContext.AppWebContext;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Facade service implementation for Security's view of web context assignments: a read-only
 * listing plus the one-way validation action. Creating, editing, and deleting is handled
 * exclusively by {@code ejb.application.development.webContext.AppWebContextServiceFacadeBean}.
 *
 * @since 1.0.4
 */
// Explicit bean name: Spring's default naming only looks at the simple class name, which collides
// with ejb.application.development.webContext.AppWebContextServiceFacadeBean otherwise.
@Service("securityAppWebContextServiceFacadeBean")
@Slf4j
@Transactional
public class AppWebContextServiceFacadeBean implements AppWebContextService {

    /** MapStruct mapper handling transformations between entities, domain models, and DTO layouts. */
    @Autowired
    private AppWebContextMapper appWebContextMapper;

    /** Infrastructure outbound repository port managing relational lifecycle data operations. */
    @Autowired
    private AppWebContextRepository appWebContextRepository;

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries, scoped to a single
     * parent security anchor. Never returns web context links belonging to other security anchors.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      the multi-parameter business query filter boundaries
     * @param pageable      pagination layout boundaries and sorting rules configuration
     * @return a partitioned matrix page containing mapped output definitions
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AppWebContextOutputDTO> getAll(Long appSecurityId, AppWebContextCriteria criteria, Pageable pageable) {
        log.debug("Facade: Executing dynamic search pattern pipeline across application web context components for appSecurity ID: {}", appSecurityId);
        Page<AppWebContext> domainPage = appWebContextRepository.findAllByAppSecurityId(appSecurityId, criteria, pageable);
        return domainPage.map(appWebContextMapper::toResponse);
    }

    /**
     * Marks a web context assignment as validated, stamping the moment, the acting user, and the
     * given justification. One-way: once validated, this method refuses to touch it again.
     *
     * @param id       identifier of the link to validate
     * @param inputDTO payload carrying the mandatory free-text validation justification
     * @throws BusinessRuleException if the target record is missing or already validated
     */
    @Override
    public void validate(Long id, AppWebContextValidateInputDTO inputDTO) {
        log.info("Facade: Validating web context ID: {}", id);

        AppWebContext existingModel = appWebContextRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_WEB_CONTEXT_NOT_FOUND);
        }
        if (existingModel.isValidated()) {
            throw new BusinessRuleException(Constants.ERR_APP_WEB_CONTEXT_ALREADY_VALIDATED);
        }

        Utils.sanitize(inputDTO);
        existingModel.setValidated(true);
        existingModel.setValidatedAt(LocalDateTime.now());
        existingModel.setValidatedBy(Utils.resolveCurrentUsername());
        existingModel.setValidatedReason(inputDTO.getReason());

        appWebContextRepository.update(existingModel, id);
    }
}
