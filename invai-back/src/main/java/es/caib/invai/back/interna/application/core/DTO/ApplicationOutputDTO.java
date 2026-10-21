package es.caib.invai.back.interna.application.core.DTO;

import es.caib.invai.back.interna.catalog.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.back.interna.maintenance.general.category.DTO.CategoryOutputDTO;
import es.caib.invai.back.interna.maintenance.general.field.DTO.FieldOutputDTO;
import es.caib.invai.back.interna.maintenance.general.systemType.DTO.SystemTypeOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Simple outbound representation of an {@code Application}: only what the applications list shows
 * (code, prefix, name, category, system type, field and administrative unit) plus the identifier
 * needed to act on a row. Returned by {@code getAll}, {@code create}, {@code update} and {@code
 * reactivate}, and embedded wherever another resource refers to its application. The complete
 * representation, with every field, is {@link ApplicationDetailOutputDTO}, returned only by
 * {@code getById}.
 *
 * @since 1.0.1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationOutputDTO {

    /** Primary key of the application. */
    private Long id;

    /** Unique alphanumeric code identifying the application. */
    private String code;

    /** Short acronym used to prefix identifiers generated for this application. */
    private String prefix;

    /** Display name of the application. */
    private String name;

    /** The {@code Category} this application is classified under. */
    private CategoryOutputDTO category;

    /** The {@code SystemType} (infrastructure/architecture classification) of this application. */
    private SystemTypeOutputDTO systemType;

    /** The {@code Field} (business domain) this application belongs to. */
    private FieldOutputDTO field;

    /**
     * Administrative unit responsible for the application, resolved live from DIR3CAIB by the
     * stored {@code admUnitCode} — only the code is persisted, not this object. {@code null} when
     * no admin unit is linked, or the linked code has no current DIR3CAIB match.
     */
    private AdmUnitOutputDTO admUnit;
}
