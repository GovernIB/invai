package es.caib.invai.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Web Security Configuration for the public (externa) module.
 * Every endpoint exposed by this module ({@link es.caib.invai.api.externa.ConfigController})
 * is intentionally unauthenticated. This module has no login endpoint of its own: the
 * OAuth2/OIDC login handshake and its trigger controller both live in the internal
 * {@code invai-back} deployment, under its own security config.
 *
 * @since 1.0.1
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures HTTP security to permit all requests, since this module serves no
     * authenticated resources.
     *
     * @param http the {@link HttpSecurity} builder
     * @param corsConfigurationSource the CORS policy configuration source
     * @return the built {@link SecurityFilterChain}
     * @throws Exception if an error occurs during chain assembly
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /**
     * Configures the Spring Security HTTP Firewall rules.
     * Customizes default restrictions to allow semicolons (;) within URLs if requested by legacy integration frameworks.
     *
     * @return the configured {@link StrictHttpFirewall}
     */
    @Bean
    public StrictHttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true);
        return firewall;
    }
}