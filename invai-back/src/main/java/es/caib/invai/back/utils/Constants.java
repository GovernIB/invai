package es.caib.invai.back.utils;

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
     * Mandatory physical lower size constraint threshold evaluated during dynamic text asset code parsing.
     */
    int CODE_MIN_LENGTH = 4;

    /**
     * Upper size constraint for {@code Application.code}, matching the {@code INV_APPLICATION.CODE
     * VARCHAR2(10 CHAR)} column so bean validation rejects an oversized code before it ever reaches
     * the database.
     */
    int APPLICATION_CODE_MAX_LENGTH = 10;

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

    /** Generic client-facing message for an unhandled persistence-layer failure; never exposes the raw DB error. */
    String ERR_PERSISTENCE_GENERIC = "exception.persistence.generic";

    /** Generic client-facing message for any otherwise-unhandled server-side exception. */
    String ERR_UNEXPECTED_GENERIC = "exception.unexpected.generic";

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

    // --- 12. APPLICATION SECURITY / INFORMATION SYSTEM DATABASE ANCHOR VALIDATIONS ---
    String VALIDATION_APP_SECURITY_APPLICATION_ID = "validation.appsecurity.applicationId";
    String VALIDATION_APP_INFORMATION_SYSTEM_DB_APPLICATION_ID = "validation.appinformationsystemdb.applicationId";

    // --- 13. APPLICATION DEVELOPMENT VALIDATIONS ---
    String VALIDATION_DEVELOPMENT_APPLICATION_ID = "validation.development.applicationId";
    String VALIDATION_DEVELOPMENT_ENVIRONMENT_ID = "validation.development.environmentId";
    String VALIDATION_DEVELOPMENT_MODALITY_ID = "validation.development.modalityId";
    String VALIDATION_DEVELOPMENT_CODE_REQUIRED = "validation.development.code.required";
    String VALIDATION_DEVELOPMENT_CODE_OVERFLOW = "validation.development.code.overflow";
    String VALIDATION_DEVELOPMENT_STANDARD_ADAPTION_ID = "validation.development.standardAdaptionId";
    String VALIDATION_DEVELOPMENT_REVISION_DATE = "validation.development.revisionDate";

    // --- 14. APPLICATION DEVELOPMENT PROVIDER VALIDATIONS ---
    String VALIDATION_PROVIDER_APP_DEVELOPMENT_ID = "validation.provider.appDevelopmentId";
    String VALIDATION_PROVIDER_COMPANY_NAME_REQUIRED = "validation.provider.companyName.required";
    String VALIDATION_PROVIDER_COMPANY_NAME_OVERFLOW = "validation.provider.companyName.overflow";

    // --- 15. APPLICATION DEVELOPMENT TECHNOLOGY VALIDATIONS ---
    String VALIDATION_TECHNOLOGY_APP_DEVELOPMENT_ID = "validation.technology.appDevelopmentId";
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
    String VALIDATION_TECHNOLOGY_LAYER_ID = "validation.technology.layerId";
    String VALIDATION_TECHNOLOGY_TECHNOLOGY_ID = "validation.technology.technologyId";

    // --- 22. COMPANY VALIDATIONS ---
    String VALIDATION_COMPANY_NAME_REQUIRED = "validation.company.name.required";
    String VALIDATION_COMPANY_NAME_SIZE = "validation.company.name.size";

    // --- 23. PERSON VALIDATIONS ---
    String VALIDATION_PERSON_FIRSTNAME_REQUIRED = "validation.person.firstname.required";
    String VALIDATION_PERSON_FIRSTNAME_SIZE = "validation.person.firstname.size";
    String VALIDATION_PERSON_LASTNAME_REQUIRED = "validation.person.lastname.required";
    String VALIDATION_PERSON_LASTNAME_SIZE = "validation.person.lastname.size";
    String VALIDATION_PERSON_EMAIL_REQUIRED = "validation.person.email.required";
    String VALIDATION_PERSON_EMAIL_SIZE = "validation.person.email.size";

    // --- 24. AUTHORIZATION TYPE VALIDATIONS ---
    String VALIDATION_AUTHORIZATIONTYPE_NAME_REQUIRED = "validation.authorizationtype.name.required";
    String VALIDATION_AUTHORIZATIONTYPE_NAME_SIZE = "validation.authorizationtype.name.size";
    String VALIDATION_AUTHORIZATIONTYPE_NAME_ES_REQUIRED = "validation.authorizationtype.nameEs.required";
    String VALIDATION_AUTHORIZATIONTYPE_NAME_ES_SIZE = "validation.authorizationtype.nameEs.size";

    // --- 26. APPLICATION RESPONSIBLE VALIDATIONS ---
    String VALIDATION_APPRESPONSIBLE_APP_RESPONSIBLE_AUTHORIZED_ID = "validation.appresponsible.appResponsibleAuthorizedId";
    String VALIDATION_APPRESPONSIBLE_RESPONSIBLE_TYPE_ID = "validation.appresponsible.responsibleTypeId";

    // --- 27. APPLICATION AUTHORIZED VALIDATIONS ---
    String VALIDATION_APPAUTHORIZED_APP_RESPONSIBLE_AUTHORIZED_ID = "validation.appauthorized.appResponsibleAuthorizedId";
    String VALIDATION_APPAUTHORIZED_AUTHORIZATION_TYPE_IDS = "validation.appauthorized.authorizationTypeIds";

    // --- 28. ROLE TRANSFER VALIDATIONS ---
    String VALIDATION_ROLETRANSFER_ITEMS = "validation.roletransfer.items";
    String VALIDATION_ROLETRANSFER_ITEM_ID = "validation.roletransfer.item.id";
    String VALIDATION_ROLETRANSFER_ITEM_TYPE = "validation.roletransfer.item.type";

    // --- 29. IDENTITY PROVIDER VALIDATIONS ---
    String VALIDATION_IDENTITYPROVIDER_NAME_REQUIRED = "validation.identityprovider.name.required";
    String VALIDATION_IDENTITYPROVIDER_NAME_SIZE = "validation.identityprovider.name.size";

    // --- 30. PERSONAL DATA PROCESSING VALIDATIONS ---
    String VALIDATION_PERSONALDATAPROCESSING_NAME_REQUIRED = "validation.personaldataprocessing.name.required";
    String VALIDATION_PERSONALDATAPROCESSING_NAME_SIZE = "validation.personaldataprocessing.name.size";
    String VALIDATION_PERSONALDATAPROCESSING_NAME_ES_REQUIRED = "validation.personaldataprocessing.nameEs.required";
    String VALIDATION_PERSONALDATAPROCESSING_NAME_ES_SIZE = "validation.personaldataprocessing.nameEs.size";

    // --- 31. WEB CONTEXT VALIDATIONS ---
    String VALIDATION_WEBCONTEXT_NAME_REQUIRED = "validation.webcontext.name.required";
    String VALIDATION_WEBCONTEXT_NAME_SIZE = "validation.webcontext.name.size";
    String VALIDATION_WEBCONTEXT_NAME_ES_REQUIRED = "validation.webcontext.nameEs.required";
    String VALIDATION_WEBCONTEXT_NAME_ES_SIZE = "validation.webcontext.nameEs.size";

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
    String ERR_APPLICATION_ADMUNIT_NOT_FOUND = "exception.application.admunit.notfound";
    String ERR_APPLICATION_ADMUNIT_MUST_BE_DEPARTMENT_CHILD = "exception.application.admunit.mustbedepartmentchild";
    String ERR_CODE_DUPLICATED = "exception.application.codeduplicated";
    String ERR_CODE_OWNED_BY_OTHER = "exception.application.codeowned";
    String ERR_PREFIX_DUPLICATED = "exception.application.prefixduplicated";
    String ERR_PREFIX_OWNED_BY_OTHER = "exception.application.prefixowned";

    // --- CATEGORY ERRORS ---
    String ERR_CATEGORY_ACTIVE = "exception.category.active";
    String ERR_CATEGORY_DELETE_HAS_DEPENDENCIES = "exception.category.delete.hasdependencies";
    String ERR_CATEGORY_DUPLICATED = "exception.category.duplicated";
    String ERR_CATEGORY_DUPLICATED_ES = "exception.category.duplicatedEs";
    String ERR_CATEGORY_NOT_ACTIVE = "exception.category.notactive";
    String ERR_CATEGORY_NOT_FOUND = "exception.category.notfound";

    // --- FIELD ERRORS ---
    String ERR_FIELD_ACTIVE = "exception.field.active";
    String ERR_FIELD_DELETE_HAS_DEPENDENCIES = "exception.field.delete.hasdependencies";
    String ERR_FIELD_DUPLICATED = "exception.field.duplicated";
    String ERR_FIELD_DUPLICATED_ES = "exception.field.duplicatedEs";
    String ERR_FIELD_NOT_ACTIVE = "exception.field.notactive";
    String ERR_FIELD_NOT_FOUND = "exception.field.notfound";

    // --- ADMINISTRATIVE UNIT (ADMUNIT) ERRORS ---
    String ERR_ADMUNIT_DIR3_UNAVAILABLE = "exception.admunit.dir3.unavailable";

    // --- COMMISSION ERRORS ---
    String ERR_COMMISSION_ACTIVE = "exception.commission.active";
    String ERR_COMMISSION_DELETE_HAS_DEPENDENCIES = "exception.commission.delete.hasdependencies";
    String ERR_COMMISSION_DUPLICATED = "exception.commission.duplicated";
    String ERR_COMMISSION_DUPLICATED_ES = "exception.commission.duplicatedEs";
    String ERR_COMMISSION_NOT_FOUND = "exception.commission.notfound";
    String ERR_COMMISSION_NOT_ACTIVE = "exception.commission.notactive";

    // --- SYSTEM TYPE ERRORS ---
    String ERR_SYSTEM_TYPE_ACTIVE = "exception.systemtype.active";
    String ERR_SYSTEM_TYPE_DELETE_HAS_DEPENDENCIES = "exception.systemtype.delete.hasdependencies";
    String ERR_SYSTEM_TYPE_DUPLICATED = "exception.systemtype.duplicated";
    String ERR_SYSTEM_TYPE_DUPLICATED_ES = "exception.systemtype.duplicatedEs";
    String ERR_SYSTEM_TYPE_NOT_ACTIVE = "exception.systemtype.notactive";
    String ERR_SYSTEM_TYPE_NOT_FOUND = "exception.systemtype.notfound";

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
    String ERR_APP_INFORMATION_SYSTEM_DB_ALREADY_EXISTS = "exception.appinformationsystemdb.alreadyexists";

    // --- APPLICATION SECURITY ERRORS ---
    String ERR_APP_SECURITY_NOT_ACTIVE = "exception.appsecurity.notactive";
    String ERR_APP_SECURITY_NOT_FOUND = "exception.appsecurity.notfound";
    String ERR_APP_SECURITY_ALREADY_EXISTS = "exception.appsecurity.alreadyexists";

    // --- APPLICATION ACCESSIBILITY ERRORS ---
    String ERR_APP_ACCESSIBILITY_NOT_ACTIVE = "exception.appaccessibility.notactive";
    String ERR_APP_ACCESSIBILITY_NOT_FOUND = "exception.appaccessibility.notfound";
    String ERR_APP_ACCESSIBILITY_ALREADY_EXISTS = "exception.appaccessibility.alreadyexists";

    // --- APPLICATION SECURITY ROLE ERRORS ---
    String ERR_APP_ROLE_NOT_ACTIVE = "exception.approle.notactive";
    String ERR_APP_ROLE_NOT_FOUND = "exception.approle.notfound";

    // --- APPLICATION SECURITY WEB CONTEXT ERRORS ---
    String ERR_APP_WEB_CONTEXT_NOT_ACTIVE = "exception.appwebcontext.notactive";
    String ERR_APP_WEB_CONTEXT_NOT_FOUND = "exception.appwebcontext.notfound";

    // --- APPLICATION SECURITY ENS CLASSIFICATION ERRORS ---
    String ERR_APP_ENS_CLASSIFICATION_NOT_ACTIVE = "exception.appensclassification.notactive";
    String ERR_APP_ENS_CLASSIFICATION_NOT_FOUND = "exception.appensclassification.notfound";

    // --- APPLICATION SECURITY RISK ERRORS ---
    String ERR_APP_SECURITY_RISK_NOT_ACTIVE = "exception.appsecurityrisk.notactive";
    String ERR_APP_SECURITY_RISK_NOT_FOUND = "exception.appsecurityrisk.notfound";

    // --- APPLICATION SECURITY MEASURE ERRORS ---
    String ERR_APP_SECURITY_MEASURE_NOT_ACTIVE = "exception.appsecuritymeasure.notactive";
    String ERR_APP_SECURITY_MEASURE_NOT_FOUND = "exception.appsecuritymeasure.notfound";

    // --- APPLICATION DEVELOPMENT ERRORS ---
    String ERR_DEVELOPMENT_NOT_ACTIVE = "exception.development.notactive";
    String ERR_DEVELOPMENT_NOT_FOUND = "exception.development.notfound";
    String ERR_DEVELOPMENT_ALREADY_EXISTS = "exception.development.alreadyexists";

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

    // --- COMPANY CORE ERRORS ---
    String ERR_COMPANY_ACTIVE = "exception.company.active";
    String ERR_COMPANY_DUPLICATED = "exception.company.duplicated";
    String ERR_COMPANY_NOT_ACTIVE = "exception.company.notactive";
    String ERR_COMPANY_NOT_FOUND = "exception.company.notfound";
    String ERR_COMPANY_HAS_RESPONSIBLE = "exception.company.hasresponsible";
    String ERR_COMPANY_HAS_AUTHORIZED = "exception.company.hasauthorized";

    // --- PERSON CORE ERRORS ---
    String ERR_PERSON_ACTIVE = "exception.person.active";
    String ERR_PERSON_DUPLICATED = "exception.person.duplicated";
    String ERR_PERSON_OWNED_BY_OTHER = "exception.person.owned";
    String ERR_PERSON_NOT_ACTIVE = "exception.person.notactive";
    String ERR_PERSON_NOT_FOUND = "exception.person.notfound";
    String ERR_PERSON_COMPANY_REQUIRED_WHEN_NOT_CAIB = "exception.person.companyrequired";

    // --- AUTHORIZATION TYPE CORE ERRORS ---
    String ERR_AUTHORIZATIONTYPE_ACTIVE = "exception.authorizationtype.active";
    String ERR_AUTHORIZATIONTYPE_DUPLICATED = "exception.authorizationtype.duplicated";
    String ERR_AUTHORIZATIONTYPE_NOT_ACTIVE = "exception.authorizationtype.notactive";
    String ERR_AUTHORIZATIONTYPE_NOT_FOUND = "exception.authorizationtype.notfound";

    // --- APPLICATION RESPONSIBLE CORE ERRORS ---
    String ERR_APPRESPONSIBLE_ACTIVE = "exception.appresponsible.active";
    String ERR_APPRESPONSIBLE_DUPLICATED = "exception.appresponsible.duplicated";
    String ERR_APPRESPONSIBLE_NOT_ACTIVE = "exception.appresponsible.notactive";
    String ERR_APPRESPONSIBLE_NOT_FOUND = "exception.appresponsible.notfound";
    String ERR_APPRESPONSIBLE_PERSON_DATA_REQUIRED = "exception.appresponsible.persondatarequired";
    String ERR_APPRESPONSIBLE_REQUIRES_PERSONAL_CAIB = "exception.appresponsible.requirespersonalcaib";

    // --- APPLICATION AUTHORIZED CORE ERRORS ---
    String ERR_APPAUTHORIZED_ACTIVE = "exception.appauthorized.active";
    String ERR_APPAUTHORIZED_DUPLICATED = "exception.appauthorized.duplicated";
    String ERR_APPAUTHORIZED_NOT_ACTIVE = "exception.appauthorized.notactive";
    String ERR_APPAUTHORIZED_NOT_FOUND = "exception.appauthorized.notfound";
    String ERR_APPAUTHORIZED_PERSON_DATA_REQUIRED = "exception.appauthorized.persondatarequired";

    // --- ROLE TRANSFER ERRORS ---
    String ERR_ROLETRANSFER_TARGET_REQUIRED = "exception.roletransfer.targetrequired";
    String ERR_ROLETRANSFER_TARGET_NOT_FOUND = "exception.roletransfer.targetnotfound";

    // --- IDENTITY PROVIDER CORE ERRORS ---
    String ERR_IDENTITYPROVIDER_ACTIVE = "exception.identityprovider.active";
    String ERR_IDENTITYPROVIDER_DUPLICATED = "exception.identityprovider.duplicated";
    String ERR_IDENTITYPROVIDER_NOT_ACTIVE = "exception.identityprovider.notactive";
    String ERR_IDENTITYPROVIDER_NOT_FOUND = "exception.identityprovider.notfound";

    // --- PERSONAL DATA PROCESSING CORE ERRORS ---
    String ERR_PERSONALDATAPROCESSING_ACTIVE = "exception.personaldataprocessing.active";
    String ERR_PERSONALDATAPROCESSING_DUPLICATED = "exception.personaldataprocessing.duplicated";
    String ERR_PERSONALDATAPROCESSING_NOT_ACTIVE = "exception.personaldataprocessing.notactive";
    String ERR_PERSONALDATAPROCESSING_NOT_FOUND = "exception.personaldataprocessing.notfound";

    // --- WEB CONTEXT CORE ERRORS ---
    String ERR_WEBCONTEXT_ACTIVE = "exception.webcontext.active";
    String ERR_WEBCONTEXT_DUPLICATED = "exception.webcontext.duplicated";
    String ERR_WEBCONTEXT_NOT_ACTIVE = "exception.webcontext.notactive";
    String ERR_WEBCONTEXT_NOT_FOUND = "exception.webcontext.notfound";

    // --- SECURITY MEASURE TYPE CORE ERRORS ---
    String ERR_SECURITYMEASURETYPE_ACTIVE = "exception.securitymeasuretype.active";
    String ERR_SECURITYMEASURETYPE_DUPLICATED = "exception.securitymeasuretype.duplicated";
    String ERR_SECURITYMEASURETYPE_NOT_ACTIVE = "exception.securitymeasuretype.notactive";
    String ERR_SECURITYMEASURETYPE_NOT_FOUND = "exception.securitymeasuretype.notfound";

    // --- COMPLIANCE SITUATION CORE ERRORS ---
    String ERR_COMPLIANCESITUATION_ACTIVE = "exception.compliancesituation.active";
    String ERR_COMPLIANCESITUATION_DUPLICATED = "exception.compliancesituation.duplicated";
    String ERR_COMPLIANCESITUATION_NOT_ACTIVE = "exception.compliancesituation.notactive";
    String ERR_COMPLIANCESITUATION_NOT_FOUND = "exception.compliancesituation.notfound";

    // --- CLASSIFICATION SEGMENT CORE ERRORS ---
    String ERR_CLASSIFICATIONSEGMENT_ACTIVE = "exception.classificationsegment.active";
    String ERR_CLASSIFICATIONSEGMENT_DUPLICATED = "exception.classificationsegment.duplicated";
    String ERR_CLASSIFICATIONSEGMENT_NOT_ACTIVE = "exception.classificationsegment.notactive";
    String ERR_CLASSIFICATIONSEGMENT_NOT_FOUND = "exception.classificationsegment.notfound";

    // --- ENS REQUIREMENT CORE ERRORS ---
    String ERR_ENSREQUIREMENT_ACTIVE = "exception.ensrequirement.active";
    String ERR_ENSREQUIREMENT_DUPLICATED = "exception.ensrequirement.duplicated";
    String ERR_ENSREQUIREMENT_NOT_ACTIVE = "exception.ensrequirement.notactive";
    String ERR_ENSREQUIREMENT_NOT_FOUND = "exception.ensrequirement.notfound";

    // --- SOFFID INTEGRATION ERRORS ---
    String ERR_SOFFID_UNAVAILABLE = "exception.soffid.unavailable";

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