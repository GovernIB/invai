import { PageParams } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface Field {
  id: number;
  name: string | null;
  nameEs?: string | null;
  deletedAt: string | null;
}

export interface FieldInput {
  name: string;
  nameEs: string;
}

export interface FieldFilters {
  name: string | null;
  nameEs: string | null;
  status: SoftDeleteStatus | null;
}

export type FieldPageParams = PageParams;
