import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { catchError, map, of } from 'rxjs';

import { Field } from '../../fields.model';
import { FieldsService } from '../../services/fields.service';

export const FIELDS_LIST_RESOLVE_KEY = 'fieldsList';

export interface FieldsListResolvedData {
  page: SpringPage<Field> | null;
  pageLoadFailed: boolean;
}

export const fieldsListResolver: ResolveFn<FieldsListResolvedData> = () =>
  inject(FieldsService)
    .getAll({ page: 0, size: 10 })
    .pipe(
      map((page) => ({ page, pageLoadFailed: false })),
      catchError(() => of({ page: null, pageLoadFailed: true })),
    );
