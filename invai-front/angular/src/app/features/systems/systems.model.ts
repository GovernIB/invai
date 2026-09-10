import { Environment } from '@features/environments/environments.model';
import { PageParams } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export { SoftDeleteStatus as InfrastructureStatus };

export type ServerTypeCode = 'APPLICATION' | 'DATABASE';

export interface ServerTypeOutput {
  id: number;
  code: string;
  name: string | null;
  nameEs: string | null;
}

export interface InfrastructureServer {
  id: number;
  name: string;
  environment: Environment;
  serverType: ServerTypeOutput;
  deletedAt: string | null;
}

export interface InfrastructureServerInput {
  name: string;
  environmentId: number;
  serverTypeId: number;
}

export interface InfrastructureSystem {
  id: number;
  server: InfrastructureServer;
  instance: string;
  port: number;
  version: string;
  description: string | null;
  deletedAt: string | null;
}

export interface InfrastructureSystemInput {
  name: string;
  serverId: number;
  instance: string;
  port: number;
  version: string;
  description: string | null;
}

export interface DatabaseVendor {
  id: number;
  name: string;
  defaultPort: number;
  deletedAt: string | null;
}

export interface DatabaseVendorInput {
  name: string;
  defaultPort: number;
}

export interface DatabaseRecord {
  id: number;
  server: InfrastructureServer;
  service: string;
  port: number;
  databaseType: DatabaseVendor;
  description: string | null;
  deletedAt: string | null;
}

export interface DatabaseInput {
  serverId: number;
  service: string;
  port: number;
  databaseTypeId: number;
  description: string | null;
}

export interface ServerFilters {
  name: string | null;
  environmentId: number | null;
  status: SoftDeleteStatus | null;
}

export interface SystemFilters {
  serverId: number | null;
  instance: string | null;
  version: string | null;
  status: SoftDeleteStatus | null;
}

export interface DatabaseFilters {
  serverId: number | null;
  service: string | null;
  databaseTypeId: number | null;
  status: SoftDeleteStatus | null;
}

export interface DatabaseVendorFilters {
  name: string | null;
  defaultPort: number | null;
  status: SoftDeleteStatus | null;
}

export interface InfrastructurePageParams extends PageParams {
  statusId?: SoftDeleteStatus;
}

export interface ServerPageParams extends InfrastructurePageParams {
  name?: string;
  environmentId?: number;
  serverTypeCode: ServerTypeCode;
  search?: string;
}

export interface SystemPageParams extends InfrastructurePageParams {
  search?: string;
  serverId?: number;
  instance?: string;
  version?: string;
  unassignedToInformationSystemDbId?: number;
}

export interface DatabasePageParams extends InfrastructurePageParams {
  serverId?: number;
  service?: string;
  databaseTypeId?: number;
  search?: string;
  unassignedToInformationSystemDbId?: number;
}

export interface DatabaseVendorPageParams extends InfrastructurePageParams {
  name?: string;
  defaultPort?: number;
  search?: string;
}

export type InfrastructureDialogMode = 'create' | 'view' | 'edit';

export interface InfrastructureTableRow {
  [key: string]: string | number | null;
  id: number;
  deletedAt: string | null;
}

export interface PhysicalServerTableRow extends InfrastructureTableRow {
  name: string;
  environment: string;
  status: string;
}

export interface ServerTableRow extends InfrastructureTableRow {
  server: string;
  environment: string;
  instance: string;
  port: number;
  version: string;
  status: string;
  description: string;
}

export interface DatabaseTableRow extends InfrastructureTableRow {
  server: string;
  environment: string;
  service: string;
  port: number;
  databaseType: string;
  status: string;
  description: string;
}

export interface DatabaseVendorTableRow extends InfrastructureTableRow {
  name: string;
  defaultPort: number;
  status: string;
}

export interface ServerCatalogOption {
  id: number;
  name: string;
  environment: Environment;
  label: string;
}

export interface DatabaseVendorCatalogOption {
  id: number;
  name: string;
  defaultPort: number;
}
