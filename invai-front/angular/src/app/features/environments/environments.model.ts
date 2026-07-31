import { PageParams } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export { SoftDeleteStatus as EnvironmentStatus };

export interface Environment {
  id: number;
  code: string | null;
  name: string | null;
  nameEs: string | null;
  deletedAt: string | null;
}

export interface EnvironmentInput {
  code: string;
  name: string;
  nameEs: string;
}

export interface EnvironmentCatalogOption {
  id: number;
  code: string;
  label: string;
}

export interface EnvironmentFilters {
  code: string | null;
  name: string | null;
  nameEs: string | null;
  status: SoftDeleteStatus | null;
}

export interface EnvironmentPageParams extends PageParams {
  code?: string;
  name?: string;
  nameEs?: string;
  statusId?: SoftDeleteStatus;
  search?: string;
}
