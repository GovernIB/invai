import { KeyLabel } from '@models/table.model';

export const ROLES_TABLE_COLUMNS: KeyLabel[] = [
  {
    key: 'name',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Nom`,
    sortBy: 'name',
    minWidth: '14rem',
  },
  {
    key: 'nameEs',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Nom en castellà`,
    sortBy: 'nameEs',
    minWidth: '14rem',
  },
  { key: 'status', label: $localize`Estat`, sortBy: 'deletedAt', minWidth: '8rem' },
];
