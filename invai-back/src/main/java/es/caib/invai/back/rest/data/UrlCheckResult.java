package es.caib.invai.back.rest.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Outcome of querying a URL published by an application (the swagger of its "Open Data" or
 * "Reutilització" tab), as reported by {@code invai-api-interna}: only whether it answered with a
 * 2xx status code.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UrlCheckResult {

    /** The URL that was queried. */
    private String url;

    /** {@code true} if the URL answered with a 2xx status code; {@code false} for any other outcome. */
    private boolean valid;

    /**
     * The HTTP status code the URL answered with, once any redirect has been followed; {@code null}
     * if no response was obtained at all (malformed or unsupported URL, network failure, timeout).
     */
    private Integer statusCode;
}
