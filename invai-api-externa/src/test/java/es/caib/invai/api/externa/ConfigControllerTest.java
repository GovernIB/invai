package es.caib.invai.api.externa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ConfigController}. {@code Environment} is mocked directly rather than
 * loading a real properties file, since {@code environment.getProperty} is exactly where Spring
 * would already have resolved any nested {@code ${...}} placeholder - this controller no longer
 * parses the file itself.
 */
@ExtendWith(MockitoExtension.class)
class ConfigControllerTest {

    private static final String KEY_HOST = "es.caib.invai.login.host";

    @Mock
    private Environment environment;

    @Mock
    private MessageSource messageSource;

    private ConfigController configController;

    @BeforeEach
    void setUp() {
        configController = new ConfigController();
        ReflectionTestUtils.setField(configController, "environment", environment);
        ReflectionTestUtils.setField(configController, "messageSource", messageSource);

        lenient().when(messageSource.getMessage(eq("exception.config.hostnotfound"), any(), any()))
                .thenReturn("Unable to read host");
        lenient().when(messageSource.getMessage(eq("exception.config.unexpected"), any(), any()))
                .thenReturn("Unexpected server error");
    }

    @Test
    void getLoggingUrl_hostNotConfigured_returnsUnableToReadHost() {
        when(environment.getProperty(KEY_HOST)).thenReturn(null);

        ResponseEntity<?> response = configController.getLoggingUrl();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unable to read host", response.getBody());
    }

    @Test
    void getLoggingUrl_hostBlank_returnsUnableToReadHost() {
        when(environment.getProperty(KEY_HOST)).thenReturn("   ");

        ResponseEntity<?> response = configController.getLoggingUrl();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unable to read host", response.getBody());
    }

    @Test
    void getLoggingUrl_validHost_returnsAssembledLoginUrl() {
        when(environment.getProperty(KEY_HOST)).thenReturn("https://invai.example.com");

        ResponseEntity<?> response = configController.getLoggingUrl();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("https://invai.example.com/api/auth/login", body.get("url"));
    }

    @Test
    void getLoggingUrl_environmentThrows_returnsUnexpectedServerError() {
        when(environment.getProperty(KEY_HOST)).thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> response = configController.getLoggingUrl();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected server error", response.getBody());
    }
}
