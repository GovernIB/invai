package es.caib.invai.back.persistence.model.maintenance.responsible.person;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_PERSON} database table.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@Entity
@Table(name = "INV_PERSON_AUD")
public class PersonAudEntity {

    /** Primary key of this audit snapshot row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_person_aud_seq")
    @SequenceGenerator(
            name = "inv_person_aud_seq",
            sequenceName = "INV_PERSON_AUD_SEQ",
            allocationSize = 1
    )
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    /** Identifier of the original {@code INV_PERSON} record this snapshot refers to. */
    @Column(name = "PERSON_ID", nullable = false)
    private Long personId;

    /** Snapshot of the company identifier at the time of the audited change. */
    @Column(name = "COMPANY_ID")
    private Long companyId;

    /** Snapshot of the person's first name at the time of the audited change. */
    @Column(name = "FIRST_NAME", length = 150, nullable = false)
    private String firstName;

    /** Snapshot of the person's last name at the time of the audited change. */
    @Column(name = "LAST_NAME", length = 150, nullable = false)
    private String lastName;

    /** Snapshot of the person's email at the time of the audited change. */
    @Column(name = "EMAIL", length = 150, nullable = false)
    private String email;

    /** Snapshot of the CAIB staff flag at the time of the audited change. */
    @Column(name = "IS_PERSONAL_CAIB")
    private Boolean personalCaib;

    /** Timestamp at which the original record was created. */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** Username of the user who created the original record. */
    @Column(name = "CREATED_BY", length = 64)
    private String createdBy;

    /** Timestamp of the last update to the original record. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** Username of the user who last updated the original record. */
    @Column(name = "UPDATED_BY", length = 64)
    private String updatedBy;

    /** Timestamp at which the original record was logically deleted, or {@code null} if still active. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Username of the user who logically deleted the original record. */
    @Column(name = "DELETED_BY", length = 64)
    private String deletedBy;

    /** Type of audited action that produced this snapshot (e.g. insert, update, delete). */
    @Column(name = "AUD_ACTION", length = 10, nullable = false)
    private String audAction;

    /** Timestamp at which this audit snapshot was recorded. */
    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    /** Username of the user who triggered the audited action. */
    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}
