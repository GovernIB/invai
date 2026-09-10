package es.caib.invai.back.rest.dir3;

import java.util.List;

/**
 * Outbound port boundary interface declaring read access to the external DIR3CAIB service.
 *
 * @since 1.0.4
 */
public interface Dir3CaibClient {

    /**
     * Fetches the full, flattened tree of organizational units under the given root code.
     *
     * @param rootCode   the root unit's code
     * @param coOfficial whether to prefer the co-official denomination when one exists
     * @return every descendant unit (root included), or an empty list if the root itself is not found
     */
    List<UnidadRest> getTree(String rootCode, boolean coOfficial);
}
