import { PageParams } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface Layer {
  id: number;
  name: string | null;
  deletedAt: string | null;
}

export interface LayerInput {
  name: string;
}

export interface LayerOption {
  id: number;
  label: string;
}

export interface LayerFilters {
  name: string | null;
  status: SoftDeleteStatus | null;
}

export interface LayerPageParams extends PageParams {
  name?: string;
  statusId?: SoftDeleteStatus;
  search?: string;
}
