package es.caib.invai.back.service.model.catalog.ensSubject;

import lombok.Getter;
import lombok.Setter;

/**
 * Rich domain dictionary object tracking ENS subjection lookup records assignable within
 * the Manteniments / Seguretat catalog.
 *
 * @since 1.0.4
 */
@Getter
@Setter
public class EnsSubject {

    /** Unique database ENS subjection lookup index identifier key. */
    private Long id;

    /** ENS subjection descriptive label string (e.g. "Sí"). */
    private String name;

    /** Alternate localized descriptive label matching the ENS subjection for Spanish. */
    private String nameEs;

}
