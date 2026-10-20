package es.caib.invai.back.service.model.catalog.dir3Status;

import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonDir3CheckOutputDTO;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * Type-safe domain enumeration of the fixed DIR3 validation states an {@code AppResponsible}/
 * {@code AppAuthorized} assignment can be in, mirroring {@code StatusEnum}: each value maps to a
 * fixed, well-known {@code INV_LKUP_DIR3_STATUS} row id.
 *
 * @since 1.0.5
 */
@Getter
public enum Dir3ValidationStatus {

    /** The person's DIR3CAIB code matches the application's assigned administrative unit. */
    VALIDATED(1L),

    /** The person's DIR3CAIB code does not match (or could not be determined). */
    NOT_VALIDATED(2L),

    /** Did not match, but a ROLE_INV_SUPER user manually validated it anyway. */
    MANUAL(3L),

    /** The check does not apply (the person isn't Personal CAIB, or the application has no administrative unit assigned). */
    NOT_APPLY(4L);

    /** Persistent primary key matching the {@code INV_LKUP_DIR3_STATUS} row. */
    private final Long id;

    Dir3ValidationStatus(Long id) {
        this.id = id;
    }

    /**
     * Resolves the DIR3 validation state an {@code AppResponsible}/{@code AppAuthorized} assignment
     * should be given, from the DIR3 check result and the traits already known to the caller.
     * {@code checkResult} may be {@code null} when the caller attempted the check but it failed
     * (e.g. Soffid unavailable/timed out) - a failed/inconclusive check is treated the same as a
     * mismatch ({@link #NOT_VALIDATED}), never as a reason to fail the assignment itself.
     *
     * @param personalCaib whether the assigned person is Personal CAIB
     * @param admUnitCode the application's assigned administrative unit code, possibly blank
     * @param checkResult the result of {@code PersonService#checkDir3}, or {@code null} if that call failed
     * @return {@link #NOT_APPLY} when the check doesn't apply (external person, or no administrative
     * unit assigned); otherwise {@link #VALIDATED} or {@link #NOT_VALIDATED} depending on whether the
     * person's DIR3 matches
     */
    public static Dir3ValidationStatus resolve(boolean personalCaib, String admUnitCode, PersonDir3CheckOutputDTO checkResult) {
        if (!personalCaib || StringUtils.isBlank(admUnitCode)) {
            return NOT_APPLY;
        }
        return checkResult != null && checkResult.isMatches() ? VALIDATED : NOT_VALIDATED;
    }

    /**
     * Resolves the DIR3 validation state to record for a newly created assignment from the
     * tri-state match flag submitted by the caller (typically the front end, from a prior call to
     * the DIR3 check endpoint) - unlike {@link #resolve}, this trusts the submitted flag as-is
     * rather than recomputing it via a live Soffid call.
     *
     * @param matches the submitted match flag, or {@code null} when the check doesn't apply
     * (external person, or no administrative unit assigned)
     * @return {@link #NOT_APPLY} when {@code matches} is {@code null}; otherwise {@link #VALIDATED}
     * or {@link #NOT_VALIDATED}
     */
    public static Dir3ValidationStatus resolveFromInputFlag(Boolean matches) {
        if (matches == null) {
            return NOT_APPLY;
        }
        return matches ? VALIDATED : NOT_VALIDATED;
    }
}
