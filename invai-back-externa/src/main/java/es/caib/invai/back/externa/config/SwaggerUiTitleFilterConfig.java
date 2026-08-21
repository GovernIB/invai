package es.caib.invai.back.externa.config;

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

    /**
     * Registers the {@link SwaggerUiTitleFilter} against the swagger-ui index page URLs so the
     * default browser-tab title is rewritten to the project-specific one.
     *
     * @return the configured {@link FilterRegistrationBean}
     */
    @Bean
    public FilterRegistrationBean<SwaggerUiTitleFilter> swaggerUiTitleFilter() {
        FilterRegistrationBean<SwaggerUiTitleFilter> registration =
                new FilterRegistrationBean<>(new SwaggerUiTitleFilter("INVAI – API EXTERNA"));
        registration.addUrlPatterns("/index.html", "/swagger-ui/*");
        return registration;
    }

    /** Buffers the HTML response for the swagger-ui index page so its title can be rewritten. */
    private static class SwaggerUiTitleFilter extends OncePerRequestFilter {

        /** The project-specific title to inject into the swagger-ui HTML page. */
        private final String title;

        /**
         * Creates a filter that rewrites the swagger-ui page title.
         *
         * @param title the project-specific title to use in place of the default "Swagger UI"
         */
        SwaggerUiTitleFilter(String title) {
            this.title = title;
        }

        /**
         * Buffers the downstream response and, when it is an HTML page, rewrites the default
         * swagger-ui {@code <title>} tag to the configured project-specific title before writing
         * the (possibly modified) content back to the real response.
         *
         * @param request  the incoming HTTP request
         * @param response the outgoing HTTP response
         * @param chain    the remaining filter chain to invoke
         * @throws ServletException if an error occurs while processing the filter chain
         * @throws IOException      if an I/O error occurs while reading or writing the response
         */
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

        /** In-memory buffer accumulating the full response body before it is flushed to the client. */
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        /** Output stream implementation that writes directly into the in-memory {@link #buffer}. */
        private final ServletOutputStream servletOutputStream = new ServletOutputStream() {
            /**
             * Always ready, since writes go directly into the in-memory buffer rather than a real socket.
             *
             * @return {@code true} always
             */
            @Override
            public boolean isReady() {
                return true;
            }

            /**
             * No-op: not needed for a synchronous, fully-buffered response.
             *
             * @param writeListener the write listener (unused)
             */
            @Override
            public void setWriteListener(WriteListener writeListener) {
                // Not needed for a synchronous, fully-buffered response.
            }

            /**
             * Writes a single byte into the in-memory buffer.
             *
             * @param b the byte to write
             */
            @Override
            public void write(int b) {
                buffer.write(b);
            }
        };
        /** Lazily-created writer over {@link #buffer}, used when the response is written via a {@link PrintWriter}. */
        private PrintWriter writer;

        /**
         * Wraps the given response so its output can be buffered and inspected before being flushed.
         *
         * @param response the original HTTP response to wrap
         */
        BufferedResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        /**
         * Returns the buffering output stream instead of the underlying response's stream, so writes
         * can be inspected before being flushed to the client.
         *
         * @return the buffering {@link ServletOutputStream}
         */
        @Override
        public ServletOutputStream getOutputStream() {
            return servletOutputStream;
        }

        /**
         * Returns a writer over the in-memory buffer, creating it lazily on first use.
         *
         * @return the buffering {@link PrintWriter}
         */
        @Override
        public PrintWriter getWriter() {
            if (writer == null) {
                writer = new PrintWriter(new OutputStreamWriter(buffer, StandardCharsets.UTF_8), true);
            }
            return writer;
        }

        /**
         * Flushes any pending writer output and returns the full buffered response body.
         *
         * @return the buffered response body as a byte array
         */
        byte[] getBuffer() {
            if (writer != null) {
                writer.flush();
            }
            return buffer.toByteArray();
        }
    }
}
