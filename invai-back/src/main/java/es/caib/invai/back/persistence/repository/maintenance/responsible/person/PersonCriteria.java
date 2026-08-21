package es.caib.invai.back.persistence.repository.maintenance.responsible.person;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) capturing search filters used to build dynamic database queries
 * targeting person catalog records.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonCriteria {

    /** Partial person first name identification label token filter. */
    private String firstName;

    /** Partial person last name identification label token filter. */
    private String lastName;

    /** Partial e-mail identification label token filter. */
    private String email;

    /** Foreign key filter matching the target company. */
    private Long companyId;

    /** Master registry identity indicator managing active vs logical soft-deleted state lifecycles. */
    private Long statusId;

    /** Global text lookup variable cross-referencing the {@code firstName}, {@code lastName} and {@code email} columns. */
    private String search;

    /**
     * Identifier to omit from the result set. Used by pickers that must not offer back the
     * already-selected person (e.g. the "persona nova" selector on the role-transfer screen,
     * which must exclude the currently selected "persona actual").
     */
    private Long excludeId;
}
