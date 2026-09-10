import { Injectable, inject } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { Observable, tap } from 'rxjs';

import { RoleAssignmentOutput, RoleTransferInput } from '../responsibles.model';
import { ResponsibleDataChangesService } from './responsible-data-changes.service';

@Injectable({ providedIn: 'root' })
export class RoleTransferService extends BaseApiService {
  protected override readonly ENTITY_URI = 'role-transfer';
  private readonly changes = inject(ResponsibleDataChangesService);

  getAssignments(personId: number): Observable<RoleAssignmentOutput[]> {
    return this.http.get<RoleAssignmentOutput[]>(this.url(personId));
  }

  apply(input: RoleTransferInput): Observable<void> {
    return this.http.post<void>(this.url(), input).pipe(
      tap(() => {
        this.changes.peopleChanged();
        this.changes.assignmentsChanged();
      }),
    );
  }
}
