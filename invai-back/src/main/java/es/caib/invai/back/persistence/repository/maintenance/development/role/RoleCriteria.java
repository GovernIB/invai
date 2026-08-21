package es.caib.invai.back.persistence.repository.maintenance.development.role;

import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object (DTO) capturing search filters and evaluation metrics used
 * to build dynamic database queries targeting the provider Role registry ecosystem.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleCriteria {

    /** Partial role name identification label token filter (Catalan). */
    private String name;

    /** Partial role name identification label token filter (Spanish). */
    private String nameEs;

    /**
     * Master registry identity indicator managing active vs logical soft-deleted runtime ecosystem state lifecycles.
     *
     * @see StatusEnum
     */
    private Long statusId;

    /**
     * Global text lookup variable cross-referencing keywords concurrently across all functional
     * columns ({@code name} and {@code nameEs}) inside the programmatic criteria context.
     */
    private String search;
}
