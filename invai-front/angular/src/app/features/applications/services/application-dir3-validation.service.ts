import { Injectable, inject } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { ResponsibleDataChangesService } from '@features/maintenances/responsibles/services/responsible-data-changes.service';
import { Observable, tap } from 'rxjs';
import { Dir3Validation } from '../application-dir3.model';

@Injectable({ providedIn: 'root' })
export class ApplicationDir3ValidationService extends BaseApiService {
  protected override readonly ENTITY_URI = 'application/dir3-validation';
  private readonly changes = inject(ResponsibleDataChangesService);

  validateManually(id: number, reason: string): Observable<Dir3Validation> {
    return this.http
      .put<Dir3Validation>(this.url('manual-validate', id), { reason })
      .pipe(tap(() => this.changes.assignmentsChanged()));
  }
}
