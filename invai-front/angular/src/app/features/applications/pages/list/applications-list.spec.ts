import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { MessageService } from 'primeng/api';
import { TableLazyLoadEvent } from 'primeng/table';
import { Observable, Subject, of, throwError } from 'rxjs';

import { Application, ApplicationPageParams } from '../../applications.model';
import { ApplicationTableAction } from '../../components';
import { ApplicationFiltersFormGroup } from '../../forms/application-form.factory';
import {
  ApplicationOptionsService,
  ApplicationSelectOptions,
} from '../../services/application-options.service';
import { ApplicationsService } from '../../services/applications.service';
import { ApplicationsList } from './applications-list';

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
    administrativeUnit: 'Direcció General',
    status: 'Activo',
    description: 'Aplicació interna',
    creationDate: '2026-01-01T10:00:00',
    modificationDate: '',
    withdrawalDate: '',
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
    administrativeUnit: 'Servei TIC',
    status: 'Activo',
    description: 'Portal corporatiu',
    creationDate: '2026-02-01T10:00:00',
    modificationDate: '',
    withdrawalDate: '',
  },
];

const FILTER_OPTIONS: ApplicationSelectOptions = {
  categories: [{ label: 'DRASSANA', value: 1 }],
  informationSystems: [{ label: 'Instrumental', value: 2 }],
  scopes: [{ label: 'Departamental', value: 3 }],
  commissions: [{ label: 'Equip directiu', value: 4 }],
  administrativeUnits: [{ label: 'Direcció General', value: 5 }],
};

interface ApplicationsListAccess {
  filtersForm: ApplicationFiltersFormGroup;
  isSearchIndicatorLoading: () => boolean;
  isWithdrawalDialogVisible: () => boolean;
  onFilterSearch: () => void;
  onPageChange: (event: TableLazyLoadEvent) => void;
  onQuickSearchChange: (value: string) => void;
  onTableActions: (event: { action: ApplicationTableAction; params: Application }) => void;
  onWithdrawalDialogConfirm: () => void;
  selectedApplicationForWithdrawal: () => Application | null;
  tableFirst: () => number;
}

describe('ApplicationsList', () => {
  let component: ApplicationsList;
  let fixture: ComponentFixture<ApplicationsList>;
  let messageService: MessageService;
  let getPage: ReturnType<typeof vi.fn>;
  let deleteApplication: ReturnType<typeof vi.fn>;
  let pendingPages: Subject<SpringPage<Application>>[];

  beforeEach(async () => {
    pendingPages = [];
    getPage = vi.fn((_params?: ApplicationPageParams): Observable<SpringPage<Application>> => {
      const request = new Subject<SpringPage<Application>>();
      pendingPages.push(request);
      return request;
    });
    deleteApplication = vi.fn(() => of(null));

    await TestBed.configureTestingModule({
      imports: [ApplicationsList],
      providers: [
        MessageService,
        { provide: ApplicationsService, useValue: { getPage, delete: deleteApplication } },
        { provide: ApplicationOptionsService, useValue: { getOptions: () => of(FILTER_OPTIONS) } },
        { provide: ActivatedRoute, useValue: {} },
        { provide: Router, useValue: { navigate: vi.fn() } },
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

  it('should request the first server page on initialization', () => {
    expect(component).toBeTruthy();
    expect(getPage).toHaveBeenCalledOnce();
    expect(getPage).toHaveBeenCalledWith({ page: 0, size: 10 });
    expect(component.isLoading()).toBe(true);
    expect(fixture.nativeElement.querySelector('.section-actions-search__spinner')).toBeTruthy();
  });

  it('should expose the server page and total in the table', () => {
    resolvePage(0, APPLICATIONS, 25);

    expect(component.itemsList()).toEqual({ items: APPLICATIONS, total: 25 });
    expect(component.isLoading()).toBe(false);
  });

  it('should map filters, pagination and nested sorting to API criteria', () => {
    resolvePage(0, APPLICATIONS);
    const list = accessList();
    list.filtersForm.patchValue({
      prefix: ' INV ',
      application: ' Portal ',
      category: 1,
      informationSystem: 2,
      scope: 3,
      commission: 4,
      administrativeUnit: 5,
      status: 2,
      description: ' interna ',
    });

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
      admUnitId: 5,
      statusId: 2,
      description: 'interna',
    });
    expect(list.tableFirst()).toBe(40);
  });

  it('should show loading immediately and debounce quick search for 400 ms', () => {
    resolvePage(0, APPLICATIONS);
    fixture.detectChanges();
    vi.useFakeTimers();
    const list = accessList();

    expect(fixture.nativeElement.querySelector('.section-actions-search__spinner')).toBeFalsy();
    list.onQuickSearchChange('i');
    fixture.detectChanges();

    expect(list.isSearchIndicatorLoading()).toBe(true);
    expect(fixture.nativeElement.querySelector('.section-actions-search__spinner')).toBeTruthy();
    vi.advanceTimersByTime(399);
    expect(getPage).toHaveBeenCalledOnce();

    vi.advanceTimersByTime(1);

    expect(getPage).toHaveBeenCalledTimes(2);
    expect(getPage).toHaveBeenLastCalledWith({ page: 0, size: 10, quickSearch: 'i' });
    expect(component.isLoading()).toBe(true);
    resolvePage(1, [APPLICATIONS[0]], 1);
    fixture.detectChanges();
    expect(component.isLoading()).toBe(false);
    expect(list.isSearchIndicatorLoading()).toBe(false);
    expect(fixture.nativeElement.querySelector('.section-actions-search__spinner')).toBeFalsy();
  });

  it('should cancel stale quick searches and ignore repeated trimmed values', () => {
    resolvePage(0, APPLICATIONS);
    vi.useFakeTimers();
    const list = accessList();

    list.onQuickSearchChange('inv');
    vi.advanceTimersByTime(400);
    list.onQuickSearchChange('invai');
    vi.advanceTimersByTime(400);

    pendingPages[1].next(applicationPage([APPLICATIONS[1]], 1));
    expect(component.itemsList().items).toEqual(APPLICATIONS);

    resolvePage(2, [APPLICATIONS[0]], 1);
    expect(component.itemsList().items).toEqual([APPLICATIONS[0]]);

    list.onQuickSearchChange(' invai ');
    expect(list.isSearchIndicatorLoading()).toBe(true);
    vi.advanceTimersByTime(400);
    expect(getPage).toHaveBeenCalledTimes(3);
    expect(list.isSearchIndicatorLoading()).toBe(false);
  });

  it('should clear the quick search from its remove button and reload the first page', () => {
    resolvePage(0, APPLICATIONS);
    vi.useFakeTimers();
    const searchInput = fixture.nativeElement.querySelector(
      '.section-actions-search__input',
    ) as HTMLInputElement;
    searchInput.value = 'inv';
    searchInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    vi.advanceTimersByTime(400);
    resolvePage(1, [APPLICATIONS[0]], 1);
    fixture.detectChanges();

    const clearButton = fixture.nativeElement.querySelector(
      '.section-actions-search__clear button',
    ) as HTMLButtonElement;
    clearButton.click();
    fixture.detectChanges();
    vi.advanceTimersByTime(400);

    expect(searchInput.value).toBe('');
    expect(getPage).toHaveBeenLastCalledWith({ page: 0, size: 10 });
    expect(accessList().tableFirst()).toBe(0);
  });

  it('should count incomplete but omit it from the request and warn in the console', () => {
    resolvePage(0, APPLICATIONS);
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => undefined);
    const list = accessList();
    list.filtersForm.patchValue({ incomplete: true });

    list.onFilterSearch();

    expect(component.selectedFilters()).toBe(1);
    expect(warn).toHaveBeenCalledOnce();
    expect(getPage).toHaveBeenLastCalledWith({ page: 0, size: 10 });
  });

  it('should clear the table and show an error when loading fails', () => {
    const addSpy = vi.spyOn(messageService, 'add');

    pendingPages[0].error(new Error('Request failed'));

    expect(component.isLoading()).toBe(false);
    expect(component.itemsList()).toEqual({ items: [], total: 0 });
    expect(addSpy).toHaveBeenCalledWith({
      severity: 'error',
      summary: 'Error',
      detail: "No s'han pogut carregar les aplicacions.",
    });
  });

  it('should reset filters, their count and the paginator', () => {
    resolvePage(0, APPLICATIONS);
    const list = accessList();
    list.filtersForm.patchValue({ prefix: 'APP', category: 1, incomplete: true });
    list.onPageChange({ first: 20, rows: 10 } as TableLazyLoadEvent);
    expect(component.selectedFilters()).toBe(3);

    component.reset();

    expect(component.selectedFilters()).toBe(0);
    expect(list.tableFirst()).toBe(0);
    expect(list.filtersForm.getRawValue()).toEqual({
      prefix: null,
      application: null,
      category: null,
      informationSystem: null,
      scope: null,
      commission: null,
      administrativeUnit: null,
      status: null,
      description: null,
      incomplete: false,
    });
  });

  it('should not expose an incomplete table column', () => {
    expect(component.selectableColumns().some((column) => column.key === 'incomplete')).toBe(false);
    expect(component.sortedSelectedColumns().some((column) => column.key === 'incomplete')).toBe(
      false,
    );
  });

  it('should open the withdrawal dialog when deleting an application', () => {
    const list = accessList();
    list.onTableActions({ action: ApplicationTableAction.Delete, params: APPLICATIONS[0] });

    expect(list.isWithdrawalDialogVisible()).toBe(true);
    expect(list.selectedApplicationForWithdrawal()).toBe(APPLICATIONS[0]);
  });

  it('should delete and reload the first page after confirming withdrawal', () => {
    resolvePage(0, APPLICATIONS);
    const addSpy = vi.spyOn(messageService, 'add');
    const list = accessList();
    list.onTableActions({ action: ApplicationTableAction.Delete, params: APPLICATIONS[0] });

    list.onWithdrawalDialogConfirm();

    expect(deleteApplication).toHaveBeenCalledWith(1);
    expect(list.isWithdrawalDialogVisible()).toBe(false);
    expect(list.selectedApplicationForWithdrawal()).toBeNull();
    expect(getPage).toHaveBeenCalledTimes(2);
    expect(getPage).toHaveBeenLastCalledWith({ page: 0, size: 10 });
    expect(addSpy).toHaveBeenCalledWith({
      severity: 'success',
      summary: 'Aplicació donada de baixa',
      detail: "L'aplicació s'ha donat de baixa correctament.",
    });
  });

  it('should keep the withdrawal dialog open and show an error when delete fails', () => {
    deleteApplication.mockReturnValueOnce(throwError(() => new Error('Request failed')));
    const addSpy = vi.spyOn(messageService, 'add');
    const list = accessList();
    list.onTableActions({ action: ApplicationTableAction.Delete, params: APPLICATIONS[0] });

    list.onWithdrawalDialogConfirm();

    expect(list.isWithdrawalDialogVisible()).toBe(true);
    expect(list.selectedApplicationForWithdrawal()).toBe(APPLICATIONS[0]);
    expect(getPage).toHaveBeenCalledOnce();
    expect(addSpy).toHaveBeenCalledWith({
      severity: 'error',
      summary: 'Error',
      detail: "No s'ha pogut donar de baixa l'aplicació.",
    });
  });

  function accessList(): ApplicationsListAccess {
    return component as unknown as ApplicationsListAccess;
  }

  function resolvePage(index: number, content: Application[], total = content.length): void {
    pendingPages[index].next(applicationPage(content, total));
    pendingPages[index].complete();
  }
});

function applicationPage(content: Application[], totalElements: number): SpringPage<Application> {
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
