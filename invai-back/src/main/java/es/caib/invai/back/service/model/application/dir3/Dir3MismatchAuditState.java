package es.caib.invai.back.service.model.application.dir3;

/**
 * Lifecycle of the background DIR3 audit of every application.
 *
 * @since 1.0.6
 */
public enum Dir3MismatchAuditState {

    /** No audit has been launched since the application started. */
    IDLE,

    /** An audit is currently running. */
    RUNNING,

    /** The last audit went through every application (some may still have failed individually, see the failed ids). */
    COMPLETED,

    /** The last audit was aborted before going through every application. */
    FAILED
}
