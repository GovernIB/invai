package es.caib.invai.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Registers a filter that rewrites the bundled swagger-ui {@code <title>Swagger UI</title>} to a
 * project-specific title: springdoc-openapi ships swagger-ui's HTML with a fixed browser-tab title
 * and exposes no configuration property to change it.
 *
 * @since 1.0.2
 */
@Configuration
public class SwaggerUiTitleFilterConfig {

    @Bean
    public FilterRegistrationBean<SwaggerUiTitleFilter> swaggerUiTitleFilter() {
        FilterRegistrationBean<SwaggerUiTitleFilter> registration =
                new FilterRegistrationBean<>(new SwaggerUiTitleFilter("INVAI – API EXTERNA"));
        registration.addUrlPatterns("/index.html", "/swagger-ui/*");
        return registration;
    }

    /** Buffers the HTML response for the swagger-ui index page so its title can be rewritten. */
    private static class SwaggerUiTitleFilter extends OncePerRequestFilter {

        private final String title;

        SwaggerUiTitleFilter(String title) {
            this.title = title;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            BufferedResponseWrapper wrapper = new BufferedResponseWrapper(response);
            chain.doFilter(request, wrapper);

            byte[] content = wrapper.getBuffer();
            String contentType = wrapper.getContentType();

            if (contentType != null && contentType.contains("text/html")) {
                String html = new String(content, StandardCharsets.UTF_8)
                        .replace("<title>Swagger UI</title>", "<title>" + title + "</title>");
                byte[] rewritten = html.getBytes(StandardCharsets.UTF_8);
                response.setContentLength(rewritten.length);
                response.getOutputStream().write(rewritten);
            } else {
                if (contentType != null) {
                    response.setContentLength(content.length);
                }
                response.getOutputStream().write(content);
            }
        }
    }

    /** Buffers the whole response body so it can be inspected/rewritten before flushing to the client. */
    private static class BufferedResponseWrapper extends HttpServletResponseWrapper {

        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private final ServletOutputStream servletOutputStream = new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                // Not needed for a synchronous, fully-buffered response.
            }

            @Override
            public void write(int b) {
                buffer.write(b);
            }
        };
        private PrintWriter writer;

        BufferedResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public ServletOutputStream getOutputStream() {
            return servletOutputStream;
        }

        @Override
        public PrintWriter getWriter() {
            if (writer == null) {
                writer = new PrintWriter(new OutputStreamWriter(buffer, StandardCharsets.UTF_8), true);
            }
            return writer;
        }

        byte[] getBuffer() {
            if (writer != null) {
                writer.flush();
            }
            return buffer.toByteArray();
        }
    }
}
