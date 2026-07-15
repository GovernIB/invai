package es.caib.invai.front;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter to ensure that the request is returned with one of the supported languages, according to preferences
 * indicated in the request.
 */
public class LocaleRedirectFilter implements Filter {

    private static final String[] SUPPORTED_LOCALES = {"ca", "es"};

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        var isLocalized = false;

        for (String locale : SUPPORTED_LOCALES) {
            if (path.startsWith("/invaifront/" + locale + "/")) {
                isLocalized = true;
                break;
            }
        }

        if (!isLocalized) {
            httpResponse.sendRedirect("/invaifront/ca/");
        } else {
            chain.doFilter(request, response);
        }
    }

}
