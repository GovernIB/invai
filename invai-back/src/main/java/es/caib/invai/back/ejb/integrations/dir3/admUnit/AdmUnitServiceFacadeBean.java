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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Facade service implementation for Administrative Units (AdmUnit) — pure external reference data
 * mirrored live from DIR3CAIB, never created/edited/deleted locally. Handles hierarchical tree
 * browsing and department (Conselleria) derivation, fetching the full DIR3CAIB tree fresh on every
 * call: no in-memory caching, since the previous background pre-warming job had no authenticated
 * user session to forward to {@code invai-api-interna} and always failed with 401.
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
            return resolveByCodeOrThrow(admUnitCode);
        } catch (BusinessRuleException e) {
            log.warn("Facade: DIR3CAIB unavailable while resolving administrative unit by code: {}", admUnitCode);
            return null;
        }
    }

    /**
     * Resolves a single administrative unit by its DIR3CAIB code against the tree, propagating a
     * DIR3CAIB outage instead of swallowing it. See the interface Javadoc for when
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
     * Derives the department (Conselleria) ancestor of a given DIR3CAIB administrative unit by
     * walking up the tree's parent-code links, starting from the unit itself, until a unit at
     * {@link #departmentHierarchyLevel} is reached. Tolerant of DIR3CAIB being unreachable:
     * returns {@code null} rather than propagating the failure, for the same reason as
     * {@link #resolveByCode}.
     *
     * @param departmentCode the DIR3CAIB code to resolve from may be blank or {@code null}
     * @return the ancestor at the department level, or {@code null} if {@code admUnitCode} is
     * blank, not found in the tree, has no such ancestor, or DIR3CAIB is unreachable
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
     * Lists every department (Conselleria) in the DIR3CAIB tree.
     *
     * @param pageable pagination and sorting parameters, applied in-memory over the filtered list
     * @return a page of every unit at the department hierarchy level
     * @throws BusinessRuleException if DIR3CAIB cannot be reached
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AdmUnitOutputDTO> getDepartments(Pageable pageable) {
        List<AdmUnitOutputDTO> departments = fetchTree().stream()
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
     * when {@code departmentCode} is blank or not found in the tree
     * @throws BusinessRuleException if DIR3CAIB cannot be reached
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

        List<AdmUnitOutputDTO> descendants = fetchTree().stream()
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
     * @param treeByCode     the tree indexed by code, used to walk from a parent code to its unit
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
     * Fetches the full DIR3CAIB tree fresh, on every call. DIR3CAIB's {@code obtenerArbolUnidades}
     * operation has no native pagination (confirmed: extra {@code page}/{@code size} query
     * parameters are silently ignored and the full ~840-row tree is always returned), so the whole
     * tree is fetched and mapped here on each request rather than queried page by page.
     *
     * @return the current tree, freshly fetched from DIR3CAIB
     * @throws BusinessRuleException if DIR3CAIB cannot be reached
     */
    private List<AdmUnitOutputDTO> fetchTree() {
        List<UnidadRest> results = dir3CaibClient.getTree(rootAdmUnitCode, true);
        return results.stream().map(this::toAdmUnitOutputDTO).toList();
    }

    /**
     * Indexes a freshly fetched tree by code, for O(1) lookups shared by {@link #resolveByCode} and
     * {@link #resolveDepartment}.
     *
     * @return the current tree keyed by DIR3CAIB code
     */
    private Map<String, AdmUnitOutputDTO> getTreeByCode() {
        return fetchTree().stream()
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
