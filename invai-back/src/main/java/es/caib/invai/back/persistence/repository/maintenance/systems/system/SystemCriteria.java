package es.caib.invai.back.persistence.repository.maintenance.systems.system;

import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object (DTO) capturing search filters and evaluation metrics used
 * to build dynamic database queries targeting application registry assets within the
 * system inventory ecosystem.
 * <p>
 * Evaluates basic functional property tokens, host server relationship keys, and maps composite
 * global keyword filtering definitions across isolated system descriptor columns.
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
public class SystemCriteria {

    /**
     * Foreign key identity indicator matching the host {@code Server} record association.
     */
    private Long serverId;

    /**
     * Exact or partial deployment instance identifier descriptor filter.
     */
    private String instance;

    /**
     * Exact or partial version descriptor token filter.
     */
    private String version;

    /**
     * Master registry identity indicator managing active vs logical soft-deleted
     * runtime ecosystem state lifecycles.
     *
     * @see StatusEnum
     */
    private Long statusId;

    /**
     * Global text lookup variable cross-referencing keywords concurrently across all functional
     * columns ({@code name}, {@code instance}, and {@code version}) inside the programmatic criteria context.
     */
    private String search;

    /**
     * When present, restricts the result set to systems with no active (non-deleted)
     * {@code AppSystem} assignment linking them to this specific application's information
     * system database grouping. Scoped at the application level: a system already assigned to
     * a <em>different</em> application still appears in the result set.
     */
    private Long unassignedToInformationSystemDbId;
}
