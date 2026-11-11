package es.caib.invai.api.interna.utils;

/**
 * Shared constants of {@code invai-api-interna}.
 *
 * @since 1.0.7
 */
public interface Constants {

    /** Every role of the application: all of them can use the API, a user with none of them can't. */
    String[] ALL_ROLES = {"INV_USER", "INV_SISTEMES", "INV_DESENVO", "INV_SEGURETAT", "INV_DADES",
            "INV_WS", "INV_ACCESIBILITAT", "INV_SUPER"};
}
