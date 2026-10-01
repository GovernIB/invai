package es.caib.invai.back.exception;

import es.caib.invai.back.rest.soffid.SoffidClient;
import es.caib.invai.back.utils.Constants;
import lombok.Getter;

import java.io.Serial;

/**
 * Thrown when a call to the Soffid SCIM 2.0 API fails for any reason other than a timeout (see
 * {@link SoffidTimeoutException} for that case). Extends {@link BusinessRuleException} so existing
 * callers that already handle/propagate that type (e.g. role-transfer target resolution via
 * {@link SoffidClient#searchByEmail(String)}) keep working unchanged; carries {@link #errorCode},
 * the actual error captured at the call site (an HTTP status code when Soffid responded with one,
 * or the causing exception's simple class name otherwise) so callers that need it - such as
 * {@code PersonService#checkDir3} - can surface the real failure instead of a generic message.
 *
 * @since 1.0.5
 */
@Getter
public class SoffidClientException extends BusinessRuleException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** The real error captured at the call site: an HTTP status code (e.g. {@code "500"}), or the causing exception's simple class name. */
    private final String errorCode;

    /**
     * @param errorCode the real error captured at the call site
     */
    public SoffidClientException(String errorCode) {
        super(Constants.ERR_SOFFID_UNAVAILABLE);
        this.errorCode = errorCode;
    }
}
