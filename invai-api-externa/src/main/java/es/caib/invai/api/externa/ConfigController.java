package es.caib.invai.api.externa;

import es.caib.invai.api.externa.utils.Constants;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static es.caib.invai.api.externa.utils.Constants.KEY_HOST;

/**
 * Public configuration controller exposing non-secured configuration metadata endpoints.
 *
 * @since 1.0.1
 */
@Tag(name = "Configuració", description = "Servei públic de configuració, exposa la URL d'inici de sessió cap a l'API interna.")
@RestController
@Slf4j
@RequestMapping("config")
public class ConfigController {

    /**
     * Resolves configuration properties already loaded into the Spring context (see
     * {@code InvaiApiExternaApplication}'s {@code @PropertySource}), including any nested
     * {@code ${...}} placeholder it may contain - unlike a raw {@code java.util.Properties} read,
     * which never substitutes those.
     */
    @Autowired
    private Environment environment;

    /** Message source used to resolve localized error messages. */
    @Autowired
    private MessageSource messageSource;

    /**
     * Retrieves the fully qualified target authentication login URL.
     *
     * @return a {@link ResponseEntity} wrapping either a map containing the assembled login URL,
     * or an error message if the host isn't configured
     */
    @GetMapping("/url")
    public ResponseEntity<?> getLoggingUrl() {
        Locale currentLocale = LocaleContextHolder.getLocale();
        try {
            String host = environment.getProperty(KEY_HOST);

            if (StringUtils.isBlank(host)) {
                log.error("Host not found");
                return ResponseEntity.internalServerError()
                        .body(messageSource.getMessage("exception.config.hostnotfound", null, currentLocale));
            }

            Map<String, String> response = new HashMap<>();
            response.put("url", host + Constants.LOGIN_PATH);

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Unexpected error occurred", ex);
            return ResponseEntity.internalServerError()
                    .body(messageSource.getMessage("exception.config.unexpected", null, currentLocale));
        }
    }
}
