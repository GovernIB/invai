package es.caib.invai.back.ejb.catalog.admUnit;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.catalog.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.back.rest.dir3.Dir3CaibClient;
import es.caib.invai.back.rest.dir3.UnidadRest;
import es.caib.invai.back.service.facade.catalog.admUnit.AdmUnitService;
import es.caib.invai.back.service.mapper.catalog.admUnit.AdmUnitMapper;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Facade service implementation for Administrative Units (AdmUnit) — pure external reference data
 * mirrored live from DIR3CAIB, never created/edited/deleted locally. Handles hierarchical tree
 * browsing and department (Conselleria) derivation. The DIR3CAIB tree itself is cached and
 * proactively refreshed on the {@code invai-api-interna} side (see its {@code Dir3CaibTreeCache}),
 * which authenticates to DIR3CAIB with its own fixed service credentials rather than a forwarded
 * end-user token — so every call from here returns near-instantly regardless of how long a live
 * DIR3CAIB fetch itself would take.
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
     * Mapper converting a raw DIR3CAIB tree node into its outbound {@link AdmUnitOutputDTO}.
     */
    @Autowired
    private AdmUnitMapper admUnitMapper;

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
     * Resolves a single administrative unit by its DIR3CAIB code against the tree. Tolerant of
     * DIR3CAIB being unreachable: returns {@code null} rather than propagating the failure, since
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
            if (StringUtils.isBlank(admUnitCode)) {
                return null;
            }
            return getTreeByCode().get(admUnitCode);
        } catch (BusinessRuleException e) {
            log.warn("Facade: DIR3CAIB unavailable while resolving administrative unit by code: {}", admUnitCode);
            return null;
        }
    }

    /**
     * Bulk variant of {@link #resolveByCode}, resolving every code in a single DIR3CAIB tree fetch.
     *
     * @param admUnitCodes the DIR3CAIB codes to resolve; blank entries are ignored
     * @return the matching units keyed by code; a code with no match, or DIR3CAIB being
     * unreachable, is simply absent from the result rather than throwing
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, AdmUnitOutputDTO> getAdmUnitsByCodes(Collection<String> admUnitCodes) {
        Map<String, AdmUnitOutputDTO> treeByCode;
        try {
            treeByCode = getTreeByCode();
        } catch (BusinessRuleException e) {
            log.warn("Facade: DIR3CAIB unavailable while bulk-resolving administrative unit codes");
            return Map.of();
        }
        return admUnitCodes.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .map(treeByCode::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(AdmUnitOutputDTO::getCode, dto -> dto));
    }

    /**
     * Validates that {@code admUnitCode}, when provided, matches a unit in the live DIR3CAIB tree
     * at department (Conselleria) level or below. No local record is created or referenced — the
     * code is simply validated, the caller stores it as given.
     *
     * @param admUnitCode the DIR3CAIB code to validate, may be blank or {@code null}
     * @throws BusinessRuleException if {@code admUnitCode} is non-blank and has no match in
     * DIR3CAIB, matches a unit above department level, or DIR3CAIB cannot be reached (uses
     * {@link #getTreeByCode} directly rather than the null-swallowing {@link #resolveByCode}, so
     * an outage is reported as "unavailable" instead of being misreported as "code not found")
     */
    @Override
    @Transactional(readOnly = true)
    public void validateAdmUnitCode(String admUnitCode) {
        if (StringUtils.isBlank(admUnitCode)) {
            return;
        }
        AdmUnitOutputDTO admUnit = getTreeByCode().get(admUnitCode);
        if (admUnit == null) {
            throw new BusinessRuleException(Constants.ERR_ADMUNIT_NOT_FOUND);
        }
        if (admUnit.getLevel() == null || admUnit.getLevel() < departmentHierarchyLevel) {
            throw new BusinessRuleException(Constants.ERR_ADMUNIT_ABOVE_DEPARTMENT_LEVEL);
        }
    }

    /**
     * Resolves the DIR3CAIB codes of every unit in the tree whose name contains the given text
     * (case-insensitive).
     *
     * @param name the text to search for may be blank or {@code null}
     * @return the matching codes, or an empty list if {@code pattern} is blank
     * @throws BusinessRuleException if DIR3CAIB cannot be reached
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> findCodesByNameContaining(String name) {
        if (StringUtils.isBlank(name)) {
            return List.of();
        }
        String nameLowerCase = name.toLowerCase();
        return fetchTree().stream()
                .filter(dto -> dto.getName() != null && dto.getName().toLowerCase().contains(nameLowerCase))
                .map(AdmUnitOutputDTO::getCode)
                .toList();
    }

    /**
     * Lists every administrative unit at department (Conselleria) hierarchy level or below in the
     * DIR3CAIB tree, optionally narrowed down to those whose name or DIR3CAIB code contains
     * {@code search} (case-insensitive).
     *
     * @param search   free-text filter matched against each unit's name or code, may be blank or {@code null}
     * @param pageable pagination and sorting parameters, applied in-memory over the filtered list
     * @return a page of every eligible unit matching {@code search}
     * @throws BusinessRuleException if DIR3CAIB cannot be reached
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AdmUnitOutputDTO> getAll(String search, Pageable pageable) {
        String searchLowerCase = StringUtils.isBlank(search) ? null : search.toLowerCase();
        List<AdmUnitOutputDTO> eligibleUnits = fetchTree().stream()
                .filter(dto -> dto.getLevel() != null && dto.getLevel() >= departmentHierarchyLevel)
                .filter(dto -> searchLowerCase == null
                        || (dto.getName() != null && dto.getName().toLowerCase().contains(searchLowerCase))
                        || (dto.getCode() != null && dto.getCode().toLowerCase().contains(searchLowerCase)))
                .toList();
        return Utils.paginate(eligibleUnits, pageable);
    }

    @Override
    public int getDepartmentHierarchyLevel() {
        return departmentHierarchyLevel;
    }

    /**
     * Fetches the full DIR3CAIB tree, mapped to outbound DTOs. DIR3CAIB's
     * {@code obtenerArbolUnidades} operation has no native pagination (confirmed: extra
     * {@code page}/{@code size} query parameters are silently ignored and the full ~840-row tree is
     * always returned), so the whole tree is fetched and mapped in one go and then filtered/paginated
     * in memory by each caller.
     *
     * @return the current tree snapshot
     * @throws BusinessRuleException if DIR3CAIB cannot be reached
     */
    private List<AdmUnitOutputDTO> fetchTree() {
        List<UnidadRest> results = dir3CaibClient.getTree(rootAdmUnitCode, true);
        return results.stream().map(admUnitMapper::toResponse).toList();
    }

    /**
     * Indexes the cached tree by code, for O(1) lookups shared by {@link #resolveByCode} and
     * {@link #validateAdmUnitCode}.
     *
     * @return the cached tree keyed by DIR3CAIB code
     */
    private Map<String, AdmUnitOutputDTO> getTreeByCode() {
        return fetchTree().stream()
                .collect(Collectors.toMap(AdmUnitOutputDTO::getCode, dto -> dto, (first, duplicate) -> first));
    }
}
