import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { RoleAssignmentOutput, RoleAssignmentType, RoleTransferInput } from '../responsibles.model';
import { ResponsibleDataChangesService } from './responsible-data-changes.service';
import { RoleTransferService } from './role-transfer.service';

describe('RoleTransferService', () => {
  let http: HttpTestingController;
  let service: RoleTransferService;
  let changes: ResponsibleDataChangesService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    service = TestBed.inject(RoleTransferService);
    changes = TestBed.inject(ResponsibleDataChangesService);
  });

  afterEach(() => http.verify());

  it('loads the active assignments of one person', () => {
    const expected: RoleAssignmentOutput[] = [
      {
        id: 4,
        type: RoleAssignmentType.RESPONSIBLE,
        applicationId: 9,
        applicationName: 'Invai',
        responsibleType: {
          id: 2,
          name: 'Responsable del servei',
          nameEs: 'Responsable del servicio',
          requiresPersonalCaib: false,
        },
        authorizationTypes: null,
      },
      {
        id: 5,
        type: RoleAssignmentType.AUTHORIZED,
        applicationId: 9,
        applicationName: 'Invai',
        responsibleType: null,
        authorizationTypes: [
          {
            id: 7,
            name: 'Signar peticions',
            nameEs: 'Firmar peticiones',
            deletedAt: null,
          },
          {
            id: 8,
            name: 'Autorització històrica',
            nameEs: 'Autorización histórica',
            deletedAt: '2026-08-01T10:00:00',
          },
        ],
      },
    ];

    service.getAssignments(12).subscribe((assignments) => expect(assignments).toEqual(expected));

    const request = http.expectOne('/invaiapi/interna/role-transfer/12');
    expect(request.request.method).toBe('GET');
    request.flush(expected);
  });

  it('posts the exact batch and announces person and assignment changes only after success', () => {
    const assignmentsChanged = vi.fn();
    const peopleChanged = vi.fn();
    changes.assignments.subscribe(assignmentsChanged);
    changes.people.subscribe(peopleChanged);
    const input: RoleTransferInput = {
      items: [{ id: 4, type: RoleAssignmentType.AUTHORIZED }],
      toPersonEmailAddress: null,
      revoke: true,
    };

    service.apply(input).subscribe();
    const request = http.expectOne('/invaiapi/interna/role-transfer');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(input);
    expect(assignmentsChanged).not.toHaveBeenCalled();
    expect(peopleChanged).not.toHaveBeenCalled();
    request.flush(null, { status: 204, statusText: 'No Content' });
    expect(assignmentsChanged).toHaveBeenCalledOnce();
    expect(peopleChanged).toHaveBeenCalledOnce();
  });
});
