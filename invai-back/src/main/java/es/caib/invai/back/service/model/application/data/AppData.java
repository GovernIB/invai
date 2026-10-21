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
 * no swagger/OpenAPI data of its own: only the URL of each tab's swagger when a person typed one in
 * ({@code null} means the default URL, derived from the application's {@code code}). Whether a URL
 * works is never stored: it is queried every time it is needed (see {@code
 * es.caib.invai.back.ejb.application.data.AppDataServiceFacadeBean}).
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
    /** URL of the swagger of the "Open Data" tab typed in by hand; {@code null} means the tab uses the default one, derived from the application's {@code code}. */
    private String openDataUrl;
    /** URL of the swagger of the "Reutilització" tab typed in by hand; {@code null} means the tab uses the default one, derived from the application's {@code code}. */
    private String reuseUrl;
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
