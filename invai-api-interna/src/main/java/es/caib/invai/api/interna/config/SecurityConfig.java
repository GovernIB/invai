package es.caib.invai.api.interna.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Web Security Configuration for the integrations module, acting as an OAuth2 Resource Server:
 * it validates the Bearer JWT that {@code invai-back} forwards from its own authenticated OIDC
 * session, without running any login flow of its own.
 *
 * @since 1.0.4
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * OAuth2 client identifier registered with the Identity Provider, used to look up the
     * client-scoped roles claim ({@code resource_access.<client-id>.roles}) inside the JWT.
     */
    @Value("${spring.security.oauth2.client.registration.soffid.client-id}")
    private String clientId;

    /**
     * Configures HTTP security to require a valid Bearer JWT, mapping its roles claim to Spring
     * Security authorities, with no session state kept between requests.
     *
     * @param http the {@link HttpSecurity} builder
     * @return the built {@link SecurityFilterChain}
     * @throws Exception if an error occurs during chain assembly
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().hasAnyRole("INV_SUPER"))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    /**
     * Builds the converter that maps an incoming JWT's {@code resource_access.<client-id>.roles}
     * claim to Spring Security authorities, replicating the same role-extraction logic
     * {@code invai-back}'s {@code OidcUserService} applies to the ID token at login time.
     *
     * @return the configured {@link JwtAuthenticationConverter}
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    /**
     * Extracts the client-scoped roles from the JWT and maps them as Spring Security authorities
     * using the standard {@code ROLE_} prefix.
     *
     * @param jwt the decoded and verified JWT
     * @return the mapped authorities, or an empty set if the roles claim is absent
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");

        Map<String, Object> clientIdResourceAccess = resourceAccess != null
                ? objectMapper.convertValue(resourceAccess.get(clientId), new TypeReference<>() {})
                : null;

        List<String> roles = clientIdResourceAccess != null
                ? objectMapper.convertValue(clientIdResourceAccess.get("roles"), new TypeReference<>() {})
                : List.of();

        return roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
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
