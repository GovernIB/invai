package es.caib.invai.back.service.model.catalog.securityLevel;

import lombok.Getter;
import lombok.Setter;

/**
 * Rich domain dictionary object tracking security level lookup records assignable within
 * the Manteniments / Seguretat catalog.
 *
 * @since 1.0.4
 */
@Getter
@Setter
public class SecurityLevel {

    /** Unique database security level lookup index identifier key. */
    private Long id;

    /** Security level descriptive label string (e.g. "Alt"). */
    private String name;

    /** Alternate localized descriptive label matching the security level for Spanish. */
    private String nameEs;

}
