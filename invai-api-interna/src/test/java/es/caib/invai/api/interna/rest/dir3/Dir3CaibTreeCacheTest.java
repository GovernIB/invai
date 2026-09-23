package es.caib.invai.api.interna.rest.dir3;

import es.caib.invai.api.interna.exception.IntegrationUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link Dir3CaibTreeCache}.
 */
@ExtendWith(MockitoExtension.class)
class Dir3CaibTreeCacheTest {

    @Mock
    private Dir3CaibClient dir3CaibClient;

    private Dir3CaibTreeCache dir3CaibTreeCache;

    @BeforeEach
    void setUp() {
        dir3CaibTreeCache = new Dir3CaibTreeCache(dir3CaibClient);
        ReflectionTestUtils.setField(dir3CaibTreeCache, "rootAdmUnitCode", "A04003003");
    }

    @Test
    void getTree_beforeAnyRefresh_returnsEmptyListWithoutCallingClient() {
        List<UnidadRest> result = dir3CaibTreeCache.getTree("A04003003", true);

        assertTrue(result.isEmpty());
    }

    @Test
    void getTree_configuredRootAfterRefresh_servesCachedSnapshotWithoutCallingClientAgain() {
        UnidadRest unidad = new UnidadRest();
        unidad.setCodigo("A04003003");
        when(dir3CaibClient.getTree("A04003003", true)).thenReturn(List.of(unidad));

        dir3CaibTreeCache.refresh();
        List<UnidadRest> first = dir3CaibTreeCache.getTree("A04003003", true);
        List<UnidadRest> second = dir3CaibTreeCache.getTree("A04003003", true);

        assertSame(first, second);
        assertEquals(1, first.size());
        verify(dir3CaibClient, times(1)).getTree("A04003003", true);
    }

    @Test
    void getTree_differentRootOrDenomination_bypassesCacheAndCallsClientDirectly() {
        UnidadRest unidad = new UnidadRest();
        unidad.setCodigo("OTHER");
        when(dir3CaibClient.getTree("OTHER", false)).thenReturn(List.of(unidad));

        List<UnidadRest> result = dir3CaibTreeCache.getTree("OTHER", false);

        assertEquals(1, result.size());
        verify(dir3CaibClient).getTree("OTHER", false);
    }

    @Test
    void refresh_clientFails_keepsPreviousSnapshotWithoutThrowing() {
        UnidadRest unidad = new UnidadRest();
        unidad.setCodigo("A04003003");
        when(dir3CaibClient.getTree("A04003003", true))
                .thenReturn(List.of(unidad))
                .thenThrow(new IntegrationUnavailableException("DIR3CAIB unavailable", null));

        dir3CaibTreeCache.refresh();
        dir3CaibTreeCache.refresh();

        List<UnidadRest> result = dir3CaibTreeCache.getTree("A04003003", true);
        assertEquals(1, result.size());
    }
}
