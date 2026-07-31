import { PageParams } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface Category {
  id: number;
  name: string | null;
  nameEs?: string | null;
  deletedAt: string | null;
}

export interface CategoryInput {
  name: string;
  nameEs: string;
}

export interface CategoryFilters {
  name: string | null;
  nameEs: string | null;
  status: SoftDeleteStatus | null;
}

export interface CategoryPageParams extends PageParams {
  quickSearch?: string;
  name?: string;
  nameEs?: string;
}
