import { PageParams } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface SystemType {
  id: number;
  name: string | null;
  nameEs?: string | null;
  deletedAt: string | null;
}

export interface SystemTypeInput {
  name: string;
  nameEs: string;
}

export interface SystemTypeFilters {
  name: string | null;
  nameEs: string | null;
  status: SoftDeleteStatus | null;
}

export type SystemTypePageParams = PageParams;
