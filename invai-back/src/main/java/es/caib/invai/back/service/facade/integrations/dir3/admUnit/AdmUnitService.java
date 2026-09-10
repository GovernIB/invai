package es.caib.invai.back.service.facade.integrations.dir3.admUnit;

import es.caib.invai.back.interna.integrations.dir3.admUnit.DTO.AdmUnitOutputDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting Administrative Units ({@code AdmUnit}). Administrative units are pure external
 * reference data mirrored live from DIR3CAIB — nothing is created, edited, or deleted locally.
 * Acts as the primary application service boundary exposed to external API web controllers.
 *
 * @since 1.0.4
 */
public interface AdmUnitService {

    /**
     * Resolves a single administrative unit by its DIR3CAIB code against the cached tree. Tolerant
     * of DIR3CAIB being unreachable: returns {@code null} rather than propagating the failure. Use
     * this for read paths that enrich a response with an already-valid record (e.g. an
     * {@code Application}'s admin unit), where a transient DIR3CAIB outage shouldn't block the
     * whole response. For write-path validation, where a {@code null} result must be
     * distinguishable from "DIR3CAIB is unreachable", use {@link #resolveByCodeOrThrow} instead.
     *
     * @param admUnitCode the DIR3CAIB code to resolve, may be blank or {@code null}
     * @return the matching unit, or {@code null} if {@code admUnitCode} is blank, not found in
     * the cached tree, or DIR3CAIB is unreachable
     */
    AdmUnitOutputDTO resolveByCode(String admUnitCode);

    /**
     * Resolves a single administrative unit by its DIR3CAIB code against the cached tree, same as
     * {@link #resolveByCode} except a DIR3CAIB outage is NOT swallowed: the failure propagates so
     * callers validating a client-supplied code can tell "code not found" (returns {@code null})
     * apart from "couldn't check" (throws). Intended for write-path validation
     * (e.g. {@code Application} create/update), not for read paths enriching an existing record.
     *
     * @param admUnitCode the DIR3CAIB code to resolve, may be blank or {@code null}
     * @return the matching unit, or {@code null} if {@code admUnitCode} is blank or not found in
     * the cached tree
     * @throws es.caib.invai.back.exception.BusinessRuleException if DIR3CAIB cannot be reached
     */
    AdmUnitOutputDTO resolveByCodeOrThrow(String admUnitCode);

    /**
     * Resolves the DIR3CAIB codes of every administrative unit in the cached tree whose name
     * contains the given text (case-insensitive). Used to translate a free-text search term into a
     * set of codes that can be matched against the {@code admUnitCode} stored on an {@code
     * Application}, since admin unit names are no longer stored locally to search against directly.
     *
     * @param pattern the text to search for, may be blank or {@code null}
     * @return the matching codes, or an empty list if {@code pattern} is blank
     */
    List<String> findCodesByNameContaining(String pattern);

    /**
     * Derives the department (Conselleria) ancestor of a given DIR3CAIB administrative unit,
     * walking up its hierarchy (via each ancestor's parent code, against the cached DIR3CAIB tree
     * snapshot) until a unit with the configured department hierarchy level (property
     * {@code es.caib.invai.dir3caib.department-hierarchy-level}) is found.
     * Resolved live rather than stored, so a government reorganization is reflected immediately
     * without any data migration.
     *
     * @param admUnitCode the DIR3CAIB code of the administrative unit to resolve from, may be blank or {@code null}
     * @return the ancestor administrative unit at the department hierarchy level, or {@code null}
     * if {@code admUnitCode} is blank, not found in the DIR3CAIB tree, or has no such ancestor
     */
    AdmUnitOutputDTO resolveDepartment(String admUnitCode);

    /**
     * Lists every department (Conselleria) in the DIR3CAIB tree — i.e. every unit at the configured
     * department hierarchy level (property {@code es.caib.invai.dir3caib.department-hierarchy-level}) —
     * paginated in-memory over the cached DIR3CAIB tree snapshot. Backs the first step of the
     * Application form picker: choose a department before narrowing down to one of its
     * administrative units.
     *
     * @param pageable pagination threshold constraints and structural sorting rules
     * @return a page wrapper grouping every department-level unit
     */
    Page<AdmUnitOutputDTO> getDepartments(Pageable pageable);

    /**
     * Lists every descendant of a given department (Conselleria) at any depth in the DIR3CAIB tree
     * — the department itself is deliberately excluded, since an {@code Application} can never
     * link to a department directly, only to one of its descendants — paginated in-memory. Backs
     * the second step of the Application form picker, once a department has been chosen via
     * {@link #getDepartments}.
     *
     * @param departmentCode the DIR3CAIB code of the chosen department; may be blank or {@code null}
     * @param pageable       pagination threshold constraints and structural sorting rules
     * @return a page wrapper grouping the department's descendants (not the department itself), or
     * an empty page if {@code departmentCode} is blank or not found in the DIR3CAIB tree
     */
    Page<AdmUnitOutputDTO> getAdmUnitsByDepartment(String departmentCode, Pageable pageable);

    /**
     * The configured DIR3CAIB hierarchy level identifying a department (Conselleria) — the
     * property {@code es.caib.invai.dir3caib.department-hierarchy-level}. Exposed so callers can
     * validate an administrative unit's position relative to department level (e.g. rejecting a
     * department itself in favor of one of its descendants) without duplicating that
     * configuration.
     *
     * @return the department hierarchy level
     */
    int getDepartmentHierarchyLevel();
}
