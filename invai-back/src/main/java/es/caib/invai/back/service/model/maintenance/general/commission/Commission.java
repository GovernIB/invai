package es.caib.invai.back.service.model.maintenance.general.commission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Pure domain business logic model defining organizational oversight and evaluation Working Commissions
 * managing architectural review boards.
 * <p>
 * This entity acts as an isolated intermediate boundary representation decoupling presentation DTOs
 * from the structural relational JPA database entity schemas.
 * </p>
 *
 * @since 1.0.1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Commission {

    /**
     * Unique administrative evaluation group primary index identity.
     */
    private Long id;

    /**
     * Official organizational department committee layout string designation (typically Catalan).
     */
    private String name;

    /**
     * Translated committee variant layout naming explicitly matching Spanish context configurations.
     */
    private String nameEs;

    /**
     * Unique formal administrative tracking code linked to the commission establishment dossier.
     */
    private String expedientNumber;

    /**
     * The exact calendar day milestone marking the legal or technical setup validation.
     */
    private CommissionType commissionType;

    /**
     * Categorization rank defining the authority scope of the committee panel.
     */
    private LocalDate approvalDate;

    /** Timestamp marking when the commission was created. */
    private LocalDateTime createdAt;
    /** Username (or system fallback identifier) of the actor who created the commission. */
    private String createdBy;
    /** Timestamp marking the most recent update to the commission, or {@code null} if never updated. */
    private LocalDateTime updatedAt;
    /** Username (or system fallback identifier) of the actor who last updated the commission. */
    private String updatedBy;
    /** Timestamp marking when the commission was soft-deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
    /** Username (or system fallback identifier) of the actor who soft-deleted the commission. */
    private String deletedBy;
}