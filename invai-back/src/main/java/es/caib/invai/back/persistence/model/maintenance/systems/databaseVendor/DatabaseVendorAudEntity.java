package es.caib.invai.back.persistence.model.maintenance.systems.databaseVendor;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_DATABASE_VENDOR} database table.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Entity
@Table(name = "INV_DATABASE_VENDOR_AUD")
public class DatabaseVendorAudEntity {

    /** Unique audit record surrogate identifier. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_database_vendor_aud_seq")
    @SequenceGenerator(
            name = "inv_database_vendor_aud_seq",
            sequenceName = "INV_DATABASE_VENDOR_AUD_SEQ",
            allocationSize = 1
    )
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    /** Identifier of the original {@code INV_DATABASE_VENDOR} record this snapshot audits. */
    @Column(name = "DATABASE_VENDOR_ID", nullable = false)
    private Long databaseVendorId;

    /** Vendor/type name at the time of this snapshot. */
    @Column(name = "NAME", length = 100, nullable = false)
    private String name;

    /** Default connection port at the time of this snapshot. */
    @Column(name = "DEFAULT_PORT")
    private Integer defaultPort;

    /** Original record creation timestamp. */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** Identity of the user who created the original record. */
    @Column(name = "CREATED_BY", length = 64)
    private String createdBy;

    /** Original record last update timestamp. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** Identity of the user who last updated the original record. */
    @Column(name = "UPDATED_BY", length = 64)
    private String updatedBy;

    /** Original record logical soft-deletion timestamp, if applicable. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Identity of the user who logically soft-deleted the original record, if applicable. */
    @Column(name = "DELETED_BY", length = 64)
    private String deletedBy;

    /** Type of mutation captured by this audit record (e.g. {@code INSERT}, {@code UPDATE}, {@code DELETE}). */
    @Column(name = "AUD_ACTION", length = 10, nullable = false)
    private String audAction;

    /** Timestamp at which this audit snapshot was recorded. */
    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    /** Identity of the user whose action triggered this audit snapshot. */
    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}
