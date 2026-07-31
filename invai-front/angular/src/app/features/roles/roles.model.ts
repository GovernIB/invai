import { PageParams } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface Role {
  id: number;
  name: string | null;
  nameEs: string | null;
  deletedAt: string | null;
}

export interface RoleInput {
  name: string;
  nameEs: string | null;
}

export interface RoleCatalogOption {
  id: number;
  label: string;
}

export interface RoleFilters {
  name: string | null;
  nameEs: string | null;
  status: SoftDeleteStatus | null;
}

export interface RolePageParams extends PageParams {
  name?: string;
  nameEs?: string;
  statusId?: SoftDeleteStatus;
  search?: string;
}
