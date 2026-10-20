package es.caib.invai.back.service.model.application.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import es.caib.invai.back.service.model.application.core.Application;

/**
 * Domain model representing the "data" anchor owned by an {@link Application}, covering both its
 * "Open Data" and "Reutilització" tabs. Like its
 * AppDevelopment/AppSecurity/AppAccessibility/AppInformationSystemDb/AppResponsibleAuthorized
 * counterparts, this anchor is auto-created when the owning {@code Application} is created, so it
 * should only be {@code null} for applications created before this anchor was introduced. Carries
 * no swagger/OpenAPI data of its own - that is resolved live on every {@code getById} call (see
 * {@code es.caib.invai.back.ejb.application.data.AppDataServiceFacadeBean}), independently for
 * each tab, fetched from either the application's own external REST API/backend (derived from its
 * {@code code}), or, when {@link #useOpenDataUrl}/{@link #useReuseUrl} is {@code true}, from
 * {@link #openDataUrl}/{@link #reuseUrl} instead.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppData {
    /** Unique identification pointer of this data anchor. */
    private Long id;
    /** Corporate application this data anchor belongs to. */
    private Application application;
    /** Free-text observations about this application's open data / external REST API. */
    private String observation;
    /** Explicit OpenAPI document URL to query instead of the application-name-derived one, used only when {@link #useOpenDataUrl} is {@code true}. */
    private String openDataUrl;
    /** Whether to query {@link #openDataUrl} instead of deriving the URL from the application's own {@code code}. Defaults to {@code false}. */
    private boolean useOpenDataUrl;
    /** Explicit reuse document URL to query instead of the application-name-derived one, used only when {@link #useReuseUrl} is {@code true}. */
    private String reuseUrl;
    /** Whether to query {@link #reuseUrl} instead of deriving the URL from the application's own {@code code}. Defaults to {@code false}. */
    private boolean useReuseUrl;
    /** Timestamp at which this record was created. */
    private LocalDateTime createdAt;
    /** Identifier of the user who created this record. */
    private String createdBy;
    /** Timestamp at which this record was last updated. */
    private LocalDateTime updatedAt;
    /** Identifier of the user who last updated this record. */
    private String updatedBy;
    /** Timestamp at which this record was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
    /** Identifier of the user who logically deleted this record, or {@code null} if still active. */
    private String deletedBy;
}
