package es.caib.invai.api.interna.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SwaggerUiTitleFilterConfig}: the registered filter rewrites the HTML title of
 * the Swagger UI pages and leaves any other response body untouched.
 */
class SwaggerUiTitleFilterConfigTest {

    private final FilterRegistrationBean<?> registration = new SwaggerUiTitleFilterConfig().swaggerUiTitleFilter();

    private MockHttpServletResponse run(FilterChain chain) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        registration.getFilter().doFilter(new MockHttpServletRequest("GET", "/index.html"), response, chain);
        return response;
    }

    @Test
    void registration_coversSwaggerUiUrls() {
        assertTrue(registration.getUrlPatterns().contains("/index.html"));
        assertTrue(registration.getUrlPatterns().contains("/swagger-ui/*"));
    }

    @Test
    void htmlResponse_titleIsRewritten() throws Exception {
        MockHttpServletResponse response = run((req, res) -> {
            res.setContentType("text/html");
            res.getOutputStream().write("<html><title>Swagger UI</title></html>".getBytes(StandardCharsets.UTF_8));
        });

        String body = response.getContentAsString(StandardCharsets.UTF_8);
        assertTrue(body.contains("<title>INVAI – API INTERNA (Integracions)</title>"));
        assertEquals(body.getBytes(StandardCharsets.UTF_8).length, response.getContentLength());
    }

    @Test
    void htmlResponseWrittenThroughWriter_titleIsRewritten() throws Exception {
        MockHttpServletResponse response = run((req, res) -> {
            res.setContentType("text/html");
            res.getWriter().write("<title>Swagger UI</title>");
        });

        assertTrue(response.getContentAsString(StandardCharsets.UTF_8).contains("INVAI – API INTERNA"));
    }

    @Test
    void nonHtmlResponse_isPassedThroughWithContentLength() throws Exception {
        MockHttpServletResponse response = run((req, res) -> {
            res.setContentType("application/json");
            res.getOutputStream().write("{\"a\":1}".getBytes(StandardCharsets.UTF_8));
        });

        assertEquals("{\"a\":1}", response.getContentAsString());
        assertEquals(7, response.getContentLength());
    }

    @Test
    void responseWithoutContentType_isPassedThroughUntouched() throws Exception {
        MockHttpServletResponse response = run((req, res) -> res.getOutputStream().write(new byte[]{1, 2, 3}));

        assertEquals(3, response.getContentAsByteArray().length);
    }
}
