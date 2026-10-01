package es.caib.invai.back.persistence.model.application.responsibleAuthorized.dir3;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.catalog.dir3Status.LkupDir3StatusEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * JPA entity tracking the DIR3 validation state of a single {@code AppResponsible}/
 * {@code AppAuthorized} assignment, mapped onto the {@code INV_DIR3_VALIDATION} table. Kept as its
 * own first-class entity (rather than flat columns on the assignment) so it has its own dedicated
 * audit trail ({@code INV_DIR3_VALIDATION_AUD}), independent of unrelated field changes
 * (job title, observation, etc.) on the owning assignment.
 *
 * @since 1.0.5
 */
@Entity
@Table(name = "INV_DIR3_VALIDATION")
@Getter
@Setter
public class Dir3ValidationEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Primary key unique identifier. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_DIR3_VALIDATION_SEQ")
    @SequenceGenerator(name = "INV_DIR3_VALIDATION_SEQ", sequenceName = "INV_DIR3_VALIDATION_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Current DIR3 validation state. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DIR3_STATUS_ID", nullable = false)
    private LkupDir3StatusEntity dir3Status;

    /** Timestamp of the manual DIR3 validation, populated only when {@link #dir3Status} is MANUAL. */
    @Column(name = "MANUAL_VALIDATED_AT")
    private LocalDateTime manualValidatedAt;

    /** User who performed the manual DIR3 validation. */
    @Column(name = "MANUAL_VALIDATED_BY", length = 64)
    private String manualValidatedBy;

    /** Free-text reason given when manually validating, populated only when {@link #dir3Status} is MANUAL. */
    @Lob
    @Column(name = "REASON")
    private String reason;
}
