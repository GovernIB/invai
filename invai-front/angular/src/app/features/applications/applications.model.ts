import type { Dir3Validation } from './application-dir3.model';
import type { ValidationStatus } from './validation-status.model';
import { AdministrativeUnit } from '@features/administrative-units/administrative-units.model';
import { Category } from '@features/categories/categories.model';
import { Commission } from '@features/commissions/commissions.model';
import { ExternalSystem } from '@features/external-systems/external-systems.model';
import { Field } from '@features/fields/fields.model';
import { Layer } from '@features/layers/layers.model';
import { AccessibilityResource } from '@features/maintenances/accessibility/accessibility.model';
import {
  ResponsibleAuthorization,
  ResponsiblePerson,
  ResponsiblePersonOption,
  ResponsibleType,
} from '@features/maintenances/responsibles/responsibles.model';
import { Role } from '@features/roles/roles.model';
import { SystemType } from '@features/system-types/system-types.model';
import { DatabaseRecord, InfrastructureSystem } from '@features/systems/systems.model';
import { Technology } from '@features/technologies/technologies.model';
import { PageParams } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface Application {
  id: string;
  code: string;
  prefix: string;
  name: string;
  category: string;
  informationSystem: string;
  scope: string;
  commission: string;
  administrativeUnit: string;
  department: string;
  status: ApplicationStatus | null;
  description: string;
  creationDate: string;
  modificationDate: string;
  withdrawalDate: string;
  environment?: string;
  database?: string;
  server?: string;
  responsible?: string;
  categoryId?: number;
  informationSystemId?: number;
  scopeId?: number;
  commissionId?: number;
  admUnitCode?: string;
  departmentCode?: string;
  statusId?: ApplicationStatus;
  informationSystemDbId?: number | null;
  appDevelopmentId?: number | null;
  appSecurityId?: number | null;
  appAccessibilityId?: number | null;
  appDataId?: number | null;
  appIntegrationId?: number | null;
  appResponsibleAuthorizedId: number | null;
  /** At least one active Personal CAIB assignment has a DIR3 that mismatches the application. */
  dir3Mismatch: boolean;
  incomplete: boolean | null;
  missingResponsibleTypes: boolean | null;
  missingAuthorized: boolean | null;
  missingDevelopmentFields: boolean | null;
  missingSystems: boolean | null;
  missingDatabases: boolean | null;
  missingAccessibilityFields: boolean | null;
  missingSecurityData: boolean | null;
  missingIntegrationData: boolean | null;
}

export enum ApplicationStatus {
  ACTIVE = 1,
  INACTIVE = 2,
}

export enum ApplicationStatusCode {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

export interface ApplicationOutput {
  id: number;
  code: string | null;
  prefix: string | null;
  name: string | null;
  category: Category | null;
  systemType: SystemType | null;
  field: Field | null;
  admUnit: AdministrativeUnit | null;
  department: AdministrativeUnit | null;
  csCommission: Commission | null;
  description: string | null;
  status: ApplicationStatusCode | null;
  expirationDate: string | null;
  createdAt: string | null;
  createdBy: string | null;
  updatedAt: string | null;
  updatedBy: string | null;
  loadUser: string | null;
  loadDate: string | null;
  appInformationSystemDbId: number | null;
  appDevelopmentId: number | null;
  appSecurityId?: number | null;
  appAccessibilityId?: number | null;
  // Only returned by the detail, create, update and reactivate endpoints.
  appDataId?: number | null;
  appIntegrationId?: number | null;
  appResponsibleAuthorizedId: number | null;
  // Persisted by the DIR3 audits (`dir3/check-all`, `dir3/check/{id}`).
  dir3Mismatch?: boolean;
  incomplete: boolean | null;
  missingResponsibleTypes: boolean | null;
  missingAuthorized: boolean | null;
  missingDevelopmentFields: boolean | null;
  missingSystems: boolean | null;
  missingDatabases: boolean | null;
  missingAccessibilityFields: boolean | null;
  missingSecurityData: boolean | null;
  missingIntegrationData: boolean | null;
}

export interface ApplicationDir3MismatchOutput {
  id: number;
  name: string;
  dir3Mismatch: boolean;
}

// The DIR3 audit of every application runs in the background on the server that launched it.
export type Dir3AuditState = 'IDLE' | 'RUNNING' | 'COMPLETED' | 'FAILED';

export interface Dir3AuditStatus {
  state: Dir3AuditState;
  startedBy: string | null;
  startedAt: string | null;
  finishedAt: string | null;
  totalApplications: number;
  processedApplications: number;
  mismatchedApplications: ApplicationDir3MismatchOutput[];
  // Back-end message key (not a localized text) explaining why a FAILED audit stopped, if known.
  failureReason: string | null;
  // Applications whose audit failed and were left unchanged; a COMPLETED audit may still have some.
  failedApplicationIds: number[];
}

export interface ApplicationInput {
  name: string;
  prefix: string;
  code: string;
  categoryId: number;
  systemTypeId: number;
  fieldId: number;
  admUnitCode: string;
  commissionId: number;
  description: string;
  statusId: ApplicationStatus;
}

export interface ApplicationPageParams extends PageParams {
  incomplete?: boolean;
  prefix?: string;
  applicationName?: string;
  categoryId?: number;
  systemTypeId?: number;
  fieldId?: number;
  commissionId?: number;
  admUnitCode?: string;
  statusId?: ApplicationStatus;
  description?: string;
  quickSearch?: string;
  responsibleId?: number;
  databaseId?: number;
  serverId?: number;
  environmentId?: number;
}

export interface ApplicationFilters {
  prefix: string | null;
  application: string | null;
  category: number | null;
  informationSystem: number | null;
  scope: number | null;
  administrativeUnit: string | null;
  status: ApplicationStatus | null;
  responsible: ResponsiblePersonOption | null;
  database: number | null;
  server: number | null;
  environment: number | null;
  incomplete: boolean;
}

export interface SelectOption<TValue = string> {
  label: string;
  value: TValue;
}

export interface ApplicationServer {
  id: number;
  informationSystemDbId: number;
  systemId: number;
  deletedAt: string | null;
  environment: string;
  server: string;
  instance: string;
  port: number;
  version: string;
  status: string;
  observations: string;
  catalogItem: ApplicationSystemCatalogRow;
}

export interface ApplicationDatabase {
  id: number;
  informationSystemDbId: number;
  databaseId: number;
  deletedAt: string | null;
  environment: string;
  server: string;
  database: string;
  service: string;
  port: number;
  type: string;
  status: string;
  observations: string;
  catalogItem: ApplicationDatabaseCatalogRow;
}

export type ApplicationInfrastructureResource = ApplicationServer | ApplicationDatabase;

export enum ApplicationInfrastructureStatus {
  ACTIVE = 1,
  INACTIVE = 2,
}

export interface ApplicationInfrastructureBasePageParams extends PageParams {
  informationSystemDbId: number;
  statusId?: ApplicationInfrastructureStatus;
}

export interface ApplicationSystemsPageParams extends ApplicationInfrastructureBasePageParams {
  systemId?: number;
  environmentId?: number;
}

export interface ApplicationDatabasesPageParams extends ApplicationInfrastructureBasePageParams {
  databaseId?: number;
  environmentId?: number;
}

export interface ApplicationServerFilters {
  environment: number | null;
  server: number | null;
  instance: string | null;
  port: number | null;
  version: string | null;
  status: ApplicationInfrastructureStatus | null;
  observations: string | null;
}

export interface ApplicationDatabaseFilters {
  environment: number | null;
  server: string | null;
  version: string | null;
  database: number | null;
  service: string | null;
  port: number | null;
  type: string | null;
  status: ApplicationInfrastructureStatus | null;
  observations: string | null;
}

export interface ApplicationInfrastructureFilterOptions {
  servers: SelectOption<number>[];
  databases: SelectOption<number>[];
  environments: SelectOption<number>[];
}

export interface ApplicationInfrastructureEnvironmentOutput {
  id: number;
  code: string | null;
  name: string | null;
  nameEs: string | null;
}

export interface ApplicationSystemCatalogRow {
  id: number;
  server: string;
  environment: string;
  instance: string;
  port: number;
  version: string;
  description: string;
  source: InfrastructureSystem;
}

export interface ApplicationDatabaseCatalogRow {
  id: number;
  server: string;
  environment: string;
  service: string;
  port: number;
  databaseType: string;
  description: string;
  source: DatabaseRecord;
}

export type ApplicationInfrastructureCatalogRow =
  ApplicationSystemCatalogRow | ApplicationDatabaseCatalogRow;

export interface ApplicationSystemDatabaseOutput {
  id: number;
  application: ApplicationOutput;
  observation: string | null;
  deletedAt: string | null;
}

export interface ApplicationSystemDatabaseInput {
  applicationId: number;
  observation: string;
}

export interface ApplicationSystemRelationOutput {
  id: number;
  informationSystemDb: ApplicationSystemDatabaseOutput;
  system: InfrastructureSystem;
  deletedAt: string | null;
}

export interface ApplicationDatabaseRelationOutput {
  id: number;
  informationSystemDb: ApplicationSystemDatabaseOutput;
  database: DatabaseRecord;
  deletedAt: string | null;
}

export interface ApplicationSystemRelationInput {
  informationSystemDbId: number;
  systemId: number;
}

export interface ApplicationDatabaseRelationInput {
  informationSystemDbId: number;
  databaseId: number;
}

export enum DevelopmentModality {
  INTERNAL = 1,
  EXTERNAL = 2,
  MIXED = 3,
}

export enum DevelopmentStandardAdaption {
  CONFORMING = 1,
  PARTIALLY_CONFORMING = 2,
  NON_CONFORMING = 3,
}

export interface DevelopmentLookupOutput<TId extends number> {
  id: TId;
  name: string | null;
  nameEs: string | null;
}

export interface ApplicationDevelopmentOutput {
  id: number;
  application: ApplicationOutput;
  environment: ApplicationInfrastructureEnvironmentOutput;
  modality: DevelopmentLookupOutput<DevelopmentModality>;
  code: string;
  standardAdaption: DevelopmentLookupOutput<DevelopmentStandardAdaption>;
  revisionDate: string;
  observation: string | null;
  deletedAt: string | null;
}

export interface ApplicationDevelopmentInput {
  applicationId: number;
  environmentId: number;
  modalityId: DevelopmentModality;
  code: string;
  standardAdaptionId: DevelopmentStandardAdaption;
  revisionDate: string;
  observation: string;
}

export interface ApplicationProviderOutput {
  id: number;
  companyName: string;
  role: Role | null;
  startDate: string | null;
  expireDate: string | null;
  deletedAt: string | null;
}

export interface ApplicationProviderInput {
  appDevelopmentId: number;
  companyName: string;
  roleId: number | null;
  startDate: string | null;
  expireDate: string | null;
}

export interface ApplicationTechnologyOutput {
  id: number;
  layer: Layer;
  technology: Technology;
  version: string;
  architecture: string;
  deletedAt: string | null;
}

export interface ApplicationTechnologyInput {
  appDevelopmentId: number;
  layerId: number;
  technologyId: number;
  version: string;
  architecture: string;
}

export interface ApplicationDevelopmentResourcePageParams extends PageParams {
  appDevelopmentId: number;
}

interface ApplicationResponsibleOutputBase {
  dir3Validation?: Dir3Validation | null;
  appResponsibleAuthorizedId: number;
  responsibleType: ResponsibleType;
  jobTitle: string | null;
  observation: string | null;
  deletedAt: string | null;
}

export interface ApplicationAssignedResponsibleOutput extends ApplicationResponsibleOutputBase {
  id: number;
  person: ResponsiblePerson;
}

export interface ApplicationResponsiblePlaceholderOutput extends ApplicationResponsibleOutputBase {
  id: null;
  person: null;
}

export type ApplicationResponsibleOutput =
  ApplicationAssignedResponsibleOutput | ApplicationResponsiblePlaceholderOutput;

export type ApplicationPersonReferenceInput =
  | {
      personId: number;
      personalCaib: boolean;
    }
  | {
      personId: null;
      personFirstName: string;
      personLastName: string;
      personEmail: string;
      // Soffid user code stored on the new person; null when Soffid did not provide one.
      personUserName: string | null;
      companyId: null;
      personalCaib: true;
    };

export type ApplicationResponsibleInput = ApplicationPersonReferenceInput & {
  dir3Status?: boolean | null;
  appResponsibleAuthorizedId: number;
  responsibleTypeId: number;
  jobTitle: string | null;
  observation: string | null;
};

export interface ApplicationAuthorizedOutput {
  dir3Validation?: Dir3Validation | null;
  id: number;
  appResponsibleAuthorizedId: number;
  person: ResponsiblePerson;
  authorizationTypes: ResponsibleAuthorization[];
  observation: string | null;
  deletedAt: string | null;
}

export type ApplicationAuthorizedInput = ApplicationPersonReferenceInput & {
  dir3Status?: boolean | null;
  appResponsibleAuthorizedId: number;
  authorizationTypeIds: number[];
  observation: string | null;
};

export interface ApplicationAssignmentDeactivateInput {
  observation: string | null;
}

export interface ApplicationResponsiblePageParams extends PageParams {
  appResponsibleAuthorizedId: number;
  statusId?: SoftDeleteStatus;
  personId?: number;
  responsibleTypeId?: number;
  search?: string;
}

export interface ApplicationAuthorizedPageParams extends PageParams {
  appResponsibleAuthorizedId: number;
  statusId?: SoftDeleteStatus;
  personId?: number;
  search?: string;
}

export interface SecurityCatalogItem {
  id: number;
  name: string | null;
  nameEs?: string | null;
  deletedAt?: string | null;
}

export interface ApplicationSecurityOutput {
  id: number;
  application: ApplicationOutput;
  observation: string | null;
  deletedAt: string | null;
}

export interface ApplicationSecurityInput {
  applicationId: number;
  observation: string | null;
}

export interface ApplicationSecurityRole {
  id: number | null;
  roleId: number | null;
  name: string | null;
  system: string | null;
  description: string | null;
}

export interface ApplicationSecurityRoleOutput {
  id: number;
  appSecurity: ApplicationSecurityOutput;
  securityRole: ApplicationSecurityRole;
  deletedAt: string | null;
  // VALIDATED when the role name starts with `<prefix>_` (case-sensitive), NOT_VALIDATED when it
  // does not, MANUAL when someone vouched for it; a role added by hand is saved MANUAL directly.
  validationStatus: ValidationStatus;
  // Set only when validationStatus is MANUAL.
  manualValidatedAt: string | null;
  manualValidatedBy: string | null;
}

// securityRoleId is Soffid's own role id (ApplicationSecurityRole.roleId), not the local catalog id.
export interface ApplicationSecurityRoleInput {
  appSecurityId: number;
  securityRoleId: number;
}

export interface ApplicationWebContextOutput {
  url: string | null;
  id: number;
  appSecurity: ApplicationSecurityOutput;
  appDevelopment: ApplicationDevelopmentOutput | null;
  webContext: SecurityCatalogItem;
  field: Field;
  observation: string | null;
  validated: boolean;
  validatedAt: string | null;
  validatedBy: string | null;
  validatedReason: string | null;
  deletedAt: string | null;
}

export interface ApplicationWebContextInput {
  url: string | null;
  appDevelopmentId: number;
  webContextId: number;
  fieldId: number;
  observation: string | null;
}

export interface ApplicationDevelopmentWebContextPageParams extends ApplicationDevelopmentResourcePageParams {
  statusId?: SoftDeleteStatus;
}

export interface ApplicationEnsClassificationOutput {
  id: number;
  appSecurity: ApplicationSecurityOutput;
  identityProvider: SecurityCatalogItem | null;
  ensSubject: SecurityCatalogItem | null;
  personalDataProcessing: SecurityCatalogItem | null;
  approvalDate: string | null;
  confidentiality: SecurityCatalogItem | null;
  integrity: SecurityCatalogItem | null;
  traceability: SecurityCatalogItem | null;
  availability: SecurityCatalogItem | null;
  authenticity: SecurityCatalogItem | null;
  overallGrade: SecurityCatalogItem | null;
  deletedAt: string | null;
}

export interface ApplicationEnsClassificationInput {
  appSecurityId: number;
  identityProviderId: number | null;
  ensSubjectId: number | null;
  personalDataProcessingId: number | null;
  approvalDate: string | null;
  confidentialityId: number | null;
  integrityId: number | null;
  traceabilityId: number | null;
  availabilityId: number | null;
  authenticityId: number | null;
  overallGradeId: number | null;
}

export interface ApplicationSecurityRiskOutput {
  id: number;
  appSecurity: ApplicationSecurityOutput;
  level: SecurityCatalogItem | null;
  description: string | null;
  field: Field | null;
  deletedAt: string | null;
}

export interface ApplicationSecurityRiskInput {
  appSecurityId: number;
  levelId: number | null;
  description: string | null;
  fieldId: number | null;
}

export interface ApplicationSecurityMeasureOutput {
  id: number;
  appSecurity: ApplicationSecurityOutput;
  type: SecurityCatalogItem | null;
  ensRequirement: SecurityCatalogItem | null;
  description: string | null;
  deletedAt: string | null;
}

export interface ApplicationSecurityMeasureInput {
  appSecurityId: number;
  typeId: number | null;
  ensRequirementId: number | null;
  description: string | null;
}

export interface ApplicationSecurityRolePageParams extends ApplicationSecurityPageParams {
  search?: string;
  securityRoleName?: string;
  system?: string;
}

export interface ApplicationSecurityPageParams extends PageParams {
  appSecurityId: number;
  statusId?: SoftDeleteStatus;
}

export type ApplicationSecurityResourceOutput =
  | ApplicationSecurityRoleOutput
  | ApplicationWebContextOutput
  | ApplicationSecurityRiskOutput
  | ApplicationSecurityMeasureOutput;

export interface ApplicationAccessibilityInput {
  applicationId: number;
  complianceId: number | null;
  classificationSegmentId: number | null;
  publicUrl: string | null;
  mobileApplication: boolean | null;
  mobileApplicationName: string | null;
  nonAccessibleContent: string | null;
  observations: string | null;
  expireDate: string | null;
}

export interface ApplicationAccessibilityOutput {
  id: number;
  application: Pick<ApplicationOutput, 'id'> | null;
  compliance: AccessibilityResource | null;
  classificationSegment: AccessibilityResource | null;
  publicUrl: string | null;
  mobileApplication: boolean | null;
  mobileApplicationName: string | null;
  nonAccessibleContent: string | null;
  observations: string | null;
  expireDate: string | null;
  deletedAt: string | null;
}
export type EnsClassificationLoadState = 'ready' | 'unknown' | 'inconsistent';

// The "Dades" tab has two sources, each with the URL of its swagger.
export type ApplicationDataSource = 'openData' | 'reuse';

// PUT application/data/{id} only saves the observation; each URL is set on its own.
export interface ApplicationDataInput {
  applicationId: number;
  observation: string | null;
}

// Body of PUT application/data/{id}/open-data and /reuse. A null URL goes back to the default one,
// derived by the back-end from the application's code.
export interface ApplicationDataUrlInput {
  url: string | null;
}

// Answer of GET application/data/check-url and of the PUT of a source: the URL the back-end queried
// and whether it answered with a 2xx status code.
export interface ApplicationDataUrlOutput {
  url: string | null;
  validUrl: boolean;
}

// GET gives the URL each source uses (typed in by hand, or else the default one, derived from the
// application's code) without querying it; POST and PUT return both URLs as null.
export interface ApplicationDataOutput {
  id: number;
  application: Pick<ApplicationOutput, 'id'> | null;
  observation: string | null;
  openDataUrl: string | null;
  reuseUrl: string | null;
  deletedAt: string | null;
}

// Whether the URL of a source answers: `checking` while check-url is pending, `failed` when it
// could not be checked at all.
export type ApplicationDataUrlCheck = 'checking' | 'valid' | 'invalid' | 'failed';

// URL a source uses, as the Dades tab shows it, and the outcome of checking it.
export interface ApplicationDataUrlState {
  url: string | null;
  check: ApplicationDataUrlCheck;
}

// Anchor of the "Integracions" tab: one per application, holding its observations.
export interface ApplicationIntegrationOutput {
  id: number;
  applicationId: number | null;
  observation: string | null;
  deletedAt: string | null;
}

export interface ApplicationIntegrationInput {
  applicationId: number;
  observation: string | null;
}

// Role as returned by Soffid; `id` is Soffid's own role identifier.
export interface SoffidRole {
  id: number;
  name: string | null;
  description: string | null;
  system: string | null;
}

// requiredRoles, grantedRoles and rolesMismatch are resolved live by GET and returned as null by
// POST/PUT. When GET cannot query Soffid it still returns the row, with rolesUnavailable true and
// those three fields null; rolesUnavailable itself is null in POST/PUT responses.
export interface ApplicationIntegrationConnectionOutput {
  id: number;
  appIntegrationId: number;
  application: Pick<ApplicationOutput, 'id' | 'code' | 'name'> | null;
  externalSystem: ExternalSystem | null;
  technology: Technology | null;
  username: string | null;
  requiredRoles: SoffidRole[] | null;
  grantedRoles: SoffidRole[] | null;
  rolesMismatch: boolean | null;
  rolesUnavailable: boolean | null;
  deletedAt: string | null;
}

// Exactly one of applicationId and externalSystemId identifies the other system.
export interface ApplicationIntegrationConnectionInput {
  appIntegrationId: number;
  applicationId: number | null;
  externalSystemId: number | null;
  technologyId: number;
  username: string;
  requiredRoleIds: number[];
}

export interface ApplicationIntegrationConnectionPageParams extends PageParams {
  appIntegrationId: number;
  statusId?: SoftDeleteStatus;
}
