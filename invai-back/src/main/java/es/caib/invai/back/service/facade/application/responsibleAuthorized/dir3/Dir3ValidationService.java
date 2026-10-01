package es.caib.invai.back.service.facade.application.responsibleAuthorized.dir3;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.responsibleAuthorized.dir3.DTO.Dir3ManualValidateInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.dir3.DTO.Dir3ValidationOutputDTO;

/**
 * Facade boundary declaring the transactional use cases for managing {@code Dir3Validation}
 * records directly - i.e. independently of whether they are currently linked to an
 * {@code AppResponsible} or an {@code AppAuthorized} assignment (or both, when shared across a
 * person's active assignments on the same anchor).
 *
 * @since 1.0.5
 */
public interface Dir3ValidationService {

    /**
     * Promotes a DIR3 validation from {@code NOT_VALIDATED} to {@code MANUAL}, recording the
     * moment, the acting user, and the given justification. One-way: once {@code MANUAL}, this
     * cannot be used to change it again - the only ways out of {@code MANUAL} are a person change
     * on the owning assignment or the owning application's administrative unit changing.
     * <p>
     * Since a {@code Dir3Validation} record may be shared by a person's active
     * {@code AppResponsible} and {@code AppAuthorized} assignments on the same anchor, validating
     * it here is reflected on every assignment that shares it - there is no need for a separate
     * endpoint per assignment type.
     * </p>
     *
     * @param id identifier of the DIR3 validation record to manually validate
     * @param inputDTO payload carrying the mandatory free-text reason for the manual validation
     * @return the updated DIR3 validation record
     * @throws BusinessRuleException if no DIR3 validation exists with the given ID, or its status
     * isn't currently {@code NOT_VALIDATED}
     */
    Dir3ValidationOutputDTO validateManually(Long id, Dir3ManualValidateInputDTO inputDTO);
}
