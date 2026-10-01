package es.caib.invai.back.service.facade.catalog.admUnit;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.catalog.admUnit.DTO.AdmUnitOutputDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
     * this for read paths that enrich a response with an already-valid record (e.g., an
     * {@code Application}'s admin unit), where a transient DIR3CAIB outage shouldn't block the
     * whole response. For write-path validation, use {@link #validateAdmUnitCode} instead.
     *
     * @param admUnitCode the DIR3CAIB code to resolve, may be blank or {@code null}
     * @return the matching unit, or {@code null} if {@code admUnitCode} is blank, not found in
     * the cached tree, or DIR3CAIB is unreachable
     */
    AdmUnitOutputDTO resolveByCode(String admUnitCode);

    /**
     * Bulk variant of {@link #resolveByCode}: resolves every administrative unit matching any of
     * the given DIR3CAIB codes from a single tree fetch, instead of one fetch per code. Intended
     * for read paths enriching a paginated listing (e.g. {@code Application}'s {@code getAll}),
     * where resolving one code at a time would mean one HTTP round-trip to DIR3CAIB per row.
     *
     * @param admUnitCodes the DIR3CAIB codes to resolve; blank entries are ignored
     * @return the matching units keyed by code; a code with no match, or DIR3CAIB being
     * unreachable, is simply absent from the result rather than throwing
     */
    Map<String, AdmUnitOutputDTO> getAdmUnitsByCodes(Collection<String> admUnitCodes);

    /**
     * Validates that {@code admUnitCode}, when provided, matches a unit in the live DIR3CAIB tree
     * at department (Conselleria) level or below — a caller can link to a department itself, or to
     * any of its descendants, but never to a unit above department level (i.e., the DIR3CAIB
     * root). Blank/{@code null} is valid: the administrative unit is optional. Unlike
     * {@link #resolveByCode}, a DIR3CAIB outage is NOT swallowed here: the failure propagates so
     * callers validating a client-supplied code can tell "code not found" apart from "couldn't
     * check".
     *
     * @param admUnitCode the DIR3CAIB code to validate, may be blank or {@code null}
     * @throws BusinessRuleException if {@code admUnitCode} is non-blank and has no match in
     * DIR3CAIB, matches a unit above department level, or DIR3CAIB cannot be reached
     */
    void validateAdmUnitCode(String admUnitCode);

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
     * Lists every administrative unit valid for direct selection on an {@code Application} — i.e.
     * every unit at department (Conselleria) hierarchy level or below (property
     * {@code es.caib.invai.dir3caib.department-hierarchy-level}), optionally narrowed down to those
     * whose name contains {@code search} (case-insensitive) — paginated in-memory over the cached
     * DIR3CAIB tree snapshot. Backs the single administrative-unit picker on the Application form:
     * a department and any of its descendants are equally valid choices, with no separate
     * department-first step.
     *
     * @param search   free-text filter matched against each unit's name, may be blank or {@code null}
     *                 to return every eligible unit
     * @param pageable pagination threshold constraints and structural sorting rules
     * @return a page wrapper grouping every eligible unit matching {@code search}
     */
    Page<AdmUnitOutputDTO> getAll(String search, Pageable pageable);

    /**
     * The configured DIR3CAIB hierarchy level identifying a department (Conselleria) — the
     * property {@code es.caib.invai.dir3caib.department-hierarchy-level}. Exposed so callers can
     * validate an administrative unit's position relative to department level (e.g. rejecting
     * anything above it) without duplicating that configuration.
     *
     * @return the department hierarchy level
     */
    int getDepartmentHierarchyLevel();
}
