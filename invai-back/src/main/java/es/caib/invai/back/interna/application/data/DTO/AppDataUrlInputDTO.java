package es.caib.invai.back.interna.application.data.DTO;

import es.caib.invai.back.utils.Constants;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Inbound payload for setting the URL of the swagger of one of the data tabs ("Open Data" or
 * "Reutilització"). The URL is the one a person typed in, and must be an absolute {@code http}/
 * {@code https} URL; {@code null} (or blank) makes the tab go back to using the default URL,
 * derived from the application's code.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppDataUrlInputDTO {

    /** The URL typed in by hand, or {@code null} to use the default one. */
    @Size(max = 500, message = "{" + Constants.VALIDATION_APPDATA_URL_OVERFLOW + "}")
    @Pattern(regexp = "^\\s*(https?://\\S+)?\\s*$", message = "{" + Constants.VALIDATION_APPDATA_URL_INVALID + "}")
    private String url;
}
