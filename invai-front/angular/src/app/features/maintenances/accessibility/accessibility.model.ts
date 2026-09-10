import { PageParams } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export const ACCESSIBILITY_RESOURCE_KEYS = [
  'classification-segment',
  'compliance-situation',
] as const;

export type AccessibilityResourceKey = (typeof ACCESSIBILITY_RESOURCE_KEYS)[number];

export interface AccessibilityResource {
  id: number;
  name: string | null;
  nameEs: string | null;
  deletedAt: string | null;
}

export interface AccessibilityResourceInput {
  name: string;
  nameEs: string;
}

export interface AccessibilityResourceFilters {
  name: string | null;
  nameEs: string | null;
  status: SoftDeleteStatus | null;
}

export interface AccessibilityResourcePageParams extends PageParams {
  name?: string;
  nameEs?: string;
  statusId?: SoftDeleteStatus;
  search?: string;
}
