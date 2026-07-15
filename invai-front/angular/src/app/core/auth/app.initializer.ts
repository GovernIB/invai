import { inject } from '@angular/core';
import { AdministrativeUnitsService } from '@features/administrative-units/services/administrative-units.service';
import { ApplicationsService } from '@features/applications/services/applications.service';
import { CategoriesService } from '@features/categories/services/categories.service';
import { CommissionsService } from '@features/commissions/services/commissions.service';
import { FieldsService } from '@features/fields/services/fields.service';
import { SystemTypesService } from '@features/system-types/services/system-types.service';
import { firstValueFrom } from 'rxjs';
import { initAuth } from './auth.initializer';

const MINIMUM_INITIAL_LOADER_MS = 600;

export async function initApp(): Promise<void> {
  const applicationsService = inject(ApplicationsService);
  const categoriesService = inject(CategoriesService);
  const commissionsService = inject(CommissionsService);
  const fieldsService = inject(FieldsService);
  const administrativeUnitsService = inject(AdministrativeUnitsService);
  const systemTypesService = inject(SystemTypesService);
  const minimumLoaderTime = wait(MINIMUM_INITIAL_LOADER_MS);

  await initAuth();

  await Promise.allSettled([
    firstValueFrom(applicationsService.getPage({ page: 0, size: 10 })),
    firstValueFrom(categoriesService.getAll()),
    firstValueFrom(commissionsService.getAll()),
    firstValueFrom(fieldsService.getAll()),
    firstValueFrom(administrativeUnitsService.getAll()),
    firstValueFrom(systemTypesService.getAll()),
  ]);

  await minimumLoaderTime;
}

function wait(milliseconds: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, milliseconds));
}
