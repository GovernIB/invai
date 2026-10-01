package es.caib.invai.back.interna.maintenance.responsible.person.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Outbound payload reporting whether a person belongs to a given DIR3CAIB administrative unit,
 * feeding an advisory (non-blocking) warning on the "assign responsible/authorized" screen - a
 * negative result never blocks the assignment itself, the frontend decides whether/how to surface
 * a warning based on this response. A genuine Soffid failure, however, is NOT represented in this
 * DTO at all: it propagates as an exception instead - {@code SoffidClientException} (mapped to the
 * usual generic {@code BusinessRuleException} error response) for any Soffid error, or
 * {@code SoffidTimeoutException} (mapped to HTTP 504) specifically for a timeout.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonDir3CheckOutputDTO {

    /**
     * The short internal name of the person's Soffid {@code primaryGroup} (e.g. {@code "sgaip"}),
     * or {@code null} when it couldn't be determined (the person isn't Personal CAIB, has no
     * matching Soffid account, or has no primary group) or when {@link #admUnitCode} itself is
     * blank. Never a DIR3CAIB code itself - see {@link #groupDir3} for that.
     */
    private String personGroup;

    /**
     * The DIR3CAIB code registered on {@link #personGroup}'s extension attributes, or {@code null}
     * when it legitimately couldn't be determined ({@link #personGroup} itself is {@code null}, or
     * that group carries no {@code DIR3} attribute).
     */
    private String groupDir3;

    /** The DIR3CAIB code being checked against (the application's assigned administrative unit), echoed back as given. */
    private String admUnitCode;

    /** Whether {@link #groupDir3} exactly matches {@link #admUnitCode}. */
    private boolean matches;
}
