package es.caib.invai.front;

import java.io.IOException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/handle404")
public class ErrorRedirectServlet extends HttpServlet {

    private static final String DEFAULT_LOCALE = "ca";
    private static final String[] SUPPORTED_LOCALES = {"ca", "es"};

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleError(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleError(request, response);
    }

    private void handleError(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String originalPath = (String) request.getAttribute("jakarta.servlet.error.request_uri");
        String locale = DEFAULT_LOCALE;

        for (String supportedLocale : SUPPORTED_LOCALES) {
            if (originalPath != null && originalPath.contains("/" + supportedLocale + "/")) {
                locale = supportedLocale;
                break;
            }
        }

        String redirectUrl = "/" + locale + "/index.html";
        RequestDispatcher dispatcher = request.getRequestDispatcher(redirectUrl);
        response.setStatus(HttpServletResponse.SC_OK);
        dispatcher.forward(request, response);
    }
}
