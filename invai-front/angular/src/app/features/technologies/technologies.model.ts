import { Layer } from '@features/layers/layers.model';
import { PageParams } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface Technology {
  id: number;
  name: string | null;
  layer: Layer;
  deletedAt: string | null;
}

export interface TechnologyInput {
  name: string;
  layerId: number;
}

export interface TechnologyCatalogOption {
  id: number;
  label: string;
  layerId: number;
  layerLabel: string;
}

export interface TechnologyFilters {
  name: string | null;
  layerId: number | null;
  status: SoftDeleteStatus | null;
}

export interface TechnologyPageParams extends PageParams {
  name?: string;
  layerId?: number;
  statusId?: SoftDeleteStatus;
  search?: string;
}
