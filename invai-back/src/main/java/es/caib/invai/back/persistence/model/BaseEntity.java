package es.caib.invai.back.persistence.model;

import es.caib.invai.back.utils.SecurityUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract base class and mapped superclass providing common structural metadata definitions
 * for domain entities, capturing unified creation, modification, and soft-delete attributes.
 * <p>
 * Implements standard JPA lifecycle interceptors to automatically populate audit metadata
 * fields extracted from the active security context's OIDC user principal token data.
 * </p>
 *
 * @since 1.0.1
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Timestamp marking when the entity was created. */
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Username (or system fallback identifier) of the actor who created the entity. */
    @Column(name = "CREATED_BY", length = 50, nullable = false, updatable = false)
    private String createdBy;

    /** Timestamp marking the most recent update to the entity, or {@code null} if never updated. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** Username (or system fallback identifier) of the actor who last updated the entity. */
    @Column(name = "UPDATED_BY", length = 50)
    private String updatedBy;

    /** Timestamp marking when the entity was soft-deleted, or {@code null} if not deleted. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Username (or system fallback identifier) of the actor who soft-deleted the entity. */
    @Column(name = "DELETED_BY", length = 64)
    private String deletedBy;

    /**
     * JPA Interceptor executing operations prior to database entry row persistence.
     * Initializes timestamps and maps identity username claims onto administrative record metadata fields.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        String username = getCurrentUsername();
        if (Objects.nonNull(username)) {
            this.createdBy = username;
        }
    }

    /**
     * JPA Interceptor executing operations prior to database row updates.
     * Alters timestamp indicators and active identity references unless explicitly flagged as a soft deletion trace.
     */
    @PreUpdate
    protected void onUpdate() {
        if (Objects.isNull(this.deletedAt)) {
            this.updatedAt = LocalDateTime.now();
            String username = getCurrentUsername();
            if (Objects.nonNull(username)) {
                this.updatedBy = username;
            }
        }
    }

    /**
     * Gets the username of the current actor: the {@code preferred_username} claim of an OpenID
     * Connect user, or the plain username a background job or system routine runs under (see
     * {@link SecurityUtils#getCurrentUser()}), which is not an OIDC user at all.
     *
     * @return the username, or {@code null} if the actor is an OIDC user without that claim
     */
    private static String getCurrentUsername() {
        Object currentUser = SecurityUtils.getCurrentUser();
        if (currentUser instanceof OidcUserInfo userInfo) {
            return (String) userInfo.getClaims().get("preferred_username");
        }
        return currentUser instanceof String username ? username : null;
    }
}