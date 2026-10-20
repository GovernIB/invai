package es.caib.invai.back.ejb.application.responsibleAuthorized.dir3;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.responsibleAuthorized.dir3.DTO.Dir3ManualValidateInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.dir3.DTO.Dir3ValidationOutputDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.dir3.Dir3ValidationRepository;
import es.caib.invai.back.service.facade.application.responsibleAuthorized.dir3.Dir3ValidationService;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.dir3.Dir3ValidationMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.dir3.Dir3Validation;
import es.caib.invai.back.service.model.catalog.dir3Status.Dir3ValidationStatus;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Facade service implementation for managing {@code Dir3Validation} records directly, replacing
 * what used to be two near-identical endpoints (one under {@code AppResponsible}, one under
 * {@code AppAuthorized}) that both loaded their own anchor row for no reason other than to reach
 * its {@code dir3Validation} - the actual mutation target was always the {@code Dir3Validation}
 * record itself.
 *
 * @since 1.0.5
 */
@Service
@Slf4j
@Transactional
public class Dir3ValidationServiceFacadeBean implements Dir3ValidationService {

    /** Repository providing persistence operations for Dir3Validation records. */
    @Autowired
    private Dir3ValidationRepository dir3ValidationRepository;

    /** Mapper used to convert Dir3Validation domain models into their DTO representation. */
    @Autowired
    private Dir3ValidationMapper dir3ValidationMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Dir3ValidationOutputDTO validateManually(Long id, Dir3ManualValidateInputDTO inputDTO) {
        log.info("Facade: Manually validating DIR3 validation ID: {}", id);

        Dir3Validation existing = dir3ValidationRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_DIR3VALIDATION_NOT_FOUND);
        }
        if (existing.getDir3Status() != Dir3ValidationStatus.NOT_VALIDATED) {
            throw new BusinessRuleException(Constants.ERR_DIR3VALIDATION_MANUAL_VALIDATION_NOT_ALLOWED);
        }

        Utils.sanitize(inputDTO);
        existing.setDir3Status(Dir3ValidationStatus.MANUAL);
        existing.setManualValidatedAt(LocalDateTime.now());
        existing.setManualValidatedBy(Utils.resolveCurrentUsername());
        existing.setReason(inputDTO.getReason());

        Dir3Validation updated = dir3ValidationRepository.update(existing, id);
        return dir3ValidationMapper.toResponse(updated);
    }
}
