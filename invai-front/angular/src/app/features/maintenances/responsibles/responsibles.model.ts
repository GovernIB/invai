import { PageParams } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface ResponsibleMaintenanceEntity {
  id: number;
  deletedAt: string | null;
}

export interface ResponsibleCompany extends ResponsibleMaintenanceEntity {
  name: string;
}

export interface ResponsiblePerson extends ResponsibleMaintenanceEntity {
  company: ResponsibleCompany | null;
  firstName: string;
  lastName: string;
  email: string;
  personalCaib: boolean;
}

export interface ResponsibleAuthorization extends ResponsibleMaintenanceEntity {
  name: string;
  nameEs: string;
}

export interface ResponsibleNameInput {
  name: string;
}

export interface ResponsiblePersonInput {
  companyId: number | null;
  firstName: string;
  lastName: string;
  email: string;
  personalCaib: boolean;
}

export interface ResponsibleAuthorizationInput {
  name: string;
  nameEs: string;
}

export interface ResponsibleNameFilters {
  name: string | null;
  status: SoftDeleteStatus | null;
}

export interface ResponsiblePersonFilters {
  companyId: number | null;
  firstName: string | null;
  lastName: string | null;
  email: string | null;
  status: SoftDeleteStatus | null;
}

export interface ResponsibleAuthorizationFilters extends ResponsibleNameFilters {
  nameEs: string | null;
}

export interface ResponsibleNamePageParams extends PageParams {
  name?: string;
  statusId?: SoftDeleteStatus;
  search?: string;
}

export interface ResponsiblePersonPageParams extends PageParams {
  companyId?: number;
  excludeId?: number;
  firstName?: string;
  lastName?: string;
  email?: string;
  statusId?: SoftDeleteStatus;
  search?: string;
}

export interface ResponsibleAuthorizationPageParams extends ResponsibleNamePageParams {
  nameEs?: string;
}

export interface ResponsibleCompanyOption {
  id: number;
  label: string;
}

export interface ResponsiblePersonOption {
  id: number;
  label: string;
}

export interface ResponsibleLookup<TId extends number = number> {
  id: TId;
  name: string | null;
  nameEs: string | null;
}

export interface ResponsibleType extends ResponsibleLookup {
  requiresPersonalCaib: boolean;
}

export type ResponsibleMaintenanceItem =
  ResponsibleCompany | ResponsiblePerson | ResponsibleAuthorization;

export enum RoleAssignmentType {
  RESPONSIBLE = 'RESPONSIBLE',
  AUTHORIZED = 'AUTHORIZED',
}

export interface RoleAssignmentOutput {
  id: number;
  type: RoleAssignmentType;
  applicationId: number;
  applicationName: string;
  responsibleType: ResponsibleType | null;
  authorizationTypes: ResponsibleAuthorization[] | null;
}

export interface RoleTransferItemInput {
  id: number;
  type: RoleAssignmentType;
}

export interface RoleTransferInput {
  items: RoleTransferItemInput[];
  toPersonId: number | null;
  revoke: boolean;
}

export interface RoleTransferSourceRequest {
  requestId: number;
  person: ResponsiblePerson;
}
