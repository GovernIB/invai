package es.caib.invai.back.interna.application.data.DTO;

import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbound representation of a data anchor, covering both its "Open Data" and "Reutilització"
 * tabs. {@code openData} and {@code reuse} are never persisted: each is resolved live, on every
 * {@code getById} call, from the owning application's own external REST API / backend (see
 * {@code es.caib.invai.back.ejb.application.data.AppDataServiceFacadeBean}) - only {@code id},
 * {@code application}, {@code observation}, {@code deletedAt}, and the four URL override fields
 * come from the database.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppDataOutputDTO {

    /** Unique identification pointer of this data anchor. */
    private Long id;
    /** Corporate application this data anchor belongs to. */
    private ApplicationOutputDTO application;
    /** Free-text observations about this application's open data / external REST API. */
    private String observation;
    /**
     * Explicit OpenAPI document URL queried instead of the application-name-derived one, when
     * {@link #useOpenDataUrl} is {@code true}.
     */
    private String openDataUrl;
    /**
     * Whether {@link #openDataUrl} is queried instead of the URL derived from the application's
     * own {@code code}.
     */
    private boolean useOpenDataUrl;
    /**
     * Every GET endpoint published by the queried OpenAPI 3 document - either the application's
     * own external REST API, derived from its {@code name} (racing every known convention, e.g.
     * {@code https://intranet.caib.es/{application.name}api/externa/swagger.json}, {@code
     * .../openapi.json}, {@code .../api-docs.json}), or, when {@link #useOpenDataUrl} is {@code
     * true}, {@link #openDataUrl} itself. Empty when the document genuinely publishes zero GET
     * endpoints; {@code null} if this tab's own live fetch failed while {@link #reuse}'s
     * succeeded - only when both fail does the failure propagate as an exception instead (see
     * {@code es.caib.invai.back.ejb.application.data.AppDataServiceFacadeBean#getById}).
     */
    private List<OpenDataEndpointOutputDTO> openData;
    /**
     * Explicit reuse document URL queried instead of the application-name-derived one, when
     * {@link #useReuseUrl} is {@code true}.
     */
    private String reuseUrl;
    /**
     * Whether {@link #reuseUrl} is queried instead of the URL derived from the application's
     * own {@code code}.
     */
    private boolean useReuseUrl;
    /**
     * Every GET endpoint published by the queried reuse document - either the application's own
     * backend, derived from its {@code name} (at {@code
     * https://intranet.caib.es/{application.name}back}), or, when {@link #useReuseUrl} is
     * {@code true}, {@link #reuseUrl} itself. Same GET-only filtering and failure handling as
     * {@link #openData}: {@code null} if this tab's own live fetch failed while {@link
     * #openData}'s succeeded.
     */
    private List<OpenDataEndpointOutputDTO> reuse;
    /** Timestamp at which this record was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
