import { Component, input, output } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { AccordionPanel } from 'primeng/accordion';
import { BehaviorSubject } from 'rxjs';

import { RESPONSIBLES_MAINTENANCE_PANELS } from '../../maintenances.constants';
import { ResponsibleAuthorizationsList } from '../../responsibles/pages/responsible-authorizations-list/responsible-authorizations-list';
import { ResponsibleCompaniesList } from '../../responsibles/pages/responsible-companies-list/responsible-companies-list';
import { ResponsiblePeopleList } from '../../responsibles/pages/responsible-people-list/responsible-people-list';
import { RoleTransfer } from '../../responsibles/pages/role-transfer/role-transfer';
import {
  ResponsiblePerson,
  ResponsiblePersonCombinedSearchOutput,
  RoleTransferSourceRequest,
} from '../../responsibles/responsibles.model';
import { ResponsiblesMaintenance } from './responsibles-maintenance';
import {
  RESPONSIBLES_MAINTENANCE_RESOLVE_KEY,
  ResponsiblesMaintenanceResolvedData,
} from './responsibles-maintenance.resolver';

@Component({ selector: 'app-responsible-companies-list', standalone: true, template: '' })
class ResponsibleCompaniesListStub {
  readonly initialLoadFailed = input(false);
  readonly initialPage = input<unknown>();
}

@Component({ selector: 'app-responsible-people-list', standalone: true, template: '' })
class ResponsiblePeopleListStub {
  readonly initialActiveCompanyOptions = input<unknown>();
  readonly initialAllCompanyOptions = input<unknown>();
  readonly initialCompanyOptionsLoadFailed = input(false);
  readonly initialLoadFailed = input(false);
  readonly initialPage = input<unknown>();
  readonly transferRequested = output<ResponsiblePerson>();
}

@Component({ selector: 'app-responsible-authorizations-list', standalone: true, template: '' })
class ResponsibleAuthorizationsListStub {
  readonly initialLoadFailed = input(false);
  readonly initialPage = input<unknown>();
}

@Component({ selector: 'app-role-transfer', standalone: true, template: '' })
class RoleTransferStub {
  readonly initialPeopleSearch = input<ResponsiblePersonCombinedSearchOutput | null>(null);
  readonly initialPeopleSearchFailed = input(false);
  readonly sourceRequest = input<RoleTransferSourceRequest | null>(null);
}

const INITIAL_TRANSFER_PEOPLE: ResponsiblePersonCombinedSearchOutput = {
  database: { content: [], totalElements: 0 } as never,
  soffid: { content: [], totalElements: 0 } as never,
};

const RESOLVED_DATA: ResponsiblesMaintenanceResolvedData = {
  companiesPage: null,
  companiesLoadFailed: false,
  peoplePage: null,
  peopleLoadFailed: false,
  transferPeopleSearch: INITIAL_TRANSFER_PEOPLE,
  transferPeopleSearchFailed: false,
  authorizationsPage: null,
  authorizationsLoadFailed: false,
  activeCompanyOptions: [],
  activeCompanyOptionsLoadFailed: false,
  allCompanyOptions: [],
  allCompanyOptionsLoadFailed: false,
};

describe('ResponsiblesMaintenance', () => {
  let fixture: ComponentFixture<ResponsiblesMaintenance>;
  let fragment: BehaviorSubject<string | null>;
  let navigate: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    fragment = new BehaviorSubject<string | null>(null);
    navigate = vi.fn(() => Promise.resolve(true));
    await TestBed.configureTestingModule({
      imports: [ResponsiblesMaintenance],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              fragment: null,
              data: { [RESPONSIBLES_MAINTENANCE_RESOLVE_KEY]: RESOLVED_DATA },
            },
            fragment,
          },
        },
        { provide: Router, useValue: { navigate } },
      ],
    })
      .overrideComponent(ResponsiblesMaintenance, {
        remove: {
          imports: [
            ResponsibleAuthorizationsList,
            ResponsibleCompaniesList,
            ResponsiblePeopleList,
            RoleTransfer,
          ],
        },
        add: {
          imports: [
            ResponsibleAuthorizationsListStub,
            ResponsibleCompaniesListStub,
            ResponsiblePeopleListStub,
            RoleTransferStub,
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(ResponsiblesMaintenance);
    fixture.detectChanges();
  });

  it('renders the four panels and their descriptions', () => {
    expect(
      fixture.debugElement
        .queryAll(By.directive(AccordionPanel))
        .map(({ componentInstance }) => componentInstance.value()),
    ).toEqual(['role-transfer', 'people', 'authorizations', 'companies']);

    const headers = fixture.nativeElement.querySelectorAll(
      '.maintenance-panel-header',
    ) as NodeListOf<HTMLElement>;

    const renderedHeaders = Array.from(headers).map((header) => ({
      title: header.querySelector('.maintenance-panel-title')?.textContent,
      description: header.querySelector('.maintenance-panel-description')?.textContent,
    }));

    expect(renderedHeaders).toEqual(
      RESPONSIBLES_MAINTENANCE_PANELS.map(({ title, description }) => ({ title, description })),
    );
    expect(renderedHeaders).toEqual(
      expect.arrayContaining([
        {
          title: 'Empreses',
          description: 'Gestiona les empreses responsables de les aplicacions per empresa.',
        },
        {
          title: 'Persones',
          description: 'Gestiona les persones responsables de les aplicacions per empresa.',
        },
      ]),
    );
  });

  it('synchronizes one active panel with the URL fragment', () => {
    (fixture.componentInstance as unknown as { onPanelChange(value: string): void }).onPanelChange(
      'people',
    );
    fixture.detectChanges();

    expect(activePanels()).toEqual(['people']);
    expect(navigate).toHaveBeenCalledWith([], {
      relativeTo: TestBed.inject(ActivatedRoute),
      fragment: 'people',
      queryParamsHandling: 'preserve',
      replaceUrl: true,
    });

    fragment.next('authorizations');
    fixture.detectChanges();
    expect(activePanels()).toEqual(['authorizations']);
  });

  it('opens role transfer and passes a new source request from the people list', () => {
    const person: ResponsiblePerson = {
      id: 12,
      company: null,
      firstName: 'Maria',
      lastName: 'Tur',
      email: 'maria@example.org',
      personalCaib: false,
      deletedAt: null,
    };

    (
      fixture.componentInstance as unknown as {
        onTransferRequested(person: ResponsiblePerson): void;
      }
    ).onTransferRequested(person);
    fixture.detectChanges();

    expect(activePanels()).toEqual(['role-transfer']);
    expect(navigate).toHaveBeenLastCalledWith([], {
      relativeTo: TestBed.inject(ActivatedRoute),
      fragment: 'role-transfer',
      queryParamsHandling: 'preserve',
      replaceUrl: true,
    });
    const transfer = fixture.debugElement.query(By.directive(RoleTransferStub))
      .componentInstance as RoleTransferStub;
    expect(transfer.sourceRequest()).toEqual({ requestId: 1, person });
  });

  it('passes the resolved transfer catalog without requesting it from the panel', () => {
    const transfer = fixture.debugElement.query(By.directive(RoleTransferStub))
      .componentInstance as RoleTransferStub;

    expect(transfer.initialPeopleSearch()).toBe(INITIAL_TRANSFER_PEOPLE);
    expect(transfer.initialPeopleSearchFailed()).toBe(false);
  });

  function activePanels(): string[] {
    return fixture.debugElement
      .queryAll(By.directive(AccordionPanel))
      .filter(({ componentInstance }) => componentInstance.active())
      .map(({ componentInstance }) => componentInstance.value());
  }
});
