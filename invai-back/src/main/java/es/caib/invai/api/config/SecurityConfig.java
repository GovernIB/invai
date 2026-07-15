package es.caib.invai.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Core Web Security Configuration using Spring Security.
 * Enables method security, OAuth2 / OIDC login integration via Soffid provider,
 * session management policies, cookies handling, and custom endpoint authorization rules.
 *
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /** The JWK Set URI used to verify and decode JWT tokens from the Identity Provider. */
    @Value("${spring.security.oauth2.client.provider.soffid.jwk-set-uri}")
    private String jwkSetUri;

    /** Target frontend URL redirected to upon successful authentication. */
    @Value("${es.caib.invai.front.url.success:http://127.0.0.1:8080/invaifront/ca/aplicacions}")
    private String loginSuccessUrl;

    /** Corporate Identity Provider (IdP) URL used to process global single logout actions. */
    @Value("${es.caib.invai.idp.logout.url:https://idp.caib.es/logout}")
    private String idpLogoutUrl;

    /**
     * Configures HTTP security filters, routing permissions, session rules,
     * custom logout configurations, and fallback exception entries.
     *
     * @param http the {@link HttpSecurity} builder
     * @param jwtDecoder the JSON Web Token decoder bean
     * @param corsConfigurationSource the CORS policy configuration source
     * @return the built {@link SecurityFilterChain}
     * @throws Exception if an error occurs during chain assembly
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtDecoder jwtDecoder,
                                           CorsConfigurationSource corsConfigurationSource) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .requestCache(RequestCacheConfigurer::disable)
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers(new AntPathRequestMatcher("/externa/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/interna/auth/**")).authenticated()
                                .requestMatchers(new AntPathRequestMatcher("/interna/**")).hasAnyRole("usuari-tipus-E")
                                .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> {
                    oauth.defaultSuccessUrl(this.loginSuccessUrl, false);
                    oauth.userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService(jwtDecoder)));
                })
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl(this.idpLogoutUrl + "?redirect_uri=" + this.loginSuccessUrl)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                )
                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/interna/**")
                        )
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/oauth2/authorization/soffid"),
                                AnyRequestMatcher.INSTANCE
                        )
                );
        return http.build();
    }

    /**
     * Custom OpenID Connect (OIDC) User Service that processes incoming user payloads.
     * Extracts roles from the {@code realm_access.roles} claim inside the Access Token
     * and maps them as Spring Security authorities using the standard {@code ROLE_} prefix.
     *
     * @param jwtDecoder the decoder required to verify access token parameters
     * @return an implementation of {@link OAuth2UserService} for OIDC requests
     */
    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService(JwtDecoder jwtDecoder) {
        OidcUserService delegate = new OidcUserService();
        return userRequest -> {
            OidcUser oidcUser = delegate.loadUser(userRequest);
            OAuth2AccessToken accessToken = userRequest.getAccessToken();
            Jwt jwt = jwtDecoder.decode(accessToken.getTokenValue());

            Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");
            List<String> roles = realmAccess != null ? (List<String>) realmAccess.get("roles") : List.of();

            Set<GrantedAuthority> mappedAuthorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toSet());

            return new DefaultOidcUser(
                    mappedAuthorities,
                    oidcUser.getIdToken(),
                    oidcUser.getUserInfo()
            );
        };
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

    /**
     * Builds a JSON Web Token (JWT) decoder backed by the remote JWK Set URI provided by the Identity Provider.
     *
     * @return the configured {@link JwtDecoder}
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
    }
}