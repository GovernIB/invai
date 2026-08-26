import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SpringPage } from '@models/page.model';
import { ActionParams } from '@models/table.model';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';

import { ResponsibleMaintenanceTableAction } from '../../components/responsible-maintenance-tables/responsible-maintenance-tables';
import { RESPONSIBLE_PERSON_COPY } from '../../responsibles.i18n';
import {
  ResponsiblePerson,
  RoleAssignmentOutput,
  RoleAssignmentType,
} from '../../responsibles.model';
import { ResponsibleCompaniesService } from '../../services/responsible-companies.service';
import { ResponsibleDataChangesService } from '../../services/responsible-data-changes.service';
import { ResponsiblePeopleService } from '../../services/responsible-people.service';
import { RoleTransferService } from '../../services/role-transfer.service';
import { ResponsiblePeopleList } from './responsible-people-list';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}
globalThis.ResizeObserver ??= ResizeObserverMock;

const PERSON: ResponsiblePerson = {
  id: 12,
  company: { id: 3, name: 'Plexus', deletedAt: null },
  firstName: 'Maria',
  lastName: 'Tur Roig',
  email: 'maria@example.org',
  personalCaib: false,
  deletedAt: null,
};

const ASSIGNMENT: RoleAssignmentOutput = {
  id: 20,
  type: RoleAssignmentType.RESPONSIBLE,
  applicationId: 100,
  applicationName: 'Aplicació A',
  responsibleType: {
    id: 1,
    name: 'Responsable',
    nameEs: 'Responsable',
    requiresPersonalCaib: false,
  },
  authorizationTypes: null,
};

interface PeopleListHarness {
  isDeleteDialogVisible(): boolean;
  pendingPersonHasAssignments(): boolean;
  deleteMessage(): string;
  onTableAction(event: ActionParams<ResponsiblePerson>): void;
  confirmDelete(): void;
  transferPendingPerson(): void;
}

describe('ResponsiblePeopleList', () => {
  let fixture: ComponentFixture<ResponsiblePeopleList>;
  let component: PeopleListHarness;
  let getAssignments: ReturnType<typeof vi.fn>;
  let deactivate: ReturnType<typeof vi.fn>;
  let addMessage: ReturnType<typeof vi.fn>;
  let getPage: ReturnType<typeof vi.fn>;
  let getOptions: ReturnType<typeof vi.fn>;
  let changes: ResponsibleDataChangesService;

  beforeEach(async () => {
    getAssignments = vi.fn(() => of([]));
    deactivate = vi.fn(() => of(undefined));
    addMessage = vi.fn();
    getPage = vi.fn(() => of(page([PERSON])));
    getOptions = vi.fn(() => of([]));
    await TestBed.configureTestingModule({
      imports: [ResponsiblePeopleList],
      providers: [
        { provide: MessageService, useValue: { add: addMessage } },
        {
          provide: ResponsiblePeopleService,
          useValue: {
            getPage,
            getById: vi.fn(() => of(PERSON)),
            create: vi.fn(),
            update: vi.fn(),
            deactivate,
            reactivate: vi.fn(),
          },
        },
        {
          provide: ResponsibleCompaniesService,
          useValue: { getOptions },
        },
        {
          provide: RoleTransferService,
          useValue: { getAssignments },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ResponsiblePeopleList);
    component = fixture.componentInstance as unknown as PeopleListHarness;
    changes = TestBed.inject(ResponsibleDataChangesService);
    fixture.detectChanges();
  });

  it('keeps the ordinary confirmation when the person has no assignments', () => {
    requestDeactivation();

    expect(getAssignments).toHaveBeenCalledWith(PERSON.id);
    expect(component.isDeleteDialogVisible()).toBe(true);
    expect(component.pendingPersonHasAssignments()).toBe(false);
    expect(component.deleteMessage()).toBe(
      RESPONSIBLE_PERSON_COPY.deactivateMessage('Maria Tur Roig'),
    );

    component.confirmDelete();
    expect(deactivate).toHaveBeenCalledWith(PERSON.id);
  });

  it('warns about assignments and emits transfer without deactivating the person', () => {
    getAssignments.mockReturnValue(of([ASSIGNMENT]));
    const transferRequested = vi.fn();
    fixture.componentInstance.transferRequested.subscribe(transferRequested);

    requestDeactivation();

    expect(component.pendingPersonHasAssignments()).toBe(true);
    expect(component.deleteMessage()).toContain('responsabilitats o autoritzacions');
    expect(component.deleteMessage()).toContain('quedaran sense una persona assignada');

    component.transferPendingPerson();

    expect(transferRequested).toHaveBeenCalledWith(PERSON);
    expect(component.isDeleteDialogVisible()).toBe(false);
    expect(deactivate).not.toHaveBeenCalled();
  });

  it('blocks deactivation when assignment verification fails', () => {
    getAssignments.mockReturnValue(throwError(() => new Error('network')));

    requestDeactivation();

    expect(component.isDeleteDialogVisible()).toBe(false);
    expect(deactivate).not.toHaveBeenCalled();
    expect(addMessage).toHaveBeenCalledWith(
      expect.objectContaining({
        severity: 'error',
        detail: RESPONSIBLE_PERSON_COPY.assignmentsCheckError,
      }),
    );
  });

  it('reloads company options and the current people page after a company change', () => {
    expect(getPage).toHaveBeenCalledOnce();
    getOptions.mockClear();

    changes.companiesChanged();

    expect(getOptions).toHaveBeenCalledTimes(2);
    expect(getPage).toHaveBeenCalledTimes(2);
  });

  function requestDeactivation(): void {
    component.onTableAction({
      action: ResponsibleMaintenanceTableAction.Deactivate,
      params: PERSON,
    });
    fixture.detectChanges();
  }
});

function page<T>(content: T[]): SpringPage<T> {
  return {
    content,
    empty: content.length === 0,
    first: true,
    last: true,
    number: 0,
    numberOfElements: content.length,
    pageable: {
      offset: 0,
      pageNumber: 0,
      pageSize: 10,
      paged: true,
      sort: { empty: true, sorted: false, unsorted: true },
      unpaged: false,
    },
    size: 10,
    sort: { empty: true, sorted: false, unsorted: true },
    totalElements: content.length,
    totalPages: 1,
  };
}
