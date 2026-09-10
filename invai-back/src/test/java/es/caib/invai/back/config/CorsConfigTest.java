package es.caib.invai.back.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link CorsConfig}, verifying that the {@link CorsConfigurationSource} bean
 * builds a {@link CorsConfiguration} that reflects the injected allowed-origin property, the
 * expected HTTP methods/headers, and credential support, registered against every path.
 */
class CorsConfigTest {

    private CorsConfig corsConfig;

    @BeforeEach
    void setUp() {
        corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "allowedOrigin", "https://invai.example.com");
    }

    @Test
    void corsConfigurationSource_registersConfigurationForAllPaths() {
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/externa/config/url");
        CorsConfiguration resolved = source.getCorsConfiguration(request);

        assertNotNull(resolved);
        assertEquals(java.util.List.of("https://invai.example.com"), resolved.getAllowedOrigins());
        assertTrue(resolved.getAllowedMethods().containsAll(
                java.util.List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")));
        assertEquals(java.util.List.of("*"), resolved.getAllowedHeaders());
        assertTrue(resolved.getAllowCredentials());
    }

    @Test
    void corsConfigurationSource_appliesInjectedAllowedOriginValue() {
        ReflectionTestUtils.setField(corsConfig, "allowedOrigin", "https://another-origin.example.com");

        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/any/path");

        CorsConfiguration resolved = source.getCorsConfiguration(request);

        assertNotNull(resolved);
        assertEquals(java.util.List.of("https://another-origin.example.com"), resolved.getAllowedOrigins());
    }
}
