package es.caib.invai.api.interna.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link DataApiConfig}.
 */
class DataApiConfigTest {

    @Test
    void dataWebClient_isBuiltWithoutAnyBaseUrl() {
        DataApiConfig config = new DataApiConfig();
        ReflectionTestUtils.setField(config, "connectTimeoutSeconds", 5L);
        ReflectionTestUtils.setField(config, "responseTimeoutSeconds", 10L);

        WebClient webClient = config.dataWebClient();

        assertNotNull(webClient);
    }
}
