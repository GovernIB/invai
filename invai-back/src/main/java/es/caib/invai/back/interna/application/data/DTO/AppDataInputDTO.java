package es.caib.invai.back.interna.application.data.DTO;

import es.caib.invai.back.utils.Constants;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Inbound payload for creating or updating a data anchor. {@code applicationId} is the only
 * required field; {@code observation} is always optional. The URL of the swagger of each tab is
 * not part of it: it is set on its own, tab by tab (see {@link AppDataUrlInputDTO}), so that
 * changing one can never touch the other.
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
}
