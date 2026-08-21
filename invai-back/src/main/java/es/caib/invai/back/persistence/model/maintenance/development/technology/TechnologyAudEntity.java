package es.caib.invai.back.persistence.model.maintenance.development.technology;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_TECHNOLOGY} database table.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Entity
@Table(name = "INV_TECHNOLOGY_AUD")
public class TechnologyAudEntity {

    /** Unique identification pointer of this audit snapshot row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_technology_aud_seq")
    @SequenceGenerator(name = "inv_technology_aud_seq", sequenceName = "INV_TECHNOLOGY_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID")
    private Long audId;

    /** Identifier of the {@code INV_TECHNOLOGY} record this snapshot refers to. */
    @Column(name = "TECHNOLOGY_ID")
    private Long technologyId;

    /** Name of the technology at the time of the audited change. */
    @Column(name = "NAME")
    private String name;

    /** Identifier of the architecture layer the technology belonged to at the time of the audited change. */
    @Column(name = "LAYER_ID")
    private Long layerId;

    /** Creation timestamp captured from the audited record. */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** Username that created the audited record. */
    @Column(name = "CREATED_BY")
    private String createdBy;

    /** Last update timestamp captured from the audited record. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** Username that last updated the audited record. */
    @Column(name = "UPDATED_BY")
    private String updatedBy;

    /** Logical deletion timestamp captured from the audited record. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Username that logically deleted the audited record. */
    @Column(name = "DELETED_BY")
    private String deletedBy;

    /** Type of operation that triggered this audit snapshot (e.g. INSERT, UPDATE, DELETE). */
    @Column(name = "AUD_ACTION")
    private String audAction;

    /** Timestamp at which this audit snapshot was recorded. */
    @Column(name = "AUDIT_DATE")
    private LocalDateTime auditDate;

    /** Username that performed the action captured by this audit snapshot. */
    @Column(name = "AUDIT_USER")
    private String auditUser;
}
