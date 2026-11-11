package es.caib.invai.api.interna.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for the {@link WebClient} factory methods of {@link SoffidConfig} and
 * {@link Dir3CaibConfig}, wired with plain values instead of a Spring context.
 */
class WebClientConfigsTest {

    @Test
    void soffidWebClient_isBuiltFromConfiguredValues() {
        SoffidConfig config = new SoffidConfig();
        ReflectionTestUtils.setField(config, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(config, "username", "user");
        ReflectionTestUtils.setField(config, "password", "pass");
        ReflectionTestUtils.setField(config, "connectTimeoutSeconds", 2L);
        ReflectionTestUtils.setField(config, "responseTimeoutSeconds", 5L);

        assertNotNull(config.soffidWebClient());
    }

    @Test
    void dir3CaibWebClient_isBuiltFromConfiguredValues() {
        Dir3CaibConfig config = new Dir3CaibConfig();
        ReflectionTestUtils.setField(config, "dir3CaibBaseUrl", "http://localhost:8081");
        ReflectionTestUtils.setField(config, "dir3CaibUsername", "user");
        ReflectionTestUtils.setField(config, "dir3CaibPassword", "pass");
        ReflectionTestUtils.setField(config, "connectTimeoutSeconds", 2L);
        ReflectionTestUtils.setField(config, "responseTimeoutSeconds", 5L);

        assertNotNull(config.dir3CaibWebClient());
    }
}
