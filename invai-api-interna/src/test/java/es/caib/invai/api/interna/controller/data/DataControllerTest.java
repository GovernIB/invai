package es.caib.invai.api.interna.controller.data;

import es.caib.invai.api.interna.rest.data.DataApiClient;
import es.caib.invai.api.interna.rest.data.UrlCheckResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DataController}.
 */
@ExtendWith(MockitoExtension.class)
class DataControllerTest {

    @Mock
    private DataApiClient dataApiClient;

    @Test
    void checkUrl_delegatesToClientAndReturnsItsResult() {
        UrlCheckResult expected = new UrlCheckResult("https://intranet.caib.es/sedeibapi/externa", true, 200);
        when(dataApiClient.checkUrl("https://intranet.caib.es/sedeibapi/externa")).thenReturn(expected);

        UrlCheckResult result = new DataController(dataApiClient).checkUrl("https://intranet.caib.es/sedeibapi/externa");

        assertSame(expected, result);
        verify(dataApiClient).checkUrl("https://intranet.caib.es/sedeibapi/externa");
    }
}
