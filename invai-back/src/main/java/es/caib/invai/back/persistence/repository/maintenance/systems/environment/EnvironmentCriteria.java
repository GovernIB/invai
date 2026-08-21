package es.caib.invai.back.persistence.repository.maintenance.systems.environment;

import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object (DTO) capturing search filters and evaluation metrics used
 * to build dynamic database queries targeting application registry assets within the
 * infrastructure environments ecosystem.
 * <p>
 * Evaluates basic functional property tokens, execution tracking metrics, and maps composite
 * global keyword filtering definitions across isolated multi-language columns.
 * </p>
 *
 * @author CAIB Architecture Team
 * @version 1.0.0
 * @since 2026-07-20
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentCriteria{

    /**
     * Exact or partial functional system identifier token filter (e.g., "PRO", "DEV").
     */
    private String code;

    /**
     * Localized primary language catalog designation string parameter (Catalan name descriptor).
     */
    private String name;

    /**
     * Localized secondary language translation matching descriptor (Spanish {@code nameEs}).
     */
    private String nameEs;

    /**
     * Master registry identity indicator managing active vs logical soft-deleted
     * runtime ecosystem state lifecycles.
     *
     * @see StatusEnum
     */
    private Long statusId;

    /**
     * Global text lookup variable cross-referencing keywords concurrently across all functional
     * columns ({@code code}, {@code name}, and {@code nameEs}) inside the programmatic criteria context.
     */
    private String search;
}