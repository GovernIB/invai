import { KeyLabel } from '@models/table.model';

import { fnSetColumnVisibility } from './table.utils';

describe('table utils', () => {
  const columns: KeyLabel[] = [
    { key: 'name', label: 'Nom' },
    { key: 'status', label: 'Estat' },
    { key: 'description', label: 'Descripció' },
  ];

  it('adds a column once and restores the canonical order', () => {
    expect(
      fnSetColumnVisibility(
        columns,
        [columns[2]!, columns[0]!, { key: 'status', label: 'Estat' }],
        'status',
        true,
      ),
    ).toEqual(columns);
  });

  it('removes only the requested column', () => {
    expect(fnSetColumnVisibility(columns, columns, 'status', false)).toEqual([
      columns[0],
      columns[2],
    ]);
  });
});
