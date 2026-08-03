package es.caib.invai.api.utils;

/**
 * <p>Global shared immutable constant registry catalog dictionary.</p>
 * <p>Consolidates security group security identifier strings, system operational thresholds,
 * internationalization resource bundle property keys, and structured domain error mappings
 * adhering to the corporate GOIB/CAIB regulatory architecture standards.</p>

 * @since 1.0.1
 */
public interface Constants {

    // =========================================================================
    // GLOBAL CONFIGURATIONS & AUDIT SYSTEM KEYS
    // =========================================================================

    /**
     * Configuration environment property pointer key linking back to the target SSO authentication host.
     */
    String KEY_HOST = "es.caib.invai.login.host";

    /**
     * Mandatory physical lower size constraint threshold evaluated during dynamic text asset code parsing.
     */
    int CODE_MIN_LENGTH = 4;

    /**
     * Fallback literal name string assigned to system-driven historical lifecycle audit events.
     */
    String SYSTEM_USER_FALLBACK = "SYSTEM";

    /**
     * Server type lookup discriminator code identifying servers intended to host database engines.
     */
    String SERVER_TYPE_CODE_DATABASE = "DATABASE";

    /**
     * Server type lookup discriminator code identifying servers intended to host application instances.
     */
    String SERVER_TYPE_CODE_APPLICATION = "APPLICATION";

    // =========================================================================
    // TRANSLATION BUNDLE KEYS (jakarta.validation / message.properties)
    // =========================================================================

    // --- 1. APPLICATION VALIDATIONS ---
    String VALIDATION_APPLICATION_ADM_UNIT_ID = "validation.application.admUnitId";
    String VALIDATION_APPLICATION_CATEGORY_ID = "validation.application.categoryId";
    String VALIDATION_APPLICATION_CODE = "validation.application.code";
    String VALIDATION_APPLICATION_CS_COMMISSION_ID = "validation.application.csCommissionId";
    String VALIDATION_APPLICATION_DEFAULT = "validation.application.default";
    String VALIDATION_APPLICATION_FIELD_ID = "validation.application.fieldId";
    String VALIDATION_APPLICATION_NAME = "validation.application.name";
    String VALIDATION_APPLICATION_PREFIX = "validation.application.prefix";
    String VALIDATION_APPLICATION_STATUS_ID = "validation.application.statusId";
    String VALIDATION_APPLICATION_SYSTEM_TYPE_ID = "validation.application.systemTypeId";
    String VALIDATION_APPLICATION_TITLE = "validation.application.title";

    // --- 2. CATEGORY VALIDATIONS ---
    String VALIDATION_CATEGORY_NAME_ES_REQUIRED = "validation.category.nameEs.required";
    String VALIDATION_CATEGORY_NAME_ES_SIZE = "validation.category.nameEs.size";
    String VALIDATION_CATEGORY_NAME_REQUIRED = "validation.category.name.required";
    String VALIDATION_CATEGORY_NAME_SIZE = "validation.category.name.size";

    // --- 3. FIELD VALIDATIONS ---
    String VALIDATION_FIELD_NAME_ES_REQUIRED = "validation.field.nameEs.required";
    String VALIDATION_FIELD_NAME_ES_SIZE = "validation.field.nameEs.size";
    String VALIDATION_FIELD_NAME_REQUIRED = "validation.field.name.required";
    String VALIDATION_FIELD_NAME_SIZE = "validation.field.name.size";

    // --- 4. ADMINISTRATIVE UNIT (ADMUNIT) VALIDATIONS ---
    String VALIDATION_ADMUNIT_CODE_REQUIRED = "validation.admunit.code.required";
    String VALIDATION_ADMUNIT_CODE_SIZE = "validation.admunit.code.size";
    String VALIDATION_ADMUNIT_NAME_ES_REQUIRED = "validation.admunit.nameEs.required";
    String VALIDATION_ADMUNIT_NAME_ES_SIZE = "validation.admunit.nameEs.size";
    String VALIDATION_ADMUNIT_NAME_REQUIRED = "validation.admunit.name.required";
    String VALIDATION_ADMUNIT_NAME_SIZE = "validation.admunit.name.size";

    // --- 5. COMMISSION VALIDATIONS ---
    String VALIDATION_COMMISSION_APPROVAL_DATE_REQUIRED = "validation.commission.approvalDate.required";
    String VALIDATION_COMMISSION_EXPEDIENT_NUMBER_REQUIRED = "validation.commission.expedientNumber.required";
    String VALIDATION_COMMISSION_EXPEDIENT_NUMBER_SIZE = "validation.commission.expedientNumber.size";
    String VALIDATION_COMMISSION_NAME_ES_REQUIRED = "validation.commission.nameEs.required";
    String VALIDATION_COMMISSION_NAME_ES_SIZE = "validation.commission.nameEs.size";
    String VALIDATION_COMMISSION_NAME_REQUIRED = "validation.commission.name.required";
    String VALIDATION_COMMISSION_NAME_SIZE = "validation.commission.name.size";
    String VALIDATION_COMMISSION_TYPE_REQUIRED = "validation.commission.commissionType.required";

    // --- 6. SYSTEM TYPE VALIDATIONS ---
    String VALIDATION_SYSTEMTYPE_NAME_ES_REQUIRED = "validation.systemtype.nameEs.required";
    String VALIDATION_SYSTEMTYPE_NAME_ES_SIZE = "validation.systemtype.nameEs.size";
    String VALIDATION_SYSTEMTYPE_NAME_REQUIRED = "validation.systemtype.name.required";
    String VALIDATION_SYSTEMTYPE_NAME_SIZE = "validation.systemtype.name.size";

    // --- 7. DATABASE VALIDATIONS ---
    String VALIDATION_DATABASE_DATABASE_TYPE_ID = "validation.database.databaseTypeId";
    String VALIDATION_DATABASE_PORT = "validation.database.port";
    String VALIDATION_DATABASE_SERVER = "validation.database.server";
    String VALIDATION_DATABASE_SERVER_OVERFLOW = "validation.database.server.overflow";
    String VALIDATION_DATABASE_SERVICE = "validation.database.service";
    String VALIDATION_DATABASE_SERVICE_OVERFLOW = "validation.database.service.overflow";

    // --- 8. SYSTEM VALIDATIONS ---
    String VALIDATION_SYSTEM_INSTANCE = "validation.system.instance";
    String VALIDATION_SYSTEM_INSTANCE_OVERFLOW = "validation.system.instance.overflow";
    String VALIDATION_SYSTEM_SERVER_ID = "validation.system.serverId";
    String VALIDATION_SYSTEM_PORT = "validation.system.port";
    String VALIDATION_SYSTEM_PORT_OUT_OF_BOUNDS = "validation.system.port.outofbounds";
    String VALIDATION_SYSTEM_VERSION = "validation.system.version";
    String VALIDATION_SYSTEM_VERSION_OVERFLOW = "validation.system.version.overflow";

    // --- 9. ENVIRONMENT VALIDATIONS ---
    String VALIDATION_ENVIRONMENT_CODE = "validation.environment.code";
    String VALIDATION_ENVIRONMENT_CODE_OVERFLOW = "validation.environment.code.overflow";
    String VALIDATION_ENVIRONMENT_NAME = "validation.environment.name";
    String VALIDATION_ENVIRONMENT_NAME_OVERFLOW = "validation.environment.name.overflow";
    String VALIDATION_ENVIRONMENT_NAME_ES = "validation.environment.name_es";
    String VALIDATION_ENVIRONMENT_NAME_ES_OVERFLOW = "validation.environment.name_es.overflow";

    // --- 10. APPLICATION-SYSTEM LINK VALIDATIONS ---
    String VALIDATION_APPLICATIONSYSTEM_INFORMATION_SYSTEM_DB = "validation.applicationsystem.informationSystemDbId";
    String VALIDATION_APPLICATIONSYSTEM_SYSTEM = "validation.applicationsystem.system";

    // --- 11. APPLICATION-DATABASE LINK VALIDATIONS ---
    String VALIDATION_APPDATABASE_INFORMATION_SYSTEM_DB_ID = "validation.appdatabase.informationSystemDbId";
    String VALIDATION_APPDATABASE_DATABASE_ID = "validation.appdatabase.databaseId";

    // --- 12. APPLICATION INFORMATION SYSTEM DATABASE VALIDATIONS ---
    String VALIDATION_APPINFORMATIONSYSTEMDB_APPLICATION_ID = "validation.appinformationsystemdb.applicationId";

    // --- 13. APPLICATION DEVELOPMENT VALIDATIONS ---
    String VALIDATION_DEVELOPMENT_APPLICATION_ID = "validation.development.applicationId";
    String VALIDATION_DEVELOPMENT_ENVIRONMENT_ID = "validation.development.environmentId";
    String VALIDATION_DEVELOPMENT_MODALITY_ID = "validation.development.modalityId";
    String VALIDATION_DEVELOPMENT_CODE_REQUIRED = "validation.development.code.required";
    String VALIDATION_DEVELOPMENT_CODE_OVERFLOW = "validation.development.code.overflow";
    String VALIDATION_DEVELOPMENT_STANDARD_ADAPTION_ID = "validation.development.standardAdaptionId";
    String VALIDATION_DEVELOPMENT_REVISION_DATE = "validation.development.revisionDate";
    String VALIDATION_DEVELOPMENT_OBSERVATION_REQUIRED = "validation.development.observation.required";

    // --- 14. APPLICATION DEVELOPMENT PROVIDER VALIDATIONS ---
    String VALIDATION_PROVIDER_APP_DEVELOPMENT_ID = "validation.provider.appDevelopmentId";
    String VALIDATION_PROVIDER_COMPANY_NAME_REQUIRED = "validation.provider.companyName.required";
    String VALIDATION_PROVIDER_COMPANY_NAME_OVERFLOW = "validation.provider.companyName.overflow";
    String VALIDATION_PROVIDER_ROLE_OVERFLOW = "validation.provider.role.overflow";

    // --- 15. APPLICATION DEVELOPMENT TECHNOLOGY VALIDATIONS ---
    String VALIDATION_TECHNOLOGY_APP_DEVELOPMENT_ID = "validation.technology.appDevelopmentId";
    String VALIDATION_TECHNOLOGY_LAYER_REQUIRED = "validation.technology.layer.required";
    String VALIDATION_TECHNOLOGY_LAYER_OVERFLOW = "validation.technology.layer.overflow";
    String VALIDATION_TECHNOLOGY_TECHNOLOGY_REQUIRED = "validation.technology.technology.required";
    String VALIDATION_TECHNOLOGY_TECHNOLOGY_OVERFLOW = "validation.technology.technology.overflow";
    String VALIDATION_TECHNOLOGY_VERSION_REQUIRED = "validation.technology.version.required";
    String VALIDATION_TECHNOLOGY_VERSION_OVERFLOW = "validation.technology.version.overflow";
    String VALIDATION_TECHNOLOGY_ARCHITECTURE_REQUIRED = "validation.technology.architecture.required";
    String VALIDATION_TECHNOLOGY_ARCHITECTURE_OVERFLOW = "validation.technology.architecture.overflow";

    // --- 16. SERVER VALIDATIONS ---
    String VALIDATION_SERVER_NAME = "validation.server.name";
    String VALIDATION_SERVER_NAME_OVERFLOW = "validation.server.name.overflow";
    String VALIDATION_SERVER_ENVIRONMENT_ID = "validation.server.environmentId";
    String VALIDATION_SERVER_SERVER_TYPE_ID = "validation.server.serverTypeId";

    // --- 17. DATABASE VENDOR VALIDATIONS ---
    String VALIDATION_DATABASEVENDOR_NAME = "validation.databasevendor.name";
    String VALIDATION_DATABASEVENDOR_NAME_OVERFLOW = "validation.databasevendor.name.overflow";
    String VALIDATION_DATABASEVENDOR_DEFAULT_PORT = "validation.databasevendor.defaultPort";

    // --- 18. ROLE VALIDATIONS ---
    String VALIDATION_ROLE_NAME_REQUIRED = "validation.role.name.required";
    String VALIDATION_ROLE_NAME_SIZE = "validation.role.name.size";
    String VALIDATION_ROLE_NAME_ES_SIZE = "validation.role.nameEs.size";

    // --- 19. LAYER VALIDATIONS ---
    String VALIDATION_LAYER_NAME_REQUIRED = "validation.layer.name.required";
    String VALIDATION_LAYER_NAME_SIZE = "validation.layer.name.size";

    // --- 20. TECHNOLOGY CATALOG VALIDATIONS ---
    String VALIDATION_TECHNOLOGYCATALOG_NAME_REQUIRED = "validation.technologycatalog.name.required";
    String VALIDATION_TECHNOLOGYCATALOG_NAME_SIZE = "validation.technologycatalog.name.size";
    String VALIDATION_TECHNOLOGYCATALOG_LAYER_ID = "validation.technologycatalog.layerId";

    // --- 21. APPLICATION DEVELOPMENT PROVIDER / TECHNOLOGY FK VALIDATIONS ---
    String VALIDATION_PROVIDER_ROLE_ID = "validation.provider.roleId";
    String VALIDATION_TECHNOLOGY_LAYER_ID = "validation.technology.layerId";
    String VALIDATION_TECHNOLOGY_TECHNOLOGY_ID = "validation.technology.technologyId";

    // =========================================================================
    // EXCEPTION TRANSLATION BUNDLE KEYS (Business Rules Exceptions)
    // =========================================================================

    // --- APPLICATION ERRORS ---
    String ERR_APP_ACTIVE = "exception.application.active";
    String ERR_APP_NOT_ACTIVE = "exception.application.notactive";
    String ERR_APP_NOT_FOUND = "exception.application.notfound";
    String ERR_APPLICATION_CODE_SIZE = "exception.application.codesize";
    String ERR_APPLICATION_NAME_OVERFLOW = "exception.application.name.overflow";
    String ERR_APPLICATION_PREFIX_OVERFLOW = "exception.application.prefix.overflow";
    String ERR_CODE_DUPLICATED = "exception.application.codeduplicated";
    String ERR_CODE_OWNED_BY_OTHER = "exception.application.codeowned";
    String ERR_PREFIX_DUPLICATED = "exception.application.prefixduplicated";
    String ERR_PREFIX_OWNED_BY_OTHER = "exception.application.prefixowned";

    // --- CATEGORY ERRORS ---
    String ERR_CATEGORY_ACTIVE = "exception.category.active";
    String ERR_CATEGORY_DELETE_HAS_DEPENDENCIES = "exception.category.delete.hasdependencies";
    String ERR_CATEGORY_DUPLICATED = "exception.category.duplicated";
    String ERR_CATEGORY_DUPLICATED_ES = "exception.category.duplicatedEs";
    String ERR_CATEGORY_HAS_APPLICATIONS = "exception.category.hasapplications";
    String ERR_CATEGORY_NOT_ACTIVE = "exception.category.notactive";
    String ERR_CATEGORY_NOT_FOUND = "exception.category.notfound";

    // --- FIELD ERRORS ---
    String FIELD_ACTIVE = "exception.field.active";
    String FIELD_DELETE_HAS_DEPENDENCIES = "exception.field.delete.hasdependencies";
    String FIELD_DUPLICATED = "exception.field.duplicated";
    String FIELD_DUPLICATED_ES = "exception.field.duplicatedEs";
    String FIELD_HAS_APPLICATIONS = "exception.field.hasapplications";
    String FIELD_NOT_ACTIVE = "exception.field.notactive";
    String FIELD_NOT_FOUND = "exception.field.notfound";

    // --- ADMINISTRATIVE UNIT (ADMUNIT) ERRORS ---
    String ERR_ADMUNIT_ACTIVE = "exception.admunit.active";
    String ERR_ADMUNIT_CODE_DUPLICATED = "exception.admunit.code.duplicated";
    String ERR_ADMUNIT_DELETE_HAS_DEPENDENCIES = "exception.admunit.delete.hasdependencies";
    String ERR_ADMUNIT_DUPLICATED = "exception.admunit.duplicated";
    String ERR_ADMUNIT_DUPLICATED_ES = "exception.admunit.duplicatedEs";
    String ERR_ADMUNIT_HAS_APPLICATIONS = "exception.admunit.hasapplications";
    String ERR_ADMUNIT_NOT_ACTIVE = "exception.admunit.notactive";
    String ERR_ADMUNIT_NOT_FOUND = "exception.admunit.notfound";

    // --- COMMISSION ERRORS ---
    String ERR_COMMISSION_ACTIVE = "exception.commission.active";
    String ERR_COMMISSION_DELETE_HAS_DEPENDENCIES = "exception.commission.delete.hasdependencies";
    String ERR_COMMISSION_DUPLICATED = "exception.commission.duplicated";
    String ERR_COMMISSION_DUPLICATED_ES = "exception.commission.duplicatedEs";
    String ERR_COMMISSION_HAS_APPLICATIONS = "exception.commission.hasapplications";
    String ERR_COMMISSION_NOT_FOUND = "exception.commission.notfound";

    // --- SYSTEM TYPE ERRORS ---
    String SYSTEM_TYPE_ACTIVE = "exception.systemtype.active";
    String SYSTEM_TYPE_DELETE_HAS_DEPENDENCIES = "exception.systemtype.delete.hasdependencies";
    String SYSTEM_TYPE_DUPLICATED = "exception.systemtype.duplicated";
    String SYSTEM_TYPE_DUPLICATED_ES = "exception.systemtype.duplicatedEs";
    String SYSTEM_TYPE_HAS_APPLICATIONS = "exception.systemtype.hasapplications";
    String SYSTEM_TYPE_NOT_ACTIVE = "exception.systemtype.notactive";
    String SYSTEM_TYPE_NOT_FOUND = "exception.systemtype.notfound";

    // --- DATABASE ERRORS ---
    String ERR_DATABASE_ACTIVE = "exception.database.active";
    String ERR_DATABASE_DUPLICATED = "exception.database.duplicated";
    String ERR_DATABASE_NOT_ACTIVE = "exception.database.notactive";
    String ERR_DATABASE_NOT_FOUND = "exception.database.notfound";
    String ERR_DATABASE_SERVER_AND_SERVICE_OWNED_BY_OTHER = "exception.database.serverandservice.owned";
    String ERR_DATABASE_SERVER_TYPE_INVALID = "exception.database.servertype.invalid";

    // --- SYSTEM CORE ERRORS ---
    String ERR_SYSTEM_ACTIVE = "exception.system.active";
    String ERR_SYSTEM_NAME_AND_INSTANCE_DUPLICATED = "exception.system.nameandinstance.duplicated";
    String ERR_SYSTEM_NAME_AND_INSTANCE_OWNED_BY_OTHER = "exception.system.nameandinstance.owned";
    String ERR_SYSTEM_NOT_ACTIVE = "exception.system.notactive";
    String ERR_SYSTEM_NOT_FOUND = "exception.system.notfound";
    String ERR_SYSTEM_SERVER_TYPE_INVALID = "exception.system.servertype.invalid";

    // --- ENVIRONMENT CORE ERRORS ---
    String ERR_ENVIRONMENT_ACTIVE = "exception.environment.active";
    String ERR_ENVIRONMENT_DUPLICATED = "exception.environment.duplicated";
    String ERR_ENVIRONMENT_NOT_FOUND = "exception.environment.notfound";

    // --- APPLICATION SYSTEM LINK ERRORS ---
    String ERR_APPLICATIONSYSTEM_LINK_DUPLICATED = "exception.applicationsystem.duplicated";
    String ERR_APPLICATIONSYSTEM_LINK_OWNED_BY_OTHER = "exception.applicationsystem.owned";
    String ERR_APPLICATIONSYSTEM_NOT_ACTIVE = "exception.applicationsystem.notactive";
    String ERR_APPLICATIONSYSTEM_NOT_FOUND = "exception.applicationsystem.notfound";

    // --- APPLICATION DATABASE LINK ERRORS ---
    String ERR_APP_DATABASE_DUPLICATED = "exception.appdatabase.duplicated";
    String ERR_APP_DATABASE_NOT_ACTIVE = "exception.appdatabase.notactive";
    String ERR_APP_DATABASE_NOT_FOUND = "exception.database.notfound";

    // --- APPLICATION INFORMATION SYSTEM DATABASE ERRORS ---
    String ERR_APP_INFORMATION_SYSTEM_DB_NOT_ACTIVE = "exception.appinformationsystemdb.notactive";
    String ERR_APP_INFORMATION_SYSTEM_DB_NOT_FOUND = "exception.appinformationsystemdb.notfound";

    // --- APPLICATION DEVELOPMENT ERRORS ---
    String ERR_DEVELOPMENT_NOT_ACTIVE = "exception.development.notactive";
    String ERR_DEVELOPMENT_NOT_FOUND = "exception.development.notfound";

    // --- APPLICATION DEVELOPMENT PROVIDER ERRORS ---
    String ERR_PROVIDER_NOT_ACTIVE = "exception.provider.notactive";
    String ERR_PROVIDER_NOT_FOUND = "exception.provider.notfound";

    // --- APPLICATION DEVELOPMENT TECHNOLOGY ERRORS ---
    String ERR_TECHNOLOGY_NOT_ACTIVE = "exception.technology.notactive";
    String ERR_TECHNOLOGY_NOT_FOUND = "exception.technology.notfound";

    // --- SERVER CORE ERRORS ---
    String ERR_SERVER_ACTIVE = "exception.server.active";
    String ERR_SERVER_DUPLICATED = "exception.server.duplicated";
    String ERR_SERVER_OWNED_BY_OTHER = "exception.server.owned";
    String ERR_SERVER_NOT_ACTIVE = "exception.server.notactive";
    String ERR_SERVER_NOT_FOUND = "exception.server.notfound";

    // --- DATABASE VENDOR CORE ERRORS ---
    String ERR_DATABASEVENDOR_ACTIVE = "exception.databasevendor.active";
    String ERR_DATABASEVENDOR_DUPLICATED = "exception.databasevendor.duplicated";
    String ERR_DATABASEVENDOR_OWNED_BY_OTHER = "exception.databasevendor.owned";
    String ERR_DATABASEVENDOR_NOT_ACTIVE = "exception.databasevendor.notactive";
    String ERR_DATABASEVENDOR_NOT_FOUND = "exception.databasevendor.notfound";

    // --- ROLE CORE ERRORS ---
    String ERR_ROLE_ACTIVE = "exception.role.active";
    String ERR_ROLE_DELETE_HAS_DEPENDENCIES = "exception.role.delete.hasdependencies";
    String ERR_ROLE_DUPLICATED = "exception.role.duplicated";
    String ERR_ROLE_NOT_ACTIVE = "exception.role.notactive";
    String ERR_ROLE_NOT_FOUND = "exception.role.notfound";

    // --- LAYER CORE ERRORS ---
    String ERR_LAYER_ACTIVE = "exception.layer.active";
    String ERR_LAYER_DELETE_HAS_DEPENDENCIES = "exception.layer.delete.hasdependencies";
    String ERR_LAYER_DUPLICATED = "exception.layer.duplicated";
    String ERR_LAYER_NOT_ACTIVE = "exception.layer.notactive";
    String ERR_LAYER_NOT_FOUND = "exception.layer.notfound";

    // --- TECHNOLOGY CATALOG CORE ERRORS ---
    String ERR_TECHNOLOGYCATALOG_ACTIVE = "exception.technologycatalog.active";
    String ERR_TECHNOLOGYCATALOG_DELETE_HAS_DEPENDENCIES = "exception.technologycatalog.delete.hasdependencies";
    String ERR_TECHNOLOGYCATALOG_DUPLICATED = "exception.technologycatalog.duplicated";
    String ERR_TECHNOLOGYCATALOG_NOT_ACTIVE = "exception.technologycatalog.notactive";
    String ERR_TECHNOLOGYCATALOG_NOT_FOUND = "exception.technologycatalog.notfound";

    // --- GLOBAL / AUXILIARY ERROR CODES ---
    String ERR_AUXILIARY_DUPLICATED = "exception.auxiliary.duplicated";
    String ERR_AUXILIARY_NOT_FOUND = "exception.auxiliary.notfound";

    // =========================================================================
    // TRACE DIAGNOSTIC LOG STRINGS & PATTERNS
    // =========================================================================

    /**
     * Structured trace diagnostic log layout capturing baseline instantiation and validation.
     */
    String LOG_FACADE_CREATE = "Facade: Initiating creation for application code: {}";

    /**
     * Structured trace diagnostic log layout capturing logical lifecycle soft-deletion.
     */
    String LOG_FACADE_DEACTIVATE = "Facade: Executing logical deactivation for ID: {}";

    /**
     * Structured trace diagnostic log layout capturing direct entity lookups by identifier.
     */
    String LOG_FACADE_FETCH_BY_ID = "Facade: Requesting application retrieval for ID: {}";

    /**
     * Structured trace diagnostic log layout capturing transactional merge operational modifications.
     */
    String LOG_FACADE_UPDATE = "Facade: Initiating update for application ID: {}";
}