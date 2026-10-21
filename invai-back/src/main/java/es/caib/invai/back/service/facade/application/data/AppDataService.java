package es.caib.invai.back.service.facade.application.data;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.data.DTO.AppDataInputDTO;
import es.caib.invai.back.interna.application.data.DTO.AppDataOutputDTO;
import es.caib.invai.back.interna.application.data.DTO.AppDataUrlInputDTO;
import es.caib.invai.back.interna.application.data.DTO.AppDataUrlOutputDTO;

/**
 * Facade boundary declaring the transactional use cases for managing the "data" anchor (the "Open
 * Data" and "Reutilització" tabs), plus the check of whether a URL of the swagger of a tab answers
 * with a 2xx status code.
 *
 * @since 1.0.5
 */
public interface AppDataService {

    /**
     * Fetches the data anchor by its own primary key, with the URL each of its tabs uses (the one
     * typed in by hand, or the default one). No URL is queried: whether each work is asked, URL by
     * URL, with {@link #checkUrl}.
     *
     * @param id primary key of the data anchor
     * @return the matching anchor, or {@code null} if no anchor exists with this ID
     */
    AppDataOutputDTO getById(Long id);

    /**
     * Queries a URL to find out whether it answers with a 2xx status code. A URL that doesn't work
     * is not an error: it comes back marked as not valid.
     *
     * @param url the URL to check
     * @return the URL queried, and whether it works
     * @throws BusinessRuleException if the URL can't be checked at all (the integration module is unreachable)
     */
    AppDataUrlOutputDTO checkUrl(String url);

    /**
     * Creates a new open data anchor for the application referenced by {@code
     * inputDTO.getApplicationId()}.
     *
     * @param inputDTO creation payload; {@code applicationId} identifies the owning application
     * @return the newly created anchor, mapped to its outbound representation
     */
    AppDataOutputDTO create(AppDataInputDTO inputDTO);

    /**
     * Applies the given payload onto the open data anchor identified by {@code id}: only its
     * observation, since the URL of each tab is set on its own.
     *
     * @param id       primary key of the open data anchor to update
     * @param inputDTO payload with the new field values
     * @return the updated anchor, mapped to its outbound representation
     * @throws BusinessRuleException if no anchor exists with the given ID
     */
    AppDataOutputDTO update(Long id, AppDataInputDTO inputDTO);

    /**
     * Sets the URL of the swagger of the "Open Data" tab (a {@code null} URL goes back to the
     * default one) and queries the URL the tab will use to record whether it answers with a 2xx
     * status code.
     *
     * @param id       primary key of the data anchor
     * @param inputDTO payload carrying the URL
     * @return the URL the tab uses (the one typed in, or else the default one), and whether it works
     * @throws BusinessRuleException if no anchor exists with the given ID
     */
    AppDataUrlOutputDTO updateOpenDataUrl(Long id, AppDataUrlInputDTO inputDTO);

    /**
     * Sets the URL of the swagger of the "Reutilització" tab (a {@code null} URL goes back to the
     * default one) and queries the URL the tab will use to record whether it answers with a 2xx
     * status code.
     *
     * @param id       primary key of the data anchor
     * @param inputDTO payload carrying the URL
     * @return the URL the tab uses (the one typed in, or else the default one), and whether it works
     * @throws BusinessRuleException if no anchor exists with the given ID
     */
    AppDataUrlOutputDTO updateReuseUrl(Long id, AppDataUrlInputDTO inputDTO);

    /**
     * Soft-deletes the open data anchor identified by {@code id}.
     *
     * @param id primary key of the open data anchor to delete
     * @throws BusinessRuleException if no anchor exists with the given ID, or it is already soft-deleted
     */
    void delete(Long id);
}
