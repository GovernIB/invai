package es.caib.invai.api.service.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Primary Aggregate Root domain model tracking software assets registered
 * within the enterprise inventory system framework.
 *
 * @since 1.0.0
 */
@Getter
@Setter
public class Application {

    /** Unique application asset entry sequence sequence key. */
    private Long id;

    /** Standardized corporate structural designation code identifier. */
    private String code;

    /** Functional routing prefix parameter attached to system actions. */
    private String prefix;

    /** Public identification label descriptive text name. */
    private String name;

    /** Scope definition narrative detailing application usage context targets. */
    private String description;

    /** Lifecycle evaluation deadline timestamp tracking system retirement schedules. */
    private LocalDateTime expirationDate;

    /** Structural taxonomy catalog category classification node assignment. */
    private Category category;

    /** Core architectural execution infrastructure archetype classification tag. */
    private SystemType systemType;

    /** Functional area assignment boundary tracking business domain alignment. */
    private Field field;

    /** Responsible structural corporate administrative node identity wrapper. */
    private AdmUnit admUnit;

    /** Supervising governance working commission evaluation group connection. */
    private Commission csCommission;

    /** Current operational asset lifecycle state enumeration status token. */
    private StatusEnum status;

    /** Timestamp marking the asset's validation timeline soft-deletion target point. */
    private LocalDateTime createdAt;

    private String createdBy;

    /** Timestamp marking the asset's validation timeline soft-deletion target point. */
    private LocalDateTime updatedAt;

    private String updatedBy;

    /** Timestamp marking the asset's validation timeline soft-deletion target point. */
    private LocalDateTime deletedAt;

    /** Identity auditing identifier tracing who performed the application record deactivation. */
    private String deletedBy;
}