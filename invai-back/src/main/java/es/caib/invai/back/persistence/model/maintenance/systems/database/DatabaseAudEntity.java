package es.caib.invai.back.persistence.model.maintenance.systems.database;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA historical snapshot audit entity capturing transactional database mutations
 * on the {@code INV_DATABASE} table.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_DATABASE_AUD")
@Getter
@Setter
public class DatabaseAudEntity {

    /**
     * Unique audit record surrogate identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_DATABASE_AUD_SEQ")
    @SequenceGenerator(name = "INV_DATABASE_AUD_SEQ", sequenceName = "INV_DATABASE_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    /** Identifier of the original {@code INV_DATABASE} record this snapshot audits. */
    @Column(name = "DATABASE_ID", nullable = false)
    private Long databaseId;

    /** Identifier of the host server linked at the time of this snapshot. */
    @Column(name = "SERVER_ID", nullable = false)
    private Long serverId;

    /** Database service schema identifier at the time of this snapshot. */
    @Column(name = "SERVICE", nullable = false)
    private String service;

    /** Listening connection port at the time of this snapshot. */
    @Column(name = "PORT")
    private Integer port;

    /** Identifier of the database vendor/type catalog entry at the time of this snapshot. */
    @Column(name = "DATABASE_TYPE")
    private Long databaseTypeId;

    /** Administrative description text at the time of this snapshot. */
    @Column(name = "DESCRIPTION")
    private String description;

    /** Original record creation timestamp. */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** Identity of the user who created the original record. */
    @Column(name = "CREATED_BY")
    private String createdBy;

    /** Original record last update timestamp. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** Identity of the user who last updated the original record. */
    @Column(name = "UPDATED_BY")
    private String updatedBy;

    /** Original record logical soft-deletion timestamp, if applicable. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Identity of the user who logically soft-deleted the original record, if applicable. */
    @Column(name = "DELETED_BY")
    private String deletedBy;

    /** Type of mutation captured by this audit record (e.g. {@code INSERT}, {@code UPDATE}, {@code DELETE}). */
    @Column(name = "AUD_ACTION", nullable = false, length = 10)
    private String audAction;

    /** Timestamp at which this audit snapshot was recorded. */
    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    /** Identity of the user whose action triggered this audit snapshot. */
    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}