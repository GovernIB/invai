import { PageParams } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export const SECURITY_RESOURCE_KEYS = [
  'ens-requirement',
  'identity-provider',
  'personal-data-processing',
  'security-measure-type',
  'web-context',
] as const;

export type SecurityResourceKey = (typeof SECURITY_RESOURCE_KEYS)[number];

export interface SecurityResource {
  id: number;
  name: string | null;
  nameEs?: string | null;
  deletedAt: string | null;
}

export interface SecurityResourceInput {
  name: string;
  nameEs?: string;
}

export interface SecurityResourceFilters {
  name: string | null;
  nameEs: string | null;
  status: SoftDeleteStatus | null;
}

export interface SecurityResourcePageParams extends PageParams {
  name?: string;
  nameEs?: string;
  statusId?: SoftDeleteStatus;
  search?: string;
}
