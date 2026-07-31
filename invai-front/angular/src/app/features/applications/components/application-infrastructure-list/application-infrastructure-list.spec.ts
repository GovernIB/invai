import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ActionParams, KeyLabel, PaginatedList } from '@models/table.model';
import { TableLazyLoadEvent } from 'primeng/table';
import { SearchFiltersComponent } from '@components/search-filters/search-filters.component';
import { SectionActionsComponent } from '@components/section-actions/section-actions.component';

import {
  ApplicationInfrastructureResource,
  ApplicationInfrastructureStatus,
  ApplicationServer,
} from '../../applications.model';
import {
  ApplicationInfrastructureTable,
  ApplicationInfrastructureTableAction,
} from '../application-infrastructure-table/application-infrastructure-table';
import { ApplicationInfrastructureList } from './application-infrastructure-list';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

const SERVER: ApplicationServer = {
  id: 1,
  informationSystemDbId: 70,
  systemId: 5,
  deletedAt: null,
  environment: 'Producció',
  server: 'app01.caib.es',
  instance: 'jboss',
  port: 8080,
  version: '7.4',
  status: 'Actiu',
  observations: '',
  catalogItem: {
    id: 5,
    environment: 'Producció',
    server: 'app01.caib.es',
    instance: 'jboss',
    port: 8080,
    version: '7.4',
    description: '',
    source: null!,
  },
};
const ITEMS_LIST: PaginatedList<ApplicationInfrastructureResource> = {
  items: [SERVER],
  total: 25,
};

describe('ApplicationInfrastructureList', () => {
  let component: ApplicationInfrastructureList;
  let fixture: ComponentFixture<ApplicationInfrastructureList>;

  const columns: KeyLabel[] = [
    { key: 'environment', label: 'Entorn', sortBy: 'environment' },
    { key: 'server', label: 'Servidor', sortBy: 'server' },
    { key: 'status', label: 'Estat', sortBy: 'deletedAt' },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationInfrastructureList],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationInfrastructureList);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('title', 'Servidors');
    fixture.componentRef.setInput('resourceName', 'servidor');
    fixture.componentRef.setInput('itemsList', ITEMS_LIST);
    fixture.componentRef.setInput('columns', columns);
    fixture.componentRef.setInput('appliedStatus', ApplicationInfrastructureStatus.ACTIVE);
    fixture.componentRef.setInput('isLoading', true);
    fixture.componentRef.setInput('first', 20);
    fixture.componentRef.setInput('filtersSelected', 2);
    fixture.componentRef.setInput('filtersButtonAriaLabel', 'Filtres de servidors');
    fixture.detectChanges();
  });

  it('should hide status by default while keeping it selectable', () => {
    const list = component as unknown as {
      itemsList: () => PaginatedList<ApplicationInfrastructureResource>;
      selectableColumns: () => KeyLabel[];
      visibleColumns: () => KeyLabel[];
    };

    expect(list.visibleColumns()).toEqual(columns.slice(0, 2));
    expect(list.selectableColumns()).toContainEqual(
      expect.objectContaining({ key: 'status' }),
    );
    expect(list.itemsList()).toEqual(ITEMS_LIST);
  });

  it('should forward loading and page events without changing the server page', () => {
    const pageEvents: TableLazyLoadEvent[] = [];
    const list = component as unknown as {
      itemsList: () => PaginatedList<ApplicationInfrastructureResource>;
      onPageChange: (event: TableLazyLoadEvent) => void;
    };
    const event = { first: 20, rows: 10, sortField: 'system.name', sortOrder: -1 };

    component.pageChange.subscribe((pageEvent) => pageEvents.push(pageEvent));
    list.onPageChange(event);

    const table = fixture.debugElement.query(By.directive(ApplicationInfrastructureTable));
    expect(table.componentInstance.isLoading()).toBe(true);
    expect(table.componentInstance.first()).toBe(20);
    expect(pageEvents).toEqual([event]);
    expect(list.itemsList()).toEqual(ITEMS_LIST);
  });

  it('synchronizes status only when the applied criterion changes', () => {
    const list = component as unknown as {
      selectedColumns: {
        update(updateFn: (columns: KeyLabel[]) => KeyLabel[]): void;
      };
      visibleColumns: () => KeyLabel[];
    };

    fixture.componentRef.setInput('appliedStatus', null);
    fixture.detectChanges();
    expect(list.visibleColumns().map(({ key }) => key)).toContain('status');

    list.selectedColumns.update((selected) =>
      selected.filter(({ key }) => key !== 'status'),
    );
    fixture.componentRef.setInput('appliedStatus', null);
    fixture.detectChanges();
    expect(list.visibleColumns().map(({ key }) => key)).not.toContain('status');

    fixture.componentRef.setInput(
      'appliedStatus',
      ApplicationInfrastructureStatus.INACTIVE,
    );
    fixture.detectChanges();
    expect(list.visibleColumns().map(({ key }) => key)).not.toContain('status');
  });

  it('toggles its own filter panel and forwards search and reset events', () => {
    const searches = vi.fn();
    const resets = vi.fn();
    component.filterSearch.subscribe(searches);
    component.filterReset.subscribe(resets);

    const actions = fixture.debugElement.query(By.directive(SectionActionsComponent));
    expect(actions.componentInstance.filtersSelected()).toBe(2);
    expect(actions.componentInstance.filtersButtonAriaLabel()).toBe('Filtres de servidors');
    expect(fixture.debugElement.query(By.directive(SearchFiltersComponent))).toBeNull();

    actions.componentInstance.onToggleFilters();
    fixture.detectChanges();

    const filters = fixture.debugElement.query(By.directive(SearchFiltersComponent));
    expect(filters).toBeTruthy();
    expect(filters.componentInstance.isLoading()).toBe(true);
    filters.componentInstance.search();
    filters.componentInstance.reset();

    expect(searches).toHaveBeenCalledOnce();
    expect(resets).toHaveBeenCalledOnce();
  });

  it('emits semantic add and row actions without mutating data', () => {
    const addRequested = vi.fn();
    const rowAction = vi.fn();
    const list = component as unknown as {
      itemsList: () => PaginatedList<ApplicationInfrastructureResource>;
      onAdd: () => void;
      onTableAction: (event: ActionParams<ApplicationInfrastructureResource>) => void;
    };
    component.addRequested.subscribe(addRequested);
    component.rowAction.subscribe(rowAction);

    list.onAdd();
    const action = {
      action: ApplicationInfrastructureTableAction.Edit,
      params: SERVER,
    };
    list.onTableAction(action);

    expect(addRequested).toHaveBeenCalledOnce();
    expect(rowAction).toHaveBeenCalledWith(action);
    expect(list.itemsList()).toEqual(ITEMS_LIST);
  });
});
