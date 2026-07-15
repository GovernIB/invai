package es.caib.invai.api.persistence.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_ADM_UNIT} database table.
 * Unlike standard operational entities, this tracking row encapsulates metadata regarding
 * the exact database operations and execution contexts triggering the modification.
 *
 * @since 1.0.0
 */
@Getter
@Setter
@Entity
@Table(name = "INV_ADM_UNIT_AUD")
public class AdmUnitAudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_adm_unit_aud_seq")
    @SequenceGenerator(name = "inv_adm_unit_aud_seq", sequenceName = "INV_ADM_UNIT_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID")
    private Long audId;

    @Column(name = "ADM_UNIT_ID")
    private Long admUnitId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "NAME")
    private String name;

    @Column(name = "NAME_ES", length = 100)
    private String nameEs;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    @Column(name = "DELETED_BY")
    private String deletedBy;

    @Column(name = "AUD_ACTION")
    private String audAction;

    @Column(name = "AUDIT_DATE")
    private LocalDateTime auditDate;

    @Column(name = "AUDIT_USER")
    private String auditUser;
}