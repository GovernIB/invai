import { AdministrativeUnit } from '@features/administrative-units/administrative-units.model';
import { Category } from '@features/categories/categories.model';
import { Commission } from '@features/commissions/commissions.model';
import { Field } from '@features/fields/fields.model';
import { SystemType } from '@features/system-types/system-types.model';
import { PageParams } from '@models/page.model';

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
  status: string;
  description: string;
  creationDate: string;
  modificationDate: string;
  withdrawalDate: string;
  categoryId?: number;
  informationSystemId?: number;
  scopeId?: number;
  commissionId?: number;
  administrativeUnitId?: number;
  statusId?: number;
}

export interface ApplicationStatus {
  id: number;
  name: string | null;
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
  csCommission: Commission | null;
  description: string | null;
  status: ApplicationStatus | null;
  expirationDate: string | null;
  createdAt: string | null;
  createdBy: string | null;
  updatedAt: string | null;
  updatedBy: string | null;
  loadUser: string | null;
  loadDate: string | null;
}

export interface ApplicationInput {
  name: string;
  prefix: string;
  code: string;
  categoryId: number;
  systemTypeId: number;
  fieldId: number;
  admUnitId: number;
  commissionId: number;
  description?: string | null;
  statusId: number;
}

export interface ApplicationPageParams extends PageParams {
  prefix?: string;
  applicationName?: string;
  categoryId?: number;
  systemTypeId?: number;
  fieldId?: number;
  commissionId?: number;
  admUnitId?: number;
  statusId?: number;
  description?: string;
  quickSearch?: string;
}

export interface ApplicationFilters {
  prefix: string | null;
  application: string | null;
  category: number | null;
  informationSystem: number | null;
  scope: number | null;
  commission: number | null;
  administrativeUnit: number | null;
  status: number | null;
  description: string | null;
  incomplete: boolean;
}

export interface SelectOption<TValue = string> {
  label: string;
  value: TValue;
}

export interface ApplicationServer {
  id: string;
  environment: string;
  server: string;
  instance: string;
  port: string;
  version: string;
  status: string;
  observations: string;
}

export interface ApplicationDatabase {
  id: string;
  environment: string;
  server: string;
  version: string;
  database: string;
  service: string;
  port: string;
  type: string;
  observations: string;
}

export type ApplicationInfrastructureResource = ApplicationServer | ApplicationDatabase;
