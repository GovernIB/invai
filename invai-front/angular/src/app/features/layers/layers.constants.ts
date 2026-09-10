import { KeyLabel } from '@models/table.model';

export const LAYERS_TABLE_COLUMNS: KeyLabel[] = [
  {
    key: 'name',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Nom`,
    sortBy: 'name',
    minWidth: '14rem',
  },
  { key: 'status', label: $localize`Estat`, sortBy: 'deletedAt', minWidth: '8rem' },
];
