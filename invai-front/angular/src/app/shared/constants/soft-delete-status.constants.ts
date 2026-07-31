import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export const SOFT_DELETE_STATUS_LABELS: Record<SoftDeleteStatus, string> = {
  [SoftDeleteStatus.ACTIVE]: $localize`Actiu`,
  [SoftDeleteStatus.INACTIVE]: $localize`Inactiu`,
};

export const SOFT_DELETE_STATUS_OPTIONS = [
  {
    label: SOFT_DELETE_STATUS_LABELS[SoftDeleteStatus.ACTIVE],
    value: SoftDeleteStatus.ACTIVE,
  },
  {
    label: SOFT_DELETE_STATUS_LABELS[SoftDeleteStatus.INACTIVE],
    value: SoftDeleteStatus.INACTIVE,
  },
];

export const SOFT_DELETE_STATUS_UNSUPPORTED_SUMMARY = $localize`Informació`;
export const SOFT_DELETE_STATUS_UNSUPPORTED_DETAIL =
  $localize`La consulta de registres inactius encara no està disponible.`;
export const SOFT_DELETE_RESTORE_PENDING_SUMMARY = $localize`Funcionalitat pendent`;
export const SOFT_DELETE_RESTORE_PENDING_DETAIL =
  $localize`La restauració de registres encara no està implementada.`;

export function softDeleteStatusLabel(deletedAt: string | null): string {
  return SOFT_DELETE_STATUS_LABELS[
    deletedAt ? SoftDeleteStatus.INACTIVE : SoftDeleteStatus.ACTIVE
  ];
}
