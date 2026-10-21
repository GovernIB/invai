package es.caib.invai.back.config;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.io.Serial;
import java.util.List;

/**
 * Authentication installed on the thread of a background job (see
 * {@code Dir3BackgroundAuditServiceFacadeBean}), where there is no HTTP request. Its principal is the
 * username of whoever launched the job, so audit fields ({@code createdBy}/{@code updatedBy}) keep
 * attributing the changes to that person; it also keeps the launching user's own login
 * {@link Authentication}, which {@link IntegracionsClientConfig} uses to get (and renew, as it
 * expires) the token that {@code invai-api-interna} is called with.
 *
 * @since 1.0.6
 */
public class BackgroundJobAuthentication extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Username of the person who launched the job. */
    private final String username;

    @Getter
    private final transient Authentication userAuthentication;

    /**
     * @param username           username of the person who launched the job
     * @param userAuthentication the launching user's login authentication
     */
    public BackgroundJobAuthentication(String username, Authentication userAuthentication) {
        super(List.of());
        this.username = username;
        this.userAuthentication = userAuthentication;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }
}
