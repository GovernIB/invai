package es.caib.invai.api.persistence.repository.field;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

/**
 * Data Transfer Object (DTO) capturing search filters and evaluation metrics used
 * to build dynamic database queries targeting operational business Fields within the
 * functional area registry ecosystem.
 * <p>
 * Evaluates basic functional property tokens, execution tracking metrics, and maps composite
 * global keyword filtering definitions across isolated multi-language columns.
 * </p>
 *
 * @author CAIB Architecture Team
 * @version 1.0.0
 * @since 2026-07-28
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FieldCriteria{

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
     * @see es.caib.invai.api.service.model.StatusEnum
     */
    private Long statusId;

    /**
     * Global text lookup variable cross-referencing keywords concurrently across all functional
     * columns ({@code name} and {@code nameEs}) inside the programmatic criteria context.
     */
    private String search;
}
