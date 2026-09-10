package es.caib.invai.back.ejb.integrations.dir3.admUnit;

import es.caib.invai.back.ejb.application.core.ApplicationServiceFacadeBean;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.integrations.dir3.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.back.rest.dir3.Dir3CaibClient;
import es.caib.invai.back.rest.dir3.UnidadRest;
import es.caib.invai.back.service.facade.integrations.dir3.admUnit.AdmUnitService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Facade service implementation for Administrative Units (AdmUnit) — pure external reference data
 * mirrored live from DIR3CAIB, never created/edited/deleted locally. Handles hierarchical tree
 * browsing and department (Conselleria) derivation, backed by a short-lived in-memory cache of the
 * full DIR3CAIB tree.
 *
 * @since 1.0.4
 */
@Service
@Slf4j
@Transactional
public class AdmUnitServiceFacadeBean implements AdmUnitService {

    /**
     * Client used to fetch the organizational unit tree from the external DIR3CAIB directory.
     */
    @Autowired
    private Dir3CaibClient dir3CaibClient;

    /**
     * DIR3CAIB code of "Govern de les Illes Balears", used as the fixed root when browsing the
     * full administrative unit tree.
     */
    @Value("${es.caib.invai.dir3caib.root-adm-unit-code}")
    private String rootAdmUnitCode;

    /**
     * DIR3CAIB hierarchy level identifying a department (Conselleria, as opposed to a Direcció
     * General, Servei, or any deeper administrative unit level).
     */
    @Value("${es.caib.invai.dir3caib.department-hierarchy-level}")
    private int departmentHierarchyLevel;

    /**
     * Time-to-live applied to the cached DIR3CAIB tree snapshot before it is refetched.
     */
    private static final long TREE_CACHE_TTL_MILLIS = Duration.ofHours(1).toMillis();

    /**
     * Interval at which {@link #refreshCachedTreeInBackground()} proactively refetches the tree,
     * kept comfortably under {@link #TREE_CACHE_TTL_MILLIS} so the snapshot is renewed before it
     * ever goes stale from a request's point of view. Written as a literal (rather than via
     * {@link Duration}) because {@code @Scheduled}'s {@code fixedRate} requires a compile-time
     * constant expression.
     */
    private static final long TREE_CACHE_REFRESH_INTERVAL_MILLIS = 50 * 60 * 1000L;

    /**
     * Cached snapshot of the full DIR3CAIB tree; {@code null} until first requested.
     */
    private volatile List<AdmUnitOutputDTO> cachedTree;

    /**
     * Wall-clock time (millis) at which {@link #cachedTree} was last refreshed.
     */
    private volatile long cachedTreeAt;

    /**
     * Resolves a single administrative unit by its DIR3CAIB code against the cached tree. Tolerant
     * of DIR3CAIB being unreachable: returns {@code null} rather than propagating the failure, since
     * this is used to enrich an {@code Application} response that has its own valid data regardless
     * of DIR3CAIB's availability.
     *
     * @param admUnitCode the DIR3CAIB code to resolve may be blank or {@code null}
     * @return the matching unit, or {@code null} if blank, not found, or DIR3CAIB is unreachable
     */
    @Override
    @Transactional(readOnly = true)
    public AdmUnitOutputDTO resolveByCode(String admUnitCode) {
        try {
            return resolveByCodeOrThrow(admUnitCode);
        } catch (BusinessRuleException e) {
            log.warn("Facade: DIR3CAIB unavailable while resolving administrative unit by code: {}", admUnitCode);
            return null;
        }
    }

    /**
     * Resolves a single administrative unit by its DIR3CAIB code against the cached tree,
     * propagating a DIR3CAIB outage instead of swallowing it. See the interface Javadoc for when
     * to use this over {@link #resolveByCode}.
     *
     * @param admUnitCode the DIR3CAIB code to resolve may be blank or {@code null}
     * @return the matching unit, or {@code null} if blank or not found
     * @throws BusinessRuleException if DIR3CAIB cannot be reached
     */
    @Override
    @Transactional(readOnly = true)
    public AdmUnitOutputDTO resolveByCodeOrThrow(String admUnitCode) {
        if (StringUtils.isBlank(admUnitCode)) {
            return null;
        }
        return getTreeByCode().get(admUnitCode);
    }

    /**
     * Resolves the DIR3CAIB codes of every unit in the cached tree whose name contains the given
     * text (case-insensitive).
     *
     * @param name the text to search for may be blank or {@code null}
     * @return the matching codes, or an empty list if {@code pattern} is blank
     * @throws BusinessRuleException if DIR3CAIB cannot be reached and no cached snapshot is available
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> findCodesByNameContaining(String name) {
        if (StringUtils.isBlank(name)) {
            return List.of();
        }
        String nameLowerCase = name.toLowerCase();
        return getCachedTree().stream()
                .filter(dto -> dto.getName() != null && dto.getName().toLowerCase().contains(nameLowerCase))
                .map(AdmUnitOutputDTO::getCode)
                .toList();
    }

    /**
     * Derives the department (Conselleria) ancestor of a given DIR3CAIB administrative unit by
     * walking up the cached tree's parent-code links, starting from the unit itself, until a unit
     * at {@link #departmentHierarchyLevel} is reached. Tolerant of DIR3CAIB being unreachable:
     * returns {@code null} rather than propagating the failure, for the same reason as
     * {@link #resolveByCode}.
     *
     * @param departmentCode the DIR3CAIB code to resolve from may be blank or {@code null}
     * @return the ancestor at the department level, or {@code null} if {@code admUnitCode} is
     * blank, not found in the cached tree, has no such ancestor, or DIR3CAIB is unreachable
     */
    @Override
    @Transactional(readOnly = true)
    public AdmUnitOutputDTO resolveDepartment(String departmentCode) {
        if (StringUtils.isBlank(departmentCode)) {
            return null;
        }
        Map<String, AdmUnitOutputDTO> treeByCode;
        try {
            treeByCode = getTreeByCode();
        } catch (BusinessRuleException e) {
            log.warn("Facade: DIR3CAIB unavailable while resolving department for administrative unit code: {}", departmentCode);
            return null;
        }

        return Stream.iterate(treeByCode.get(departmentCode), Objects::nonNull, current -> treeByCode.get(current.getParentCode()))
                .filter(current -> current.getLevel() != null && current.getLevel() == departmentHierarchyLevel)
                .findFirst()
                .orElse(null);
    }

    /**
     * Lists every department (Conselleria) in the cached DIR3CAIB tree.
     *
     * @param pageable pagination and sorting parameters, applied in-memory over the filtered list
     * @return a page of every unit at the department hierarchy level
     * @throws BusinessRuleException if DIR3CAIB cannot be reached and no cached snapshot is available
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AdmUnitOutputDTO> getDepartments(Pageable pageable) {
        List<AdmUnitOutputDTO> departments = getCachedTree().stream()
                .filter(dto -> dto.getLevel() != null && dto.getLevel() == departmentHierarchyLevel)
                .toList();
        return paginate(departments, pageable);
    }

    /**
     * Lists every descendant of the department identified by {@code departmentCode} at any depth —
     * the department itself is deliberately excluded, since an {@code Application} can never link
     * to a department directly (see {@link ApplicationServiceFacadeBean}'s {@code admUnitCode}
     * validation), only to one of its descendants. A unit qualifies as a descendant by walking up
     * its {@code parentCode} chain (the same traversal {@link #resolveDepartment} uses) until either
     * {@code departmentCode} is found or the chain runs out.
     *
     * @param departmentCode the DIR3CAIB code of the chosen department may be blank or {@code null}
     * @param pageable       pagination and sorting parameters, applied in-memory over the resolved subtree
     * @return a page of the department's descendants (not the department itself), or an empty page
     * when {@code departmentCode} is blank or not found in the cached tree
     * @throws BusinessRuleException if DIR3CAIB cannot be reached and no cached snapshot is available
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AdmUnitOutputDTO> getAdmUnitsByDepartment(String departmentCode, Pageable pageable) {
        if (StringUtils.isBlank(departmentCode)) {
            return paginate(List.of(), pageable);
        }

        Map<String, AdmUnitOutputDTO> treeByCode = getTreeByCode();
        if (!treeByCode.containsKey(departmentCode)) {
            return paginate(List.of(), pageable);
        }

        List<AdmUnitOutputDTO> descendants = getCachedTree().stream()
                .filter(unit -> isDescendantOf(unit, departmentCode, treeByCode))
                .toList();
        return paginate(descendants, pageable);
    }

    /**
     * Whether {@code unit} descends from {@code departmentCode} at any depth, by walking up its
     * {@code parentCode} chain. A unit is never considered a descendant of itself.
     *
     * @param currentUnit    the candidate unit to check
     * @param departmentCode the DIR3CAIB code of the department to check against
     * @param treeByCode     the cached tree indexed by code, used to walk from a parent code to its unit
     * @return {@code true} if {@code departmentCode} is found among {@code unit}'s ancestors
     */
    private boolean isDescendantOf(AdmUnitOutputDTO currentUnit, String departmentCode, Map<String, AdmUnitOutputDTO> treeByCode) {
        return Stream.iterate(currentUnit, Objects::nonNull, i -> treeByCode.get(i.getParentCode()))
                .anyMatch(current -> departmentCode.equals(current.getParentCode()));
    }

    @Override
    public int getDepartmentHierarchyLevel() {
        return departmentHierarchyLevel;
    }

    /**
     * Proactively refetches the DIR3CAIB tree in the background, ahead of its TTL expiry, so that
     * {@link #getCachedTree()} almost never has to block a request thread on a live DIR3CAIB call —
     * this runs on Spring's own scheduler thread, never on a request-handling thread. Fires once
     * immediately at startup (also serving as cache warm-up) and then every
     * {@link #TREE_CACHE_REFRESH_INTERVAL_MILLIS}. Failures are logged and swallowed: the previous
     * snapshot (if any) is kept, and {@link #getCachedTree()} still falls back to a synchronous
     * refresh on true cache-miss.
     */
    @Scheduled(initialDelay = 0, fixedRate = TREE_CACHE_REFRESH_INTERVAL_MILLIS)
    public void refreshCachedTreeInBackground() {
        try {
            refreshCachedTree();
        } catch (BusinessRuleException e) {
            log.warn("Facade: Scheduled background refresh of the DIR3CAIB administrative unit tree failed, keeping previous cached snapshot if any", e);
        }
    }

    /**
     * Returns the cached DIR3CAIB tree snapshot, refreshing it from DIR3CAIB first if missing or
     * past its TTL. In normal operation this is a plain in-memory read, since
     * {@link #refreshCachedTreeInBackground()} keeps the snapshot from ever going stale; the
     * synchronous refresh below only actually calls DIR3CAIB as a fallback (e.g., the first request
     * arriving before the background job has completed its initial warm-up).
     *
     * @return the current tree snapshot
     * @throws BusinessRuleException if a refresh is due and DIR3CAIB cannot be reached
     */
    private List<AdmUnitOutputDTO> getCachedTree() {
        List<AdmUnitOutputDTO> snapshot = cachedTree;
        if (snapshot != null && (System.currentTimeMillis() - cachedTreeAt) < TREE_CACHE_TTL_MILLIS) {
            return snapshot;
        }
        return refreshCachedTree();
    }

    /**
     * Fetches a fresh DIR3CAIB tree snapshot and stores it, unless another thread already refreshed
     * it while this one was waiting on the lock. DIR3CAIB's {@code obtenerArbolUnidades} operation
     * has no native pagination (confirmed: extra {@code page}/{@code size} query parameters are
     * silently ignored and the full ~840-row tree is always returned), so the whole tree is fetched
     * and cached in memory here rather than queried page by page.
     *
     * @return the current (possibly just-refreshed) tree snapshot
     * @throws BusinessRuleException if DIR3CAIB cannot be reached
     */
    private synchronized List<AdmUnitOutputDTO> refreshCachedTree() {
        if (cachedTree != null && (System.currentTimeMillis() - cachedTreeAt) < TREE_CACHE_TTL_MILLIS) {
            return cachedTree;
        }
        log.debug("Facade: Refreshing cached DIR3CAIB administrative unit tree rooted at: {}", rootAdmUnitCode);
        List<UnidadRest> results = dir3CaibClient.getTree(rootAdmUnitCode, true);
        cachedTree = results.stream().map(this::toAdmUnitOutputDTO).toList();
        cachedTreeAt = System.currentTimeMillis();
        return cachedTree;
    }

    /**
     * Indexes the cached tree by code, for O(1) lookups shared by {@link #resolveByCode} and
     * {@link #resolveDepartment}.
     *
     * @return the cached tree keyed by DIR3CAIB code
     */
    private Map<String, AdmUnitOutputDTO> getTreeByCode() {
        return getCachedTree().stream()
                .collect(Collectors.toMap(AdmUnitOutputDTO::getCode, dto -> dto, (first, duplicate) -> first));
    }

    /**
     * Maps a raw DIR3CAIB tree node onto an {@link AdmUnitOutputDTO}, carrying its parent code and
     * hierarchy depth.
     *
     * @param unidadRest the raw DIR3CAIB tree node to convert
     * @return the mapped output DTO
     */
    private AdmUnitOutputDTO toAdmUnitOutputDTO(UnidadRest unidadRest) {
        AdmUnitOutputDTO dto = new AdmUnitOutputDTO();
        dto.setCode(unidadRest.getCodigo());
        dto.setName(unidadRest.getDenominacionCooficial() != null ? unidadRest.getDenominacionCooficial() : unidadRest.getDenominacion());
        dto.setParentCode(unidadRest.getCodUnidadSuperior());
        dto.setLevel(unidadRest.getNivelJerarquico());
        return dto;
    }

    /**
     * Slices an in-memory row list into the requested page, since the DIR3CAIB-backed listing has
     * no backing query to paginate at the database level.
     *
     * @param rows     the full, already-filtered row list
     * @param pageable the pagination parameters to apply
     * @return the requested page of rows
     */
    private Page<AdmUnitOutputDTO> paginate(List<AdmUnitOutputDTO> rows, Pageable pageable) {
        int start = (int) Math.min(pageable.getOffset(), rows.size());
        int end = Math.min(start + pageable.getPageSize(), rows.size());
        return new PageImpl<>(rows.subList(start, end), pageable, rows.size());
    }
}
