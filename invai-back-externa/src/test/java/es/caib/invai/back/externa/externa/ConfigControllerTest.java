package es.caib.invai.back.externa.externa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ConfigController}, exercising the branching logic around reading
 * the external server properties file pointed to by the
 * {@code es.caib.invai.system.properties} system property.
 */
@ExtendWith(MockitoExtension.class)
class ConfigControllerTest {

    private static final String FILE_NAME_PROPERTY = "es.caib.invai.system.properties";
    private static final String KEY_HOST = "es.caib.invai.login.host";

    @Mock
    private MessageSource messageSource;

    private ConfigController configController;
    private String originalPropertyValue;
    private Path tempPropertiesFile;

    @BeforeEach
    void setUp() {
        configController = new ConfigController();
        ReflectionTestUtils.setField(configController, "messageSource", messageSource);
        originalPropertyValue = System.getProperty(FILE_NAME_PROPERTY);

        lenient().when(messageSource.getMessage(eq("exception.config.missing"), any(), any()))
                .thenReturn("Configuration error");
        lenient().when(messageSource.getMessage(eq("exception.config.readerror"), any(), any()))
                .thenReturn("Unable to read configuration");
        lenient().when(messageSource.getMessage(eq("exception.config.hostnotfound"), any(), any()))
                .thenReturn("Unable to read host");
        lenient().when(messageSource.getMessage(eq("exception.config.unexpected"), any(), any()))
                .thenReturn("Unexpected server error");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (originalPropertyValue != null) {
            System.setProperty(FILE_NAME_PROPERTY, originalPropertyValue);
        } else {
            System.clearProperty(FILE_NAME_PROPERTY);
        }
        if (tempPropertiesFile != null && Files.exists(tempPropertiesFile)) {
            Files.delete(tempPropertiesFile);
        }
    }

    @Test
    void getLoggingUrl_systemPropertyNotSet_isCaughtByGenericHandlerDueToNpe() {
        // NOTE: when the system property is entirely unset, System.getProperty(...) returns null,
        // and the very next line calls propertyFile.isBlank() on that null reference, throwing an
        // NPE. This is swallowed by the method's outer try/catch(Exception), so the client receives
        // a generic 500 "Unexpected server error" instead of the intended "Configuration error"
        // message - a real bug in the null-handling of this method.
        System.clearProperty(FILE_NAME_PROPERTY);

        ResponseEntity<?> response = configController.getLoggingUrl();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected server error", response.getBody());
    }

    @Test
    void getLoggingUrl_systemPropertyBlank_returnsConfigurationError() {
        System.setProperty(FILE_NAME_PROPERTY, "   ");

        ResponseEntity<?> response = configController.getLoggingUrl();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Configuration error", response.getBody());
    }

    @Test
    void getLoggingUrl_propertyFileDoesNotExist_returnsUnableToReadConfiguration() {
        Path nonExistent = Path.of(System.getProperty("java.io.tmpdir"),
                "invai-config-test-does-not-exist-" + System.nanoTime(), "invai.properties");
        System.setProperty(FILE_NAME_PROPERTY, nonExistent.toString());

        ResponseEntity<?> response = configController.getLoggingUrl();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unable to read configuration", response.getBody());
    }

    @Test
    void getLoggingUrl_fileWithoutHostProperty_returnsUnableToReadHost() throws IOException {
        tempPropertiesFile = Files.createTempFile("invai-config-test", ".properties");
        Files.write(tempPropertiesFile, "some.other.key=value".getBytes(StandardCharsets.UTF_8));
        System.setProperty(FILE_NAME_PROPERTY, tempPropertiesFile.toString());

        ResponseEntity<?> response = configController.getLoggingUrl();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unable to read host", response.getBody());
    }

    @Test
    void getLoggingUrl_fileWithBlankHostProperty_returnsUnableToReadHost() throws IOException {
        tempPropertiesFile = Files.createTempFile("invai-config-test", ".properties");
        Files.write(tempPropertiesFile, (KEY_HOST + "=   ").getBytes(StandardCharsets.UTF_8));
        System.setProperty(FILE_NAME_PROPERTY, tempPropertiesFile.toString());

        ResponseEntity<?> response = configController.getLoggingUrl();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unable to read host", response.getBody());
    }

    @Test
    void getLoggingUrl_validHost_returnsAssembledLoginUrl() throws IOException {
        tempPropertiesFile = Files.createTempFile("invai-config-test", ".properties");
        Files.write(tempPropertiesFile,
                (KEY_HOST + "=https://invai.example.com").getBytes(StandardCharsets.UTF_8));
        System.setProperty(FILE_NAME_PROPERTY, tempPropertiesFile.toString());

        ResponseEntity<?> response = configController.getLoggingUrl();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("https://invai.example.com/api/auth/login", body.get("url"));
    }
}
