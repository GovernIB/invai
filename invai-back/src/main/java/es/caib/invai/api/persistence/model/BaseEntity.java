package es.caib.invai.api.persistence.model;

import es.caib.invai.api.utils.SecurityUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Abstract base class and mapped superclass providing common structural metadata definitions
 * for domain entities, capturing unified creation, modification, and soft-delete attributes.
 * <p>
 * Implements standard JPA lifecycle interceptors to automatically populate audit metadata
 * fields extracted from the active security context's OIDC user principal token data.
 * </p>
 *
 * @since 1.0.0
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "CREATED_BY", length = 50, nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "UPDATED_BY", length = 50)
    private String updatedBy;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    @Column(name = "DELETED_BY", length = 64)
    private String deletedBy;

    /**
     * JPA Interceptor executing operations prior to database entry row persistence.
     * Initializes timestamps and maps identity username claims onto administrative record metadata fields.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        OidcUserInfo currentUser = (OidcUserInfo) SecurityUtils.getCurrentUser();
        if (currentUser.getClaims().get("preferred_username") != null) {
            this.createdBy = (String) currentUser.getClaims().get("preferred_username");
        }
    }

    /**
     * JPA Interceptor executing operations prior to database row updates.
     * Alters timestamp indicators and active identity references unless explicitly flagged as a soft deletion trace.
     */
    @PreUpdate
    protected void onUpdate() {
        if (this.deletedAt == null) {
            this.updatedAt = LocalDateTime.now();
            OidcUserInfo currentUser = (OidcUserInfo) SecurityUtils.getCurrentUser();
            if (currentUser.getClaims().get("preferred_username") != null) {
                this.updatedBy = (String) currentUser.getClaims().get("preferred_username");
            }
        }
    }
}