package es.caib.invai.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS (Cross-Origin Resource Sharing) configuration for the INVAI API.
 * Defines allowed origins, HTTP methods, and headers for incoming requests.
 *
 * @since 1.0.0
 */
@Configuration
@Slf4j
public class CorsConfig {

  /** The allowed origin URL injected from the application properties. */
  @Value("${app.cors.allowed-origin}")
  private String allowedOrigin;

  /**
   * Configures the global CORS configuration source applied to all URL paths.
   * Defines standard HTTP methods (GET, POST, etc.) and enables credentials support.
   *
   * @return the configured {@link CorsConfigurationSource}
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    log.info("Configuring CORS with Allowed Origin: {}", allowedOrigin);

    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(allowedOrigin));

    config.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
    ));
    config.setAllowedHeaders(List.of("*"));

    config.setAllowCredentials(true);
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return source;
  }
}