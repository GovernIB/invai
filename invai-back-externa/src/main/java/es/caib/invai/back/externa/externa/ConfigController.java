package es.caib.invai.back.externa.externa;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static es.caib.invai.back.externa.utils.Constants.KEY_HOST;

/**
 * Public configuration controller exposing non-secured configuration metadata endpoints.
 * Handles reading from external server property files to compute deployment-specific URLs.
 *
 * @since 1.0.1
 */
@Tag(name = "Configuració", description = "Servei públic de configuració, exposa la URL d'inici de sessió cap a l'API interna.")
@RestController
@Slf4j
@RequestMapping("config")
public class ConfigController {

    /** System property key used to point to the external server configuration file. */
    private static final String FILE_NAME_PROPERTY = "es.caib.invai.system.properties";

    /** Message source used to resolve localized error messages. */
    @Autowired
    private MessageSource messageSource;

    /**
     * Retrieves the fully qualified target authentication login URL.
     * Reads the configured host domain parameter from the system environment property files.
     *
     * @return a {@link ResponseEntity} wrapping either a map containing the assembled login URL,
     * or an error message indicating a configuration or reading anomaly
     */
    @GetMapping("/url")
    public ResponseEntity<?> getLoggingUrl() {
        Locale currentLocale = LocaleContextHolder.getLocale();
        try {
            String propertyFile = System.getProperty(FILE_NAME_PROPERTY);

            if (propertyFile.isBlank()) {
                log.error("Missing system property: {}", FILE_NAME_PROPERTY);
                return ResponseEntity.internalServerError()
                        .body(messageSource.getMessage("exception.config.missing", null, currentLocale));
            }

            Properties properties = new Properties();

            if (loadProperties(propertyFile, properties)) {
                return ResponseEntity.internalServerError()
                        .body(messageSource.getMessage("exception.config.readerror", null, currentLocale));
            }

            String host = properties.getProperty(KEY_HOST);

            if (StringUtils.isBlank(host)) {
                log.error("Host not found");
                return ResponseEntity.internalServerError()
                        .body(messageSource.getMessage("exception.config.hostnotfound", null, currentLocale));
            }

            Map<String, String> response = new HashMap<>();
            response.put("url", host + "/api/auth/login");

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Unexpected error occurred", ex);
            return ResponseEntity.internalServerError()
                    .body(messageSource.getMessage("exception.config.unexpected", null, currentLocale));
        }
    }

    /**
     * Utility method to safely parse an external properties resource file using UTF-8 encoding.
     *
     * @param propertyFile path pointing to the configuration source on the file system
     * @param properties   the target {@link Properties} instance to load data into
     * @return {@code true} if an {@link IOException} occurred during retrieval, {@code false} otherwise
     */
    private boolean loadProperties(String propertyFile, Properties properties) {
        try (Reader reader = new FileReader(propertyFile, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            log.error("Error reading properties file", e);
            return true;
        }
        return false;
    }
}