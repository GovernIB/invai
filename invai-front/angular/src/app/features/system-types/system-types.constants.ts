import { KeyLabel } from '@models/table.model';

export const SYSTEM_TYPES_TABLE_COLUMNS: KeyLabel[] = [
  { key: 'name', label: $localize`Nom`, sortBy: 'name', minWidth: '14rem' },
  { key: 'nameEs', label: $localize`Nom en castellà`, sortBy: 'nameEs', minWidth: '14rem' },
  { key: 'status', label: $localize`Estat`, sortBy: 'deletedAt', minWidth: '8rem' },
];
