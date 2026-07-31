import { FormBuilder } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { softDeleteStatusLabel } from '@shared/constants/soft-delete-status.constants';

import { CATEGORIES_TABLE_COLUMNS } from '@features/categories/categories.constants';
import { createCategoryFiltersForm } from '@features/categories/forms/category-filters-form.factory';
import { COMMISSIONS_TABLE_COLUMNS } from '@features/commissions/commissions.constants';
import { createCommissionFiltersForm } from '@features/commissions/forms/commission-filters-form.factory';
import { ENVIRONMENTS_TABLE_COLUMNS } from '@features/environments/environments.constants';
import { createEnvironmentFiltersForm } from '@features/environments/forms/environment-filters-form.factory';
import { FIELDS_TABLE_COLUMNS } from '@features/fields/fields.constants';
import { createFieldFiltersForm } from '@features/fields/forms/field-filters-form.factory';
import { SYSTEM_TYPES_TABLE_COLUMNS } from '@features/system-types/system-types.constants';
import { createSystemTypeFiltersForm } from '@features/system-types/forms/system-type-filters-form.factory';
import { DATABASES_TABLE_COLUMNS, SERVERS_TABLE_COLUMNS } from '@features/systems/systems.constants';
import { createDatabaseFiltersForm } from '@features/systems/forms/database-filters-form.factory';
import { createServerFiltersForm } from '@features/systems/forms/server-filters-form.factory';
import { LAYERS_TABLE_COLUMNS } from '@features/layers/layers.constants';
import { createLayerFiltersForm } from '@features/layers/forms/layer-filters-form.factory';
import { ROLES_TABLE_COLUMNS } from '@features/roles/roles.constants';
import { createRoleFiltersForm } from '@features/roles/forms/role-filters-form.factory';
import { TECHNOLOGIES_TABLE_COLUMNS } from '@features/technologies/technologies.constants';
import { createTechnologyFiltersForm } from '@features/technologies/forms/technology-filters-form.factory';

describe('maintenance status filters', () => {
  it('initializes and resets every maintenance filter to active', () => {
    const formBuilder = new FormBuilder();
    const forms = [
      createCategoryFiltersForm(formBuilder),
      createSystemTypeFiltersForm(formBuilder),
      createEnvironmentFiltersForm(formBuilder),
      createFieldFiltersForm(formBuilder),
      createCommissionFiltersForm(formBuilder),
      createServerFiltersForm(formBuilder),
      createDatabaseFiltersForm(formBuilder),
      createRoleFiltersForm(formBuilder),
      createLayerFiltersForm(formBuilder),
      createTechnologyFiltersForm(formBuilder),
    ];

    for (const form of forms) {
      expect(form.controls.status.value).toBe(SoftDeleteStatus.ACTIVE);
      form.controls.status.setValue(SoftDeleteStatus.INACTIVE);
      form.controls.status.setValue(null);
      expect(form.controls.status.value).toBeNull();
      form.reset();
      expect(form.controls.status.value).toBe(SoftDeleteStatus.ACTIVE);
    }
  });

  it('exposes the status column in every maintenance table', () => {
    for (const columns of [
      CATEGORIES_TABLE_COLUMNS,
      SYSTEM_TYPES_TABLE_COLUMNS,
      ENVIRONMENTS_TABLE_COLUMNS,
      FIELDS_TABLE_COLUMNS,
      COMMISSIONS_TABLE_COLUMNS,
      SERVERS_TABLE_COLUMNS,
      DATABASES_TABLE_COLUMNS,
      ROLES_TABLE_COLUMNS,
      LAYERS_TABLE_COLUMNS,
      TECHNOLOGIES_TABLE_COLUMNS,
    ]) {
      expect(columns).toContainEqual(
        expect.objectContaining({ key: 'status', sortBy: 'deletedAt' }),
      );
    }
  });

  it('derives the localized status from deletedAt', () => {
    expect(softDeleteStatusLabel(null)).toBe('Actiu');
    expect(softDeleteStatusLabel('2026-07-22T08:00:00')).toBe('Inactiu');
  });
});
