package es.caib.invai.back.interna.maintenance.responsible.person.DTO;

import es.caib.invai.back.interna.maintenance.responsible.company.DTO.CompanyOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) wrapping outgoing person state metadata payload models.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PersonOutputDTO {

    /** Primary persistent storage unique sequence row reference. */
    private Long id;

    /** Resolved company snapshot. */
    private CompanyOutputDTO company;

    /** Person first name or identification label. */
    private String firstName;

    /** Person last name(s). */
    private String lastName;

    /** Contact e-mail address of the person. */
    private String email;

    /** Whether this person is CAIB staff (sourced via the future DIR3 integration), rather than an external company contact. */
    private boolean personalCaib;

    /** Timestamp at which the person was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
