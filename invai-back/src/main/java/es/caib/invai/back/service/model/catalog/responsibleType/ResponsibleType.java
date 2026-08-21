package es.caib.invai.back.service.model.catalog.responsibleType;

import lombok.Getter;
import lombok.Setter;

/**
 * Rich domain dictionary object tracking responsible type lookup records assignable within
 * the Manteniments / Responsables catalog.
 *
 * @since 1.0.3
 */
@Getter
@Setter
public class ResponsibleType {

    /** Unique database responsible type lookup index identifier key. */
    private Long id;

    /** Responsible type descriptive label string (e.g. "Responsable funcional"). */
    private String name;

    /** Alternate localized descriptive label matching the responsible type for Spanish. */
    private String nameEs;

    /** Whether this responsible type may only be held by Personal CAIB persons. */
    private boolean requiresPersonalCaib;
}
