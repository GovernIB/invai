import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ResponsibleDataChangesService {
  private readonly companiesSubject = new Subject<void>();
  private readonly peopleSubject = new Subject<void>();
  private readonly authorizationTypesSubject = new Subject<void>();
  private readonly assignmentsSubject = new Subject<void>();

  readonly companies = this.companiesSubject.asObservable();
  readonly people = this.peopleSubject.asObservable();
  readonly authorizationTypes = this.authorizationTypesSubject.asObservable();
  readonly assignments = this.assignmentsSubject.asObservable();

  companiesChanged(): void {
    this.companiesSubject.next();
  }

  peopleChanged(): void {
    this.peopleSubject.next();
  }

  authorizationTypesChanged(): void {
    this.authorizationTypesSubject.next();
  }

  assignmentsChanged(): void {
    this.assignmentsSubject.next();
  }
}
