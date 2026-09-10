package es.caib.invai.back.service.facade.application.accessibility;

import es.caib.invai.back.interna.application.accessibility.DTO.AppAccessibilityInputDTO;
import es.caib.invai.back.interna.application.accessibility.DTO.AppAccessibilityOutputDTO;

/**
 * Facade port for CRUD operations on the "AppAccessibility" (Accessibilitat) tab anchor, backing
 * {@code AppAccessibilityController}.
 *
 * @since 1.0.4
 */
public interface AppAccessibilityService {

    /**
     * Fetches the accessibility anchor by its own primary key.
     *
     * @param id primary key of the accessibility anchor
     * @return the mapped anchor, or {@code null} if no anchor exists with this ID (does not throw)
     */
    AppAccessibilityOutputDTO getById(Long id);

    /**
     * Creates a new accessibility anchor for the application referenced in the payload. At most
     * one active (non soft-deleted) anchor is allowed per application.
     *
     * @param inputDTO the creation payload
     * @return the created anchor, mapped to its output transfer representation
     * @throws es.caib.invai.back.exception.BusinessRuleException if the target application already
     * has an active accessibility anchor
     */
    AppAccessibilityOutputDTO create(AppAccessibilityInputDTO inputDTO);

    /**
     * Updates the accessibility anchor identified by {@code id} with the given input payload.
     * Does not check whether the anchor is currently soft-deleted.
     *
     * @param id       identifier of the anchor to update
     * @param inputDTO the update payload
     * @return the updated anchor, mapped to its output transfer representation
     * @throws es.caib.invai.back.exception.BusinessRuleException if no anchor exists with the given ID
     */
    AppAccessibilityOutputDTO update(Long id, AppAccessibilityInputDTO inputDTO);

    /**
     * Soft-deletes the accessibility anchor identified by {@code id}.
     *
     * @param id identifier of the anchor to delete
     * @throws es.caib.invai.back.exception.BusinessRuleException if no anchor exists with the given
     * ID, or it is already soft-deleted
     */
    void delete(Long id);
}
