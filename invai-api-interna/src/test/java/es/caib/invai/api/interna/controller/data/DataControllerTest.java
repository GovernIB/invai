package es.caib.invai.api.interna.controller.data;

import es.caib.invai.api.interna.rest.data.DataApiClient;
import es.caib.invai.api.interna.rest.data.OpenApiDocumentResult;
import es.caib.invai.api.interna.rest.openapi.OpenApiDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void getOpenDataDocument_delegatesToClientAndReturnsItsResult() {
        OpenApiDocumentResult document = new OpenApiDocumentResult(new OpenApiDocument(), "https://intranet.caib.es/sedeibapi/externa/openapi.json");
        when(dataApiClient.getOpenApiDocumentForOpenData("sedeib", false, null)).thenReturn(document);

        DataController controller = new DataController(dataApiClient);
        OpenApiDocumentResult result = controller.getOpenDataDocument("sedeib", false, null);

        assertEquals(document, result);
        verify(dataApiClient).getOpenApiDocumentForOpenData("sedeib", false, null);
    }

    @Test
    void getReuseDocument_delegatesToClientAndReturnsItsResult() {
        OpenApiDocumentResult document = new OpenApiDocumentResult(new OpenApiDocument(), "https://intranet.caib.es/sedeibapi/externa/swagger.json");
        when(dataApiClient.getOpenApiDocumentForReuse("sedeib", false, null)).thenReturn(document);

        DataController controller = new DataController(dataApiClient);
        OpenApiDocumentResult result = controller.getReuseDocument("sedeib", false, null);

        assertEquals(document, result);
        verify(dataApiClient).getOpenApiDocumentForReuse("sedeib", false, null);
    }
}
