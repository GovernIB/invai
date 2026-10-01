package es.caib.invai.api.interna.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link DataApiConfig#buildOpenApiUrlCandidates}/{@link
 * DataApiConfig#buildReuseUrlCandidates}, the pure URL-building logic used by {@code
 * DataApiClient} to race every known candidate when a caller doesn't ask to use its own explicit
 * override URL instead. {@code @Value}-injected fields are only resolved inside a Spring context,
 * so they're set explicitly here, mirroring what the corresponding properties resolve to in
 * production.
 */
class DataApiConfigTest {

    @Test
    void buildOpenApiUrlCandidates_concatenatesBaseUrlCodePrefixAndEverySuffixWithNoExtraSlash() {
        DataApiConfig config = new DataApiConfig();
        ReflectionTestUtils.setField(config, "baseUrl", "https://intranet.caib.es");
        ReflectionTestUtils.setField(config, "openDataPathPrefix", "api/externa/");
        ReflectionTestUtils.setField(config, "openDataPathSuffixes",
                List.of("swagger.json", "openapi.json", "api-docs.json"));

        List<String> candidates = config.buildOpenApiUrlCandidates("sedeib");

        assertEquals(List.of(
                "https://intranet.caib.es/sedeibapi/externa/swagger.json",
                "https://intranet.caib.es/sedeibapi/externa/openapi.json",
                "https://intranet.caib.es/sedeibapi/externa/api-docs.json"), candidates);
    }

    @Test
    void buildReuseUrlCandidates_concatenatesBaseUrlCodePrefixAndEverySuffixWithNoExtraSlash() {
        DataApiConfig config = new DataApiConfig();
        ReflectionTestUtils.setField(config, "baseUrl", "https://intranet.caib.es");
        ReflectionTestUtils.setField(config, "reusePathPrefix", "api/interna/");
        ReflectionTestUtils.setField(config, "reusePathSuffixes",
                List.of("swagger.json", "openapi.json"));

        List<String> candidates = config.buildReuseUrlCandidates("sedeib");

        assertEquals(List.of(
                "https://intranet.caib.es/sedeibapi/interna/swagger.json",
                "https://intranet.caib.es/sedeibapi/interna/openapi.json"), candidates);
    }
}
