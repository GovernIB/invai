package es.caib.invai.back.ejb.catalog.admUnit;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.catalog.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.back.rest.dir3.Dir3CaibClient;
import es.caib.invai.back.rest.dir3.UnidadRest;
import es.caib.invai.back.service.mapper.catalog.admUnit.AdmUnitMapper;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AdmUnitServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link Dir3CaibClient} collaborator fully mocked. Administrative units are pure
 * external reference data mirrored live from DIR3CAIB — there is no local persistence to mock.
 */
@ExtendWith(MockitoExtension.class)
class AdmUnitServiceFacadeBeanTest {

    @Mock
    private Dir3CaibClient dir3CaibClient;

    @Mock
    private AdmUnitMapper admUnitMapper;

    @InjectMocks
    private AdmUnitServiceFacadeBean admUnitServiceFacadeBean;

    /**
     * {@code @Value}-injected fields are only resolved inside a Spring context, which these plain
     * Mockito unit tests don't start — so the properties-driven configuration is set explicitly
     * here, mirroring what {@code es.caib.invai.dir3caib.root-adm-unit-code}/
     * {@code department-hierarchy-level} resolve to in production. {@link #admUnitMapper} is
     * stubbed once here (rather than per test) to replicate its real, pure mapping logic for any
     * {@link UnidadRest} built by {@link #unidadRest}; {@code lenient()} since not every test
     * exercises a DIR3CAIB fetch.
     */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(admUnitServiceFacadeBean, "rootAdmUnitCode", "A04003003");
        ReflectionTestUtils.setField(admUnitServiceFacadeBean, "departmentHierarchyLevel", 2);
        lenient().when(admUnitMapper.toResponse(any(UnidadRest.class))).thenAnswer(invocation -> {
            UnidadRest source = invocation.getArgument(0);
            AdmUnitOutputDTO dto = new AdmUnitOutputDTO();
            dto.setCode(source.getCodigo());
            dto.setName(source.getDenominacionCooficial() != null ? source.getDenominacionCooficial() : source.getDenominacion());
            dto.setParentCode(source.getCodUnidadSuperior());
            dto.setLevel(source.getNivelJerarquico());
            return dto;
        });
    }

    private static UnidadRest unidadRest(String codigo, String denominacion, String denominacionCooficial, String codUnidadSuperior, Integer nivelJerarquico) {
        UnidadRest unidadRest = new UnidadRest();
        unidadRest.setCodigo(codigo);
        unidadRest.setDenominacion(denominacion);
        unidadRest.setDenominacionCooficial(denominacionCooficial);
        unidadRest.setCodUnidadSuperior(codUnidadSuperior);
        unidadRest.setNivelJerarquico(nivelJerarquico);
        return unidadRest;
    }

    @Test
    void resolveByCode_found_returnsMatchingUnit() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1),
                unidadRest("DGSAN", "Direccio General de Salut", null, "A04003003", 3)
        ));

        AdmUnitOutputDTO result = admUnitServiceFacadeBean.resolveByCode("DGSAN");

        assertNotNull(result);
        assertEquals("DGSAN", result.getCode());
        assertEquals("Direccio General de Salut", result.getName());
    }

    @Test
    void resolveByCode_notFound_returnsNull() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1)
        ));

        assertNull(admUnitServiceFacadeBean.resolveByCode("UNKNOWN"));
    }

    @Test
    void resolveByCode_blankCode_returnsNullWithoutQueryingDir3Caib() {
        assertNull(admUnitServiceFacadeBean.resolveByCode(null));
        assertNull(admUnitServiceFacadeBean.resolveByCode(""));
        assertNull(admUnitServiceFacadeBean.resolveByCode("  "));
        verify(dir3CaibClient, never()).getTree(any(), anyBoolean());
    }

    @Test
    void resolveByCode_dir3CaibUnavailable_returnsNullInsteadOfThrowing() {
        when(dir3CaibClient.getTree("A04003003", true))
                .thenThrow(new BusinessRuleException(Constants.ERR_ADMUNIT_DIR3_UNAVAILABLE));

        assertNull(admUnitServiceFacadeBean.resolveByCode("DGSAN"));
    }

    @Test
    void validateAdmUnitCode_departmentLevelOrBelow_doesNotThrow() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1),
                unidadRest("DGSAN", "Direccio General de Salut", null, "A04003003", 3)
        ));

        assertDoesNotThrow(() -> admUnitServiceFacadeBean.validateAdmUnitCode("DGSAN"));
    }

    @Test
    void validateAdmUnitCode_blankCode_doesNotThrowWithoutQueryingDir3Caib() {
        assertDoesNotThrow(() -> admUnitServiceFacadeBean.validateAdmUnitCode(null));
        assertDoesNotThrow(() -> admUnitServiceFacadeBean.validateAdmUnitCode(""));
        assertDoesNotThrow(() -> admUnitServiceFacadeBean.validateAdmUnitCode("  "));
        verify(dir3CaibClient, never()).getTree(any(), anyBoolean());
    }

    @Test
    void validateAdmUnitCode_notFound_throwsBusinessRuleException() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1)
        ));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> admUnitServiceFacadeBean.validateAdmUnitCode("UNKNOWN"));
        assertEquals(Constants.ERR_ADMUNIT_NOT_FOUND, ex.getMessage());
    }

    @Test
    void validateAdmUnitCode_aboveDepartmentLevel_throwsBusinessRuleException() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1)
        ));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> admUnitServiceFacadeBean.validateAdmUnitCode("A04003003"));
        assertEquals(Constants.ERR_ADMUNIT_ABOVE_DEPARTMENT_LEVEL, ex.getMessage());
    }

    @Test
    void validateAdmUnitCode_dir3CaibUnavailable_propagatesException() {
        when(dir3CaibClient.getTree("A04003003", true))
                .thenThrow(new BusinessRuleException(Constants.ERR_ADMUNIT_DIR3_UNAVAILABLE));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> admUnitServiceFacadeBean.validateAdmUnitCode("DGSAN"));
        assertEquals(Constants.ERR_ADMUNIT_DIR3_UNAVAILABLE, ex.getMessage());
    }

    @Test
    void getAdmUnitsByCodes_mixOfFoundAndUnknownCodes_returnsOnlyMatchesInASingleTreeFetch() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1),
                unidadRest("DGSAN", "Direccio General de Salut", null, "A04003003", 3)
        ));

        Map<String, AdmUnitOutputDTO> result = admUnitServiceFacadeBean.getAdmUnitsByCodes(List.of("DGSAN", "UNKNOWN", "DGSAN"));

        assertEquals(1, result.size());
        assertEquals("DGSAN", result.get("DGSAN").getCode());
        verify(dir3CaibClient, times(1)).getTree("A04003003", true);
    }

    @Test
    void getAdmUnitsByCodes_blankAndNullEntries_areIgnored() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1)
        ));

        Map<String, AdmUnitOutputDTO> result = admUnitServiceFacadeBean.getAdmUnitsByCodes(Arrays.asList(null, "", "  "));

        assertTrue(result.isEmpty());
    }

    @Test
    void getAdmUnitsByCodes_dir3CaibUnavailable_returnsEmptyMapInsteadOfThrowing() {
        when(dir3CaibClient.getTree("A04003003", true))
                .thenThrow(new BusinessRuleException(Constants.ERR_ADMUNIT_DIR3_UNAVAILABLE));

        assertTrue(admUnitServiceFacadeBean.getAdmUnitsByCodes(List.of("DGSAN")).isEmpty());
    }

    @Test
    void findCodesByNameContaining_matchesCaseInsensitively() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04026919", "Conselleria de Sanidad", null, "A04003003", 2),
                unidadRest("A04026920", "Conselleria de Educacion", null, "A04003003", 2)
        ));

        List<String> codes = admUnitServiceFacadeBean.findCodesByNameContaining("sanidad");

        assertEquals(List.of("A04026919"), codes);
    }

    @Test
    void findCodesByNameContaining_blankPattern_returnsEmptyListWithoutQueryingDir3Caib() {
        assertEquals(List.of(), admUnitServiceFacadeBean.findCodesByNameContaining(null));
        assertEquals(List.of(), admUnitServiceFacadeBean.findCodesByNameContaining(""));
        verify(dir3CaibClient, never()).getTree(any(), anyBoolean());
    }

    @Test
    void getAll_noSearch_returnsDepartmentsAndTheirDescendantsButNotTheRoot() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1),
                unidadRest("A04026919", "Conselleria de Sanidad", null, "A04003003", 2),
                unidadRest("A04026920", "Conselleria de Educacion", null, "A04003003", 2),
                unidadRest("DGSAN", "Direccio General de Salut", null, "A04026919", 3),
                unidadRest("SERV1", "Servei X", null, "DGSAN", 4)
        ));
        Pageable pageable = PageRequest.of(0, 10);

        Page<AdmUnitOutputDTO> result = admUnitServiceFacadeBean.getAll(null, pageable);

        List<String> codes = result.getContent().stream().map(AdmUnitOutputDTO::getCode).toList();
        assertEquals(4, result.getTotalElements());
        assertTrue(codes.containsAll(List.of("A04026919", "A04026920", "DGSAN", "SERV1")));
        assertFalse(codes.contains("A04003003"));
    }

    @Test
    void getAll_withSearch_filtersByNameCaseInsensitively() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1),
                unidadRest("A04026919", "Conselleria de Sanidad", null, "A04003003", 2),
                unidadRest("A04026920", "Conselleria de Educacion", null, "A04003003", 2)
        ));
        Pageable pageable = PageRequest.of(0, 10);

        Page<AdmUnitOutputDTO> result = admUnitServiceFacadeBean.getAll("sanidad", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("A04026919", result.getContent().get(0).getCode());
    }

    @Test
    void getAll_withSearch_filtersByCodeCaseInsensitively() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1),
                unidadRest("A04026919", "Conselleria de Sanidad", null, "A04003003", 2),
                unidadRest("A04026920", "Conselleria de Educacion", null, "A04003003", 2)
        ));
        Pageable pageable = PageRequest.of(0, 10);

        Page<AdmUnitOutputDTO> result = admUnitServiceFacadeBean.getAll("a04026919", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("A04026919", result.getContent().get(0).getCode());
    }

    @Test
    void getAll_blankSearch_returnsEveryEligibleUnit() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1),
                unidadRest("A04026919", "Conselleria de Sanidad", null, "A04003003", 2)
        ));
        Pageable pageable = PageRequest.of(0, 10);

        Page<AdmUnitOutputDTO> result = admUnitServiceFacadeBean.getAll("  ", pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getDepartmentHierarchyLevel_returnsConfiguredValue() {
        assertEquals(2, admUnitServiceFacadeBean.getDepartmentHierarchyLevel());
    }
}
