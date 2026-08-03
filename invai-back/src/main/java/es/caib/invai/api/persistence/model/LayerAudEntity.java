package es.caib.invai.api.persistence.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_LAYER} database table.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Entity
@Table(name = "INV_LAYER_AUD")
public class LayerAudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_layer_aud_seq")
    @SequenceGenerator(name = "inv_layer_aud_seq", sequenceName = "INV_LAYER_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID")
    private Long audId;

    @Column(name = "LAYER_ID")
    private Long layerId;

    @Column(name = "NAME")
    private String name;

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
