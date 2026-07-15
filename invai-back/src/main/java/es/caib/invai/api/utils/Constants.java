package es.caib.invai.api.utils;

/**
 * Global shared immutable constant constants registry catalog dictionary.
 * Consolidates security group security identifier strings, system operational thresholds,
 * internationalization resource bundle property keys, and structured environment properties mappings.
 *
 * @since 1.0.0
 */
public interface Constants {

    /** * Configuration environment property pointer key linking back to the target SSO authentication host.
     */
    String KEY_HOST = "es.caib.invai.login.host";

    // =========================================================================
    // Validation Constraints & Security Fallbacks
    // =========================================================================

    /** * Mandatory physical lower size constraint threshold evaluated during dynamic text asset code parsing.
     */
    int CODE_MIN_LENGTH = 4;

    /** * Fallback literal name string assigned to system-driven historical lifecycle audit events when principal context resolves empty.
     */
    String SYSTEM_USER_FALLBACK = "SYSTEM";

    // =========================================================================
    // Translation Bundle Keys (Targeting message .properties files)
    // =========================================================================

    // --- Application Core Errors ---

    /** * Resource bundle property indicator mapping messages triggered when requesting restricted, non-active application nodes.
     */
    String ERR_APP_NOT_ACTIVE = "exception.application.notactive";

    /** * Resource bundle property indicator tracking messages evaluated during minimum application code length safety validation failures.
     */
    String ERR_CODE_MIN_SIZE = "exception.application.codesize";

    /** * Resource bundle property indicator tracking message templates used when application asset text nomenclature codes collide.
     */
    String ERR_CODE_DUPLICATED = "exception.application.codeduplicated";

    /** * Resource bundle property indicator routing layout validation messages when system-wide routing application prefix tags collide.
     */
    String ERR_PREFIX_DUPLICATED = "exception.application.prefixduplicated";

    /** * Resource bundle property indicator indexing message strings used during direct application data query collection misses.
     */
    String ERR_APP_NOT_FOUND = "exception.application.notfound";

    /** * Resource bundle property indicator parsing messaging blocks applied when a target application asset code belongs to another model node.
     */
    String ERR_CODE_OWNED_BY_OTHER = "exception.application.codeowned";

    /** * Resource bundle property indicator parsing messaging blocks applied when a target routing application prefix belongs to another model node.
     */
    String ERR_PREFIX_OWNED_BY_OTHER = "exception.application.prefixowned";

    // --- New Structural Validations (Overflow Protection) ---

    /** * Resource bundle property indicator used when an application prefix exceeds data engine physical layout limitations (3 characters).
     */
    String ERR_PREFIX_OVERFLOW = "exception.application.prefix.overflow";

    /** * Resource bundle property indicator used when a descriptive application name exceeds structural layout limitations (100 characters).
     */
    String ERR_NAME_OVERFLOW = "exception.application.name.overflow";

    // --- Master Auxiliary Soft-Delete Integrity Blocks ---

    /** * Resource bundle indicator mapping constraint violations when a Category master node cannot be deleted due to active application bindings.
     */
    String ERR_CATEGORY_HAS_APPS = "exception.category.hasapplications";

    /** * Resource bundle indicator mapping constraint violations when a Field/Sector master node cannot be deleted due to active application bindings.
     */
    String ERR_FIELD_HAS_APPS = "exception.field.hasapplications";

    /** * Resource bundle indicator mapping constraint violations when an Administrative Unit master node cannot be deleted due to active application bindings.
     */
    String ERR_ADMUNIT_HAS_APPS = "exception.admunit.hasapplications";

    /** * Resource bundle indicator mapping constraint violations when an IT Commission master node cannot be deleted due to active application bindings.
     */
    String ERR_COMMISSION_HAS_APPS = "exception.commission.hasapplications";

    /** * Resource bundle indicator mapping constraint violations when an Information System Type master node cannot be deleted due to active application bindings.
     */
    String ERR_SYSTEMTYPE_HAS_APPS = "exception.systemtype.hasapplications";

    // =========================================================================
    // Internal Trace Logging Message Templates
    // =========================================================================

    /** * Structured trace diagnostic log layout capturing direct entity lookups by identifier.
     */
    String LOG_FACADE_FETCH_BY_ID = "Facade: Requesting application retrieval for ID: {}";

    /** * Structured trace diagnostic log layout capturing baseline instantiation and validation initialization cycles for new application codes.
     */
    String LOG_FACADE_CREATE = "Facade: Initiating creation for application code: {}";

    /** * Structured trace diagnostic log layout capturing transactional merge operational modifications targeting active application entities.
     */
    String LOG_FACADE_UPDATE = "Facade: Initiating update for application ID: {}";

    /** * Structured trace diagnostic log layout capturing logical lifecycle soft-deletion and structural deactivation execution scopes.
     */
    String LOG_FACADE_DEACTIVATE = "Facade: Executing logical deactivation for ID: {}";
}