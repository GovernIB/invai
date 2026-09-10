import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { CommissionType } from '@features/commissions/commissions.model';
import { ResponsiblePerson } from '@features/maintenances/responsibles/responsibles.model';
import { ResponsiblePeopleService } from '@features/maintenances/responsibles/services/responsible-people.service';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { MessageService } from 'primeng/api';
import { TableLazyLoadEvent } from 'primeng/table';
import { Observable, Subject, of } from 'rxjs';

import { APPLICATIONS_TABLE_COLUMNS } from '../../applications.constants';
import {
  Application,
  ApplicationInfrastructureFilterOptions,
  ApplicationPageParams,
  ApplicationStatus,
} from '../../applications.model';
import { ApplicationFiltersFormGroup } from '../../forms/application-form.factory';
import {
  ApplicationOptionsService,
  ApplicationSelectOptions,
} from '../../services/application-options.service';
import { ApplicationsService } from '../../services/applications.service';
import { ApplicationsList } from './applications-list';
import {
  APPLICATIONS_LIST_RESOLVE_KEY,
  ApplicationsListResolvedData,
} from './applications-list.resolver';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

const APPLICATIONS: Application[] = [
  {
    id: '1',
    code: 'APP-1',
    prefix: 'INV',
    name: 'Invai',
    category: 'DRASSANA',
    informationSystem: 'Instrumental',
    scope: 'Departamental',
    commission: 'Equip directiu',
    department: 'Conselleria',
    administrativeUnit: 'Direcció General',
    status: ApplicationStatus.ACTIVE,
    description: 'Aplicació interna',
    creationDate: '2026-01-01T10:00:00',
    modificationDate: '',
    withdrawalDate: '',
    appResponsibleAuthorizedId: null,
    incomplete: false,
    missingResponsibleTypes: false,
    missingAuthorized: false,
    missingDevelopmentFields: false,
    missingSystems: false,
    missingDatabases: false,
    missingAccessibilityFields: false,
    missingSecurityData: false,
  },
  {
    id: '2',
    code: 'APP-2',
    prefix: 'WEB',
    name: 'Portal',
    category: 'Web',
    informationSystem: 'Corporatiu',
    scope: 'Transversal',
    commission: 'Comissió tècnica',
    department: 'Conselleria',
    administrativeUnit: 'Servei TIC',
    status: ApplicationStatus.INACTIVE,
    description: 'Portal corporatiu',
    creationDate: '2026-02-01T10:00:00',
    modificationDate: '',
    withdrawalDate: '',
    appResponsibleAuthorizedId: null,
    incomplete: false,
    missingResponsibleTypes: false,
    missingAuthorized: false,
    missingDevelopmentFields: false,
    missingSystems: false,
    missingDatabases: false,
    missingAccessibilityFields: false,
    missingSecurityData: false,
  },
];

const FILTER_OPTIONS: ApplicationSelectOptions = {
  categories: [{ label: 'DRASSANA', value: 1 }],
  informationSystems: [{ label: 'Instrumental', value: 2 }],
  scopes: [{ label: 'Departamental', value: 3 }],
  commissions: [
    {
      label: 'Equip directiu',
      value: 4,
      expedientNumber: 'EXP-4',
      approvalDate: '2026-07-14',
      commissionType: CommissionType.SUPERIOR,
    },
  ],
  departments: [{ label: 'Conselleria', value: 'GVA01' }],
  administrativeUnits: [{ label: 'Direcció General', value: 'UA01' }],
};

const INFRASTRUCTURE_FILTER_OPTIONS: ApplicationInfrastructureFilterOptions = {
  servers: [{ label: 'app01.caib.es', value: 5 }],
  databases: [{ label: 'INVAI', value: 8 }],
  environments: [{ label: 'Producció', value: 3 }],
};

const RESPONSIBLE_PERSON: ResponsiblePerson = {
  id: 11,
  company: null,
  firstName: 'Maria',
  lastName: 'Tur',
  email: 'maria.tur@example.org',
  personalCaib: true,
  deletedAt: null,
};

interface ApplicationsListAccess {
  filtersForm: ApplicationFiltersFormGroup;
  isSearchIndicatorLoading: () => boolean;
  onFilterSearch: () => void;
  onPageChange: (event: TableLazyLoadEvent) => void;
  onQuickSearchChange: (value: string) => void;
  onResponsibleSearch: (value: string) => void;
  responsibleOptions: () => { id: number; label: string }[];
  isResponsibleSearchLoading: () => boolean;
  tableFirst: () => number;
}

describe('ApplicationsList', () => {
  let component: ApplicationsList;
  let fixture: ComponentFixture<ApplicationsList>;
  let messageService: MessageService;
  let getPage: ReturnType<typeof vi.fn>;
  let navigate: ReturnType<typeof vi.fn>;
  let activatedRoute: object;
  let pendingPages: Subject<SpringPage<Application>>[];
  let getPeoplePage: ReturnType<typeof vi.fn>;
  let pendingPeoplePages: Subject<SpringPage<ResponsiblePerson>>[];

  beforeEach(async () => {
    pendingPages = [];
    pendingPeoplePages = [];
    getPage = vi.fn((_params?: ApplicationPageParams): Observable<SpringPage<Application>> => {
      const request = new Subject<SpringPage<Application>>();
      pendingPages.push(request);
      return request;
    });
    navigate = vi.fn();
    getPeoplePage = vi.fn(() => {
      const request = new Subject<SpringPage<ResponsiblePerson>>();
      pendingPeoplePages.push(request);
      return request;
    });
    activatedRoute = {
      snapshot: {
        data: {
          [APPLICATIONS_LIST_RESOLVE_KEY]: resolvedData(APPLICATIONS, 25),
        },
      },
    };

    await TestBed.configureTestingModule({
      imports: [ApplicationsList],
      providers: [
        MessageService,
        { provide: ApplicationsService, useValue: { getPage } },
        {
          provide: ApplicationOptionsService,
          useValue: {
            getDepartmentOptions: vi.fn(() => of(FILTER_OPTIONS.departments)),
            getAdministrativeUnitOptions: vi.fn(() => of(FILTER_OPTIONS.administrativeUnits)),
          },
        },
        { provide: ResponsiblePeopleService, useValue: { getPage: getPeoplePage } },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Router, useValue: { navigate } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationsList);
    component = fixture.componentInstance;
    messageService = TestBed.inject(MessageService);
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should consume the resolved page without requesting it again', () => {
    expect(component).toBeTruthy();
    expect(getPage).not.toHaveBeenCalled();
    expect(component.itemsList()).toEqual({ items: APPLICATIONS, total: 25 });
    expect(component.isLoading()).toBe(false);
    expect(fixture.nativeElement.querySelector('.section-actions-search__spinner')).toBeFalsy();
    expect(fixture.nativeElement.querySelectorAll('.invai-table-skeleton-row')).toHaveLength(0);
    expect(fixture.nativeElement.querySelector('.p-datatable-mask')).toBeFalsy();
    expect(
      fixture.nativeElement
        .querySelector('.invai-table-loading-container')
        .getAttribute('aria-busy'),
    ).toBe('false');
  });

  it('should expose the server page and total in the table', () => {
    expect(component.itemsList()).toEqual({ items: APPLICATIONS, total: 25 });
    expect(component.isLoading()).toBe(false);
  });

  it('should render localized application statuses instead of enum values', () => {
    const statusColumn = APPLICATIONS_TABLE_COLUMNS.find(({ key }) => key === 'status')!;
    component.selectedColumns.update((columns) => [...columns, statusColumn]);
    fixture.detectChanges();

    const tableText = fixture.nativeElement.querySelector('.p-datatable-tbody').textContent;
    expect(tableText).toContain('Actiu');
    expect(tableText).toContain('Inactiu');
    expect(tableText).not.toContain('Activo');
    expect(tableText).not.toContain('Inactivo');
  });

  it('should hide commission and status by default while keeping them selectable', () => {
    const visibleKeys = component.sortedSelectedColumns().map(({ key }) => key);
    const selectableKeys = component.selectableColumns().map(({ key }) => key);

    expect(visibleKeys).not.toContain('commission');
    expect(visibleKeys).not.toContain('status');
    expect(selectableKeys).toContain('commission');
    expect(selectableKeys).toContain('status');
  });

  it('should hide the infrastructure and responsible columns by default while keeping them selectable', () => {
    const infrastructureKeys = ['environment', 'database', 'server', 'responsible'];
    const visibleKeys = component.sortedSelectedColumns().map(({ key }) => key);
    const selectableKeys = component.selectableColumns().map(({ key }) => key);

    infrastructureKeys.forEach((key) => {
      expect(visibleKeys).not.toContain(key);
      expect(selectableKeys).toContain(key);
    });
  });

  it('should render the infrastructure and responsible columns once selected', () => {
    const infrastructureColumns = APPLICATIONS_TABLE_COLUMNS.filter(({ key }) =>
      ['environment', 'database', 'server', 'responsible'].includes(key),
    );
    component.selectedColumns.update((columns) => [...columns, ...infrastructureColumns]);
    fixture.detectChanges();

    const headerText = fixture.nativeElement.querySelector('.p-datatable-thead').textContent;
    expect(headerText).toContain('Entorn');
    expect(headerText).toContain('Bases de dades');
    expect(headerText).toContain('Servidor');
    expect(headerText).toContain('Responsables');
  });

  it('should synchronize status only when its applied criterion changes or resets', () => {
    const list = accessList();
    const commissionColumn = APPLICATIONS_TABLE_COLUMNS.find(({ key }) => key === 'commission')!;
    const statusColumn = APPLICATIONS_TABLE_COLUMNS.find(({ key }) => key === 'status')!;

    component.selectedColumns.update((columns) => [...columns, commissionColumn]);
    list.filtersForm.controls.status.setValue(null);
    list.onFilterSearch();

    expect(component.sortedSelectedColumns().map(({ key }) => key)).toEqual(
      expect.arrayContaining(['commission', 'status']),
    );

    component.selectedColumns.update((columns) => columns.filter(({ key }) => key !== 'status'));
    list.filtersForm.controls.application.setValue('interna');
    list.onFilterSearch();

    expect(component.sortedSelectedColumns().map(({ key }) => key)).not.toContain('status');

    list.filtersForm.controls.status.setValue(ApplicationStatus.INACTIVE);
    list.onFilterSearch();

    expect(component.sortedSelectedColumns().map(({ key }) => key)).toContain('commission');
    expect(component.sortedSelectedColumns().map(({ key }) => key)).not.toContain('status');

    component.selectedColumns.update((columns) => [...columns, statusColumn]);
    component.reset();

    expect(component.sortedSelectedColumns().map(({ key }) => key)).toContain('commission');
    expect(component.sortedSelectedColumns().map(({ key }) => key)).not.toContain('status');
  });

  it('should keep the applied all-statuses search while status edits are pending', () => {
    const list = accessList();

    list.filtersForm.controls.status.setValue(null);
    expect(component.sortedSelectedColumns().map(({ key }) => key)).not.toContain('status');

    list.onFilterSearch();
    expect(component.sortedSelectedColumns().map(({ key }) => key)).toContain('status');
    expect(getPage).toHaveBeenLastCalledWith(
      expect.objectContaining({ page: 0, size: 10, statusId: undefined }),
    );

    list.filtersForm.controls.status.setValue(ApplicationStatus.INACTIVE);
    list.onPageChange({ first: 10, rows: 10 } as TableLazyLoadEvent);

    expect(component.sortedSelectedColumns().map(({ key }) => key)).toContain('status');
    expect(getPage).toHaveBeenLastCalledWith(
      expect.objectContaining({ page: 1, size: 10, statusId: undefined }),
    );

    list.onFilterSearch();

    expect(component.sortedSelectedColumns().map(({ key }) => key)).not.toContain('status');
    expect(getPage).toHaveBeenLastCalledWith(
      expect.objectContaining({
        page: 0,
        size: 10,
        statusId: ApplicationStatus.INACTIVE,
      }),
    );
  });

  it('should keep the current rows and show a discreet progress indicator while refreshing', () => {
    fixture.detectChanges();
    const list = accessList();

    list.onPageChange({ first: 10, rows: 10 } as TableLazyLoadEvent);
    fixture.detectChanges();

    const loadingContainer = fixture.nativeElement.querySelector(
      '.invai-table-loading-container',
    ) as HTMLElement;
    expect(loadingContainer.textContent).toContain('Invai');
    expect(loadingContainer.getAttribute('aria-busy')).toBe('true');
    expect(loadingContainer.querySelector('.invai-table-loading-shield')).toBeTruthy();
    expect(loadingContainer.querySelector('.invai-table-refresh-indicator')).toBeTruthy();
    expect(loadingContainer.querySelector('.p-datatable-mask')).toBeFalsy();
    expect(loadingContainer.querySelector('.invai-table-skeleton-row')).toBeFalsy();

    resolvePage(0, [APPLICATIONS[1]], 1);
    fixture.detectChanges();

    expect(loadingContainer.textContent).not.toContain('Invai');
    expect(loadingContainer.textContent).toContain('Portal');
    expect(loadingContainer.getAttribute('aria-busy')).toBe('false');
    expect(loadingContainer.querySelector('.invai-table-loading-shield')).toBeFalsy();
  });

  it('should replace the initial skeletons with the empty state after a successful empty load', () => {
    recreateWithResolvedData(resolvedData([], 0));

    expect(fixture.nativeElement.querySelector('.invai-table-skeleton-row')).toBeFalsy();
    expect(fixture.nativeElement.querySelector('.invai-table-loading-shield')).toBeFalsy();
    expect(fixture.nativeElement.textContent).toContain("No s'han trobat resultats");
  });

  it('should map filters, pagination and nested sorting to API criteria', () => {
    const list = accessList();
    list.filtersForm.patchValue({
      prefix: ' INV ',
      application: ' Portal ',
      category: 1,
      informationSystem: 2,
      scope: 3,
      commission: 4,
      conselleria: 'GVA01',
      administrativeUnit: 'UA01',
      status: ApplicationStatus.INACTIVE,
      responsible: { id: 11, label: 'Maria Tur' },
      database: 8,
      server: 5,
      environment: 3,
    });

    list.onFilterSearch();
    list.onPageChange({
      first: 40,
      rows: 20,
      sortField: 'category.name',
      sortOrder: -1,
    } as TableLazyLoadEvent);

    expect(getPage).toHaveBeenLastCalledWith({
      page: 2,
      size: 20,
      sort: 'category.name,desc',
      prefix: 'INV',
      applicationName: 'Portal',
      categoryId: 1,
      systemTypeId: 2,
      fieldId: 3,
      commissionId: 4,
      admUnitCode: 'UA01',
      statusId: ApplicationStatus.INACTIVE,
      responsibleId: 11,
      databaseId: 8,
      serverId: 5,
      environmentId: 3,
    });
    expect(list.tableFirst()).toBe(40);
  });

  it('should show loading immediately and debounce quick search for 400 ms', () => {
    fixture.detectChanges();
    vi.useFakeTimers();
    const list = accessList();

    expect(fixture.nativeElement.querySelector('.section-actions-search__spinner')).toBeFalsy();
    list.onQuickSearchChange('i');
    fixture.detectChanges();

    expect(list.isSearchIndicatorLoading()).toBe(true);
    expect(fixture.nativeElement.querySelector('.section-actions-search__spinner')).toBeTruthy();
    vi.advanceTimersByTime(399);
    expect(getPage).not.toHaveBeenCalled();

    vi.advanceTimersByTime(1);

    expect(getPage).toHaveBeenCalledOnce();
    expect(getPage).toHaveBeenLastCalledWith({
      page: 0,
      size: 10,
      statusId: ApplicationStatus.ACTIVE,
      quickSearch: 'i',
    });
    expect(component.isLoading()).toBe(true);
    resolvePage(0, [APPLICATIONS[0]], 1);
    fixture.detectChanges();
    expect(component.isLoading()).toBe(false);
    expect(list.isSearchIndicatorLoading()).toBe(false);
    expect(fixture.nativeElement.querySelector('.section-actions-search__spinner')).toBeFalsy();
  });

  it('should cancel stale quick searches and ignore repeated trimmed values', () => {
    vi.useFakeTimers();
    const list = accessList();

    list.onQuickSearchChange('inv');
    vi.advanceTimersByTime(400);
    list.onQuickSearchChange('invai');
    vi.advanceTimersByTime(400);

    pendingPages[0].next(applicationPage([APPLICATIONS[1]], 1));
    expect(component.itemsList().items).toEqual(APPLICATIONS);

    resolvePage(1, [APPLICATIONS[0]], 1);
    expect(component.itemsList().items).toEqual([APPLICATIONS[0]]);

    list.onQuickSearchChange(' invai ');
    expect(list.isSearchIndicatorLoading()).toBe(true);
    vi.advanceTimersByTime(400);
    expect(getPage).toHaveBeenCalledTimes(2);
    expect(list.isSearchIndicatorLoading()).toBe(false);
  });

  it('should clear the quick search from its remove button and reload the first page', () => {
    vi.useFakeTimers();
    const searchInput = fixture.nativeElement.querySelector(
      '.section-actions-search__input',
    ) as HTMLInputElement;
    searchInput.value = 'inv';
    searchInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    vi.advanceTimersByTime(400);
    resolvePage(0, [APPLICATIONS[0]], 1);
    fixture.detectChanges();

    const clearButton = fixture.nativeElement.querySelector(
      '.section-actions-search__clear button',
    ) as HTMLButtonElement;
    clearButton.click();
    fixture.detectChanges();
    vi.advanceTimersByTime(400);

    expect(searchInput.value).toBe('');
    expect(getPage).toHaveBeenLastCalledWith({
      page: 0,
      size: 10,
      statusId: ApplicationStatus.ACTIVE,
    });
    expect(accessList().tableFirst()).toBe(0);
  });

  it('should request incomplete applications and omit the filter when unchecked', () => {
    const list = accessList();
    list.filtersForm.patchValue({ incomplete: true });
    list.onFilterSearch();

    expect(component.selectedFilters()).toBe(2);
    expect(getPage).toHaveBeenLastCalledWith({
      page: 0,
      size: 10,
      statusId: ApplicationStatus.ACTIVE,
      incomplete: true,
    });

    list.filtersForm.patchValue({ incomplete: false });
    list.onFilterSearch();

    expect(getPage).toHaveBeenLastCalledWith({
      page: 0,
      size: 10,
      statusId: ApplicationStatus.ACTIVE,
    });
    expect(component.selectedFilters()).toBe(1);
  });

  it('should debounce remote responsible searches and map active people to options', () => {
    vi.useFakeTimers();
    const list = accessList();

    list.onResponsibleSearch(' Maria ');
    expect(list.isResponsibleSearchLoading()).toBe(true);
    vi.advanceTimersByTime(399);
    expect(getPeoplePage).not.toHaveBeenCalled();

    vi.advanceTimersByTime(1);
    expect(getPeoplePage).toHaveBeenCalledWith({
      page: 0,
      size: 20,
      sort: ['firstName,asc', 'lastName,asc'],
      statusId: SoftDeleteStatus.ACTIVE,
      search: 'Maria',
    });

    pendingPeoplePages[0].next(applicationPage([RESPONSIBLE_PERSON], 1));
    pendingPeoplePages[0].complete();

    expect(list.responsibleOptions()).toEqual([{ id: 11, label: 'Maria Tur' }]);
    expect(list.isResponsibleSearchLoading()).toBe(false);
  });

  it('should cancel stale responsible searches and report the latest search error', () => {
    vi.useFakeTimers();
    const list = accessList();
    const addSpy = vi.spyOn(messageService, 'add');

    list.onResponsibleSearch('Maria');
    vi.advanceTimersByTime(400);
    list.onResponsibleSearch('Martina');
    vi.advanceTimersByTime(400);

    pendingPeoplePages[0].next(applicationPage([RESPONSIBLE_PERSON], 1));
    expect(list.responsibleOptions()).toEqual([]);

    pendingPeoplePages[1].error(new Error('Search failed'));
    expect(list.isResponsibleSearchLoading()).toBe(false);
    expect(addSpy).toHaveBeenLastCalledWith({
      severity: 'error',
      summary: 'Error',
      detail: "No s'han pogut cercar les persones responsables.",
    });
  });

  it('should send the connected responsible and infrastructure filters', () => {
    const list = accessList();
    list.filtersForm.patchValue({
      responsible: { id: 11, label: 'Maria Tur' },
      database: 8,
      server: 5,
      environment: 3,
    });

    list.onFilterSearch();

    expect(component.selectedFilters()).toBe(5);
    expect(getPage).toHaveBeenLastCalledWith({
      page: 0,
      size: 10,
      statusId: ApplicationStatus.ACTIVE,
      responsibleId: 11,
      databaseId: 8,
      serverId: 5,
      environmentId: 3,
    });
  });

  it('should clear the table and show an error when loading fails', () => {
    const addSpy = vi.spyOn(messageService, 'add');
    accessList().onFilterSearch();

    pendingPages[0].error(new Error('Request failed'));

    expect(component.isLoading()).toBe(false);
    expect(component.itemsList()).toEqual({ items: [], total: 0 });
    expect(addSpy).toHaveBeenCalledWith({
      severity: 'error',
      summary: 'Error',
      detail: "No s'han pogut carregar les aplicacions.",
    });
  });

  it('should show the page error resolved by the route without making another request', () => {
    const addSpy = vi.spyOn(messageService, 'add');

    recreateWithResolvedData({
      page: null,
      options: FILTER_OPTIONS,
      infrastructureOptions: INFRASTRUCTURE_FILTER_OPTIONS,
      pageLoadFailed: true,
      optionsLoadFailed: false,
      departmentsLoadFailed: false,
      administrativeUnitsLoadFailed: false,
    });

    expect(getPage).not.toHaveBeenCalled();
    expect(component.itemsList()).toEqual({ items: [], total: 0 });
    expect(addSpy).toHaveBeenCalledWith({
      severity: 'error',
      summary: 'Error',
      detail: "No s'han pogut carregar les aplicacions.",
    });
  });

  it('should reset filters, their count and the paginator', () => {
    const list = accessList();
    list.filtersForm.patchValue({
      prefix: 'APP',
      category: 1,
      responsible: { id: 11, label: 'Maria Tur' },
      database: 8,
      server: 5,
      environment: 3,
      incomplete: true,
    });
    list.onFilterSearch();
    list.onPageChange({ first: 20, rows: 10 } as TableLazyLoadEvent);
    expect(component.selectedFilters()).toBe(8);

    component.reset();

    expect(component.selectedFilters()).toBe(1);
    expect(list.tableFirst()).toBe(0);
    expect(list.filtersForm.getRawValue()).toEqual({
      prefix: null,
      application: null,
      category: null,
      informationSystem: null,
      scope: null,
      commission: null,
      conselleria: null,
      administrativeUnit: null,
      status: ApplicationStatus.ACTIVE,
      responsible: null,
      database: null,
      server: null,
      environment: null,
      incomplete: false,
    });
  });

  it('should not expose an incomplete table column', () => {
    expect(component.selectableColumns().some((column) => column.key === 'incomplete')).toBe(false);
    expect(component.sortedSelectedColumns().some((column) => column.key === 'incomplete')).toBe(
      false,
    );
  });

  it('should not expose an actions column', () => {
    fixture.detectChanges();
    const firstRow = fixture.nativeElement.querySelector(
      '.p-datatable-tbody > tr',
    ) as HTMLTableRowElement;

    expect(fixture.nativeElement.querySelector('.invai-table-actions-column')).toBeFalsy();
    expect(firstRow.querySelectorAll('td')).toHaveLength(component.sortedSelectedColumns().length);
    expect(firstRow.querySelector('button')).toBeFalsy();
    expect(firstRow.querySelector('.pi-eye')).toBeFalsy();
  });

  it('should navigate to the application detail on row double click', () => {
    fixture.detectChanges();
    const firstRow = fixture.nativeElement.querySelector(
      '.p-datatable-tbody > tr',
    ) as HTMLTableRowElement;

    firstRow.dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));

    expect(navigate).toHaveBeenCalledWith(['1'], { relativeTo: activatedRoute });
  });

  it('should navigate to the application detail with Enter on a focused row', () => {
    fixture.detectChanges();
    const firstRow = fixture.nativeElement.querySelector(
      '.p-datatable-tbody > tr',
    ) as HTMLTableRowElement;

    expect(firstRow.tabIndex).toBe(0);

    firstRow.dispatchEvent(new KeyboardEvent('keydown', { key: 'Space', bubbles: true }));
    expect(navigate).not.toHaveBeenCalled();

    firstRow.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    expect(navigate).toHaveBeenCalledWith(['1'], { relativeTo: activatedRoute });
  });

  function accessList(): ApplicationsListAccess {
    return component as unknown as ApplicationsListAccess;
  }

  function resolvePage(index: number, content: Application[], total = content.length): void {
    pendingPages[index].next(applicationPage(content, total));
    pendingPages[index].complete();
  }

  function recreateWithResolvedData(data: ApplicationsListResolvedData): void {
    fixture.destroy();
    (
      activatedRoute as {
        snapshot: { data: Record<string, ApplicationsListResolvedData> };
      }
    ).snapshot.data[APPLICATIONS_LIST_RESOLVE_KEY] = data;
    fixture = TestBed.createComponent(ApplicationsList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }
});

function resolvedData(content: Application[], totalElements: number): ApplicationsListResolvedData {
  return {
    page: applicationPage(content, totalElements),
    options: FILTER_OPTIONS,
    infrastructureOptions: INFRASTRUCTURE_FILTER_OPTIONS,
    pageLoadFailed: false,
    optionsLoadFailed: false,
    departmentsLoadFailed: false,
    administrativeUnitsLoadFailed: false,
  };
}

function applicationPage<TItem>(content: TItem[], totalElements: number): SpringPage<TItem> {
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
    totalElements,
    totalPages: Math.ceil(totalElements / 10),
  };
}
