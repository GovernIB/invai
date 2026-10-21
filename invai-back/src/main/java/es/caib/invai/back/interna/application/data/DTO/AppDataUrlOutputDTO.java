package es.caib.invai.back.interna.application.data.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Outbound result of checking a URL of one of the data tabs ("Open Data" or "Reutilització"): the
 * URL that was queried and whether it works.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppDataUrlOutputDTO {

    /** The URL that was queried. */
    private String url;

    /** Whether {@link #url} answered with a 2xx status code when it was queried. Never stored. */
    private boolean validUrl;
}
