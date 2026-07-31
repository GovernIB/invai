import { PageParams } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export enum CommissionType {
  TECNICA = 'TECNICA',
  SUPERIOR = 'SUPERIOR',
}

export interface Commission {
  id: number;
  name: string | null;
  nameEs: string | null;
  expedientNumber: string | null;
  approvalDate: string | null;
  commissionType: CommissionType | null;
  deletedAt: string | null;
}

export interface CommissionInput {
  name: string;
  nameEs: string;
  expedientNumber: string;
  approvalDate: string;
  commissionType: CommissionType;
}

export interface CommissionFilters {
  name: string | null;
  nameEs: string | null;
  expedientNumber: string | null;
  approvalDateFrom: string | null;
  approvalDateTo: string | null;
  commissionType: CommissionType | null;
  status: SoftDeleteStatus | null;
}

export interface CommissionPageParams extends PageParams {
  quickSearch?: string;
  name?: string;
  nameEs?: string;
  expedientNumber?: string;
  approvalDateFrom?: string;
  approvalDateTo?: string;
  commissionType?: CommissionType;
  statusId?: SoftDeleteStatus;
}
