package es.caib.invai.back.interna.application.data.DTO;

import es.caib.invai.back.utils.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for creating or updating a data anchor. {@code applicationId} is the
 * only field enforced by bean validation ({@code @NotNull}); {@code observation}, {@code
 * openDataUrl}, and {@code reuseUrl} are always optional. {@code useOpenDataUrl}/{@code
 * useReuseUrl} default to {@code false} (a client omitting either entirely gets {@code false});
 * when submitted as {@code true}, the facade requires the matching URL field to be non-blank (see
 * {@code AppDataServiceFacadeBean}).
 *
 * @since 1.0.5
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppDataInputDTO {

    /** Identifier of the application this anchor belongs to; required, and immutable after creation. */
    @NotNull(message = "{" + Constants.VALIDATION_APPDATA_APPLICATIONID + "}")
    private Long applicationId;

    /** Free-text observations about this application's open data / external REST API; never required. */
    private String observation;

    /**
     * Explicit OpenAPI document URL to query instead of the application-name-derived one; only
     * required when {@link #useOpenDataUrl} is {@code true}.
     */
    @Size(max = 500, message = "{" + Constants.VALIDATION_APPDATA_OPENDATAURL_OVERFLOW + "}")
    private String openDataUrl;

    /**
     * Whether to query {@link #openDataUrl} instead of deriving the URL from the application's
     * own {@code code}. Defaults to {@code false}.
     */
    private boolean useOpenDataUrl;

    /**
     * Explicit reuse document URL to query instead of the application-name-derived one; only
     * required when {@link #useReuseUrl} is {@code true}.
     */
    @Size(max = 500, message = "{" + Constants.VALIDATION_APPDATA_REUSEURL_OVERFLOW + "}")
    private String reuseUrl;

    /**
     * Whether to query {@link #reuseUrl} instead of deriving the URL from the application's
     * own {@code code}. Defaults to {@code false}.
     */
    private boolean useReuseUrl;
}
