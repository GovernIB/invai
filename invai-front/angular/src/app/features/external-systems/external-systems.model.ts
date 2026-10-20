import { ResponsibleCompany } from '@features/maintenances/responsibles/responsibles.model';
import { PageParams } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

// A system outside the application inventory that applications integrate with.
export interface ExternalSystem {
  id: number;
  name: string | null;
  company: ResponsibleCompany | null;
  deletedAt: string | null;
}

export interface ExternalSystemInput {
  name: string;
  companyId: number;
}

export interface ExternalSystemCatalogOption {
  id: number;
  label: string;
}

export interface ExternalSystemFilters {
  name: string | null;
  companyId: number | null;
  status: SoftDeleteStatus | null;
}

export interface ExternalSystemPageParams extends PageParams {
  name?: string;
  companyId?: number;
  statusId?: SoftDeleteStatus;
  search?: string;
}
