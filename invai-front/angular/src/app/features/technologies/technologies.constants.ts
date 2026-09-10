import { KeyLabel } from '@models/table.model';

export const TECHNOLOGIES_TABLE_COLUMNS: KeyLabel[] = [
  {
    key: 'name',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Nom`,
    sortBy: 'name',
    minWidth: '14rem',
  },
  { key: 'layer', label: $localize`Capa`, sortBy: 'layer.name', minWidth: '12rem' },
  { key: 'status', label: $localize`Estat`, sortBy: 'deletedAt', minWidth: '8rem' },
];
