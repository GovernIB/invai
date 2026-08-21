package es.caib.invai.back.service.model.maintenance.responsible.person;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import es.caib.invai.back.service.model.maintenance.responsible.company.Company;

/**
 * Domain Model object representing pure person infrastructure logic schemas, linked to a company.
 *
 * @since 1.0.3
 */
@Getter
@Setter
public class Person {

    /** Unique identifier. */
    private Long id;
    /** Associated company, or {@code null} when this person is flagged as {@code personalCaib}. */
    private Company company;
    /** Person's first name. */
    private String firstName;
    /** Person's last name(s). */
    private String lastName;
    /** Person's contact email address. */
    private String email;
    /** Flags this person as CAIB internal staff, resolved via the DIR3 directory instead of a {@link Company}. */
    private boolean personalCaib;

    /** Timestamp when this record was created. */
    private LocalDateTime createdAt;
    /** User who created this record. */
    private String createdBy;
    /** Timestamp of the last update to this record. */
    private LocalDateTime updatedAt;
    /** User who last updated this record. */
    private String updatedBy;
    /** Timestamp when this record was soft-deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
    /** User who soft-deleted this record. */
    private String deletedBy;
}
