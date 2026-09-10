package es.caib.invai.back.ejb.integrations.dir3.admUnit;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.integrations.dir3.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.back.rest.dir3.Dir3CaibClient;
import es.caib.invai.back.rest.dir3.UnidadRest;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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

    @InjectMocks
    private AdmUnitServiceFacadeBean admUnitServiceFacadeBean;

    /**
     * {@code @Value}-injected fields are only resolved inside a Spring context, which these plain
     * Mockito unit tests don't start — so the properties-driven configuration is set explicitly
     * here, mirroring what {@code es.caib.invai.dir3caib.root-adm-unit-code}/
     * {@code department-hierarchy-level} resolve to in production.
     */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(admUnitServiceFacadeBean, "rootAdmUnitCode", "A04003003");
        ReflectionTestUtils.setField(admUnitServiceFacadeBean, "departmentHierarchyLevel", 2);
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
    void resolveByCodeOrThrow_found_returnsMatchingUnit() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1),
                unidadRest("DGSAN", "Direccio General de Salut", null, "A04003003", 3)
        ));

        AdmUnitOutputDTO result = admUnitServiceFacadeBean.resolveByCodeOrThrow("DGSAN");

        assertNotNull(result);
        assertEquals("DGSAN", result.getCode());
    }

    @Test
    void resolveByCodeOrThrow_notFound_returnsNull() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1)
        ));

        assertNull(admUnitServiceFacadeBean.resolveByCodeOrThrow("UNKNOWN"));
    }

    @Test
    void resolveByCodeOrThrow_blankCode_returnsNullWithoutQueryingDir3Caib() {
        assertNull(admUnitServiceFacadeBean.resolveByCodeOrThrow(null));
        assertNull(admUnitServiceFacadeBean.resolveByCodeOrThrow(""));
        assertNull(admUnitServiceFacadeBean.resolveByCodeOrThrow("  "));
        verify(dir3CaibClient, never()).getTree(any(), anyBoolean());
    }

    @Test
    void resolveByCodeOrThrow_dir3CaibUnavailable_propagatesException() {
        when(dir3CaibClient.getTree("A04003003", true))
                .thenThrow(new BusinessRuleException(Constants.ERR_ADMUNIT_DIR3_UNAVAILABLE));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> admUnitServiceFacadeBean.resolveByCodeOrThrow("DGSAN"));
        assertEquals(Constants.ERR_ADMUNIT_DIR3_UNAVAILABLE, ex.getMessage());
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
    void resolveDepartment_deepDescendant_walksUpToLevel2Ancestor() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1),
                unidadRest("A04026919", "Conselleria de Sanidad", null, "A04003003", 2),
                unidadRest("DGSAN", "Direccio General de Salut", null, "A04026919", 3),
                unidadRest("SERV1", "Servei X", null, "DGSAN", 4)
        ));

        AdmUnitOutputDTO department = admUnitServiceFacadeBean.resolveDepartment("SERV1");

        assertNotNull(department);
        assertEquals("A04026919", department.getCode());
        assertEquals("Conselleria de Sanidad", department.getName());
    }

    @Test
    void resolveDepartment_codeIsAlreadyADepartment_returnsItself() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1),
                unidadRest("A04026919", "Conselleria de Sanidad", null, "A04003003", 2)
        ));

        AdmUnitOutputDTO department = admUnitServiceFacadeBean.resolveDepartment("A04026919");

        assertNotNull(department);
        assertEquals("A04026919", department.getCode());
    }

    @Test
    void resolveDepartment_blankCode_returnsNullWithoutQueryingDir3Caib() {
        assertNull(admUnitServiceFacadeBean.resolveDepartment(null));
        assertNull(admUnitServiceFacadeBean.resolveDepartment(""));
        assertNull(admUnitServiceFacadeBean.resolveDepartment("   "));
        verify(dir3CaibClient, never()).getTree(any(), anyBoolean());
    }

    @Test
    void resolveDepartment_dir3CaibUnavailable_returnsNullInsteadOfThrowing() {
        when(dir3CaibClient.getTree("A04003003", true))
                .thenThrow(new BusinessRuleException(Constants.ERR_ADMUNIT_DIR3_UNAVAILABLE));

        assertNull(admUnitServiceFacadeBean.resolveDepartment("DGSAN"));
    }

    @Test
    void resolveDepartment_codeNotInTree_returnsNull() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1)
        ));

        AdmUnitOutputDTO department = admUnitServiceFacadeBean.resolveDepartment("UNKNOWN");

        assertNull(department);
    }

    @Test
    void resolveDepartment_rootHasNoDepartmentAncestor_returnsNull() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1)
        ));

        AdmUnitOutputDTO department = admUnitServiceFacadeBean.resolveDepartment("A04003003");

        assertNull(department);
    }

    @Test
    void resolveDepartment_reusesCachedTreeWithoutQueryingDir3CaibAgain() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1),
                unidadRest("A04026919", "Conselleria de Sanidad", null, "A04003003", 2)
        ));
        admUnitServiceFacadeBean.getDepartments(PageRequest.of(0, 10));

        admUnitServiceFacadeBean.resolveDepartment("A04026919");

        verify(dir3CaibClient, times(1)).getTree("A04003003", true);
    }

    @Test
    void getDepartments_filtersCachedTreeToLevel2Units() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1),
                unidadRest("A04026919", "Conselleria de Sanidad", null, "A04003003", 2),
                unidadRest("A04026920", "Conselleria de Educacion", null, "A04003003", 2),
                unidadRest("DGSAN", "Direccio General de Salut", null, "A04026919", 3)
        ));
        Pageable pageable = PageRequest.of(0, 10);

        Page<AdmUnitOutputDTO> result = admUnitServiceFacadeBean.getDepartments(pageable);

        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(dto -> dto.getLevel() == 2));
    }

    @Test
    void getAdmUnitsByDepartment_returnsAllDescendantsAtAnyDepthButNotTheDepartmentItself() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1),
                unidadRest("A04026919", "Conselleria de Sanidad", null, "A04003003", 2),
                unidadRest("A04026920", "Conselleria de Educacion", null, "A04003003", 2),
                unidadRest("DGSAN", "Direccio General de Salut", null, "A04026919", 3),
                unidadRest("SERV1", "Servei X", null, "DGSAN", 4),
                unidadRest("DGEDU", "Direccio General d'Educacio", null, "A04026920", 3)
        ));
        Pageable pageable = PageRequest.of(0, 10);

        Page<AdmUnitOutputDTO> result = admUnitServiceFacadeBean.getAdmUnitsByDepartment("A04026919", pageable);

        List<String> codes = result.getContent().stream().map(AdmUnitOutputDTO::getCode).toList();
        assertEquals(2, result.getTotalElements());
        assertTrue(codes.containsAll(List.of("DGSAN", "SERV1")));
        assertFalse(codes.contains("A04026919"));
        assertFalse(codes.contains("DGEDU"));
    }

    @Test
    void getAdmUnitsByDepartment_blankCode_returnsEmptyPageWithoutQueryingDir3Caib() {
        Pageable pageable = PageRequest.of(0, 10);

        assertEquals(0, admUnitServiceFacadeBean.getAdmUnitsByDepartment(null, pageable).getTotalElements());
        assertEquals(0, admUnitServiceFacadeBean.getAdmUnitsByDepartment("  ", pageable).getTotalElements());
        verify(dir3CaibClient, never()).getTree(any(), anyBoolean());
    }

    @Test
    void getAdmUnitsByDepartment_codeNotInTree_returnsEmptyPage() {
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(
                unidadRest("A04003003", "Gobierno de las Illes Balears", "Govern de les Illes Balears", "A99999999", 1)
        ));
        Pageable pageable = PageRequest.of(0, 10);

        Page<AdmUnitOutputDTO> result = admUnitServiceFacadeBean.getAdmUnitsByDepartment("UNKNOWN", pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getDepartmentHierarchyLevel_returnsConfiguredValue() {
        assertEquals(2, admUnitServiceFacadeBean.getDepartmentHierarchyLevel());
    }
}
