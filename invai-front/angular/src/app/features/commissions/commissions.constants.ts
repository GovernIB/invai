import { KeyLabel } from '@models/table.model';

import { CommissionType } from './commissions.model';

export const COMMISSION_DATE_FORMAT = 'dd/mm/yy';
export const COMMISSION_DATE_PLACEHOLDER = 'dd/mm/aaaa';

export const COMMISSIONS_TABLE_COLUMNS: KeyLabel[] = [
  { key: 'name', label: $localize`Nom`, sortBy: 'name', minWidth: '14rem' },
  { key: 'nameEs', label: $localize`Nom en castellà`, sortBy: 'nameEs', minWidth: '14rem' },
  {
    key: 'expedientNumber',
    label: $localize`Número d'expedient`,
    sortBy: 'expedientNumber',
    minWidth: '12rem',
  },
  {
    key: 'approvalDate',
    label: $localize`Data d'aprovació`,
    sortBy: 'approvalDate',
    minWidth: '11rem',
  },
  {
    key: 'commissionType',
    label: $localize`Tipus de comissió`,
    sortBy: 'commissionType',
    minWidth: '11rem',
  },
  { key: 'status', label: $localize`Estat`, sortBy: 'deletedAt', minWidth: '8rem' },
];

export const COMMISSION_TYPE_LABELS: Record<CommissionType, string> = {
  [CommissionType.TECNICA]: $localize`Tècnica`,
  [CommissionType.SUPERIOR]: $localize`Superior`,
};

export const COMMISSION_TYPE_OPTIONS = Object.entries(COMMISSION_TYPE_LABELS).map(
  ([value, label]) => ({ value: value as CommissionType, label }),
);
