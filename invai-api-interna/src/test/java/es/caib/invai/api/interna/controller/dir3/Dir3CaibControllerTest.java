package es.caib.invai.api.interna.controller.dir3;

import es.caib.invai.api.interna.rest.dir3.Dir3CaibTreeCache;
import es.caib.invai.api.interna.rest.dir3.UnidadRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link Dir3CaibController}.
 */
@ExtendWith(MockitoExtension.class)
class Dir3CaibControllerTest {

    @Mock
    private Dir3CaibTreeCache dir3CaibTreeCache;

    @Test
    void getTree_delegatesToCacheAndReturnsItsResult() {
        UnidadRest unidad = new UnidadRest();
        unidad.setCodigo("A04003003");
        when(dir3CaibTreeCache.getTree("A04003003", true)).thenReturn(List.of(unidad));

        Dir3CaibController controller = new Dir3CaibController(dir3CaibTreeCache);
        List<UnidadRest> result = controller.getTree("A04003003", true);

        assertEquals(1, result.size());
        assertEquals("A04003003", result.get(0).getCodigo());
        verify(dir3CaibTreeCache).getTree("A04003003", true);
    }
}
