import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActionParams, KeyLabel, PaginatedList } from '@models/table.model';
import { MessageService } from 'primeng/api';
import { TableLazyLoadEvent } from 'primeng/table';

import { APPLICATION_SERVERS_SEED_DATA } from '../../applications.constants';
import { ApplicationInfrastructureResource } from '../../applications.model';
import { ApplicationInfrastructureTableAction } from '../application-infrastructure-table/application-infrastructure-table';
import { ApplicationInfrastructureList } from './application-infrastructure-list';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

describe('ApplicationInfrastructureList', () => {
  let component: ApplicationInfrastructureList;
  let fixture: ComponentFixture<ApplicationInfrastructureList>;
  let messageService: MessageService;

  const columns: KeyLabel[] = [
    { key: 'environment', label: 'Entorn', sortBy: 'environment' },
    { key: 'server', label: 'Servidor', sortBy: 'server' },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationInfrastructureList],
      providers: [MessageService],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationInfrastructureList);
    component = fixture.componentInstance;
    messageService = TestBed.inject(MessageService);
    fixture.componentRef.setInput('title', 'Servidors');
    fixture.componentRef.setInput('resourceName', 'servidor');
    fixture.componentRef.setInput('sourceItems', APPLICATION_SERVERS_SEED_DATA);
    fixture.componentRef.setInput('columns', columns);
    fixture.detectChanges();
  });

  it('should initialize all columns and records', () => {
    const list = component as unknown as {
      itemsList: () => PaginatedList<ApplicationInfrastructureResource>;
      visibleColumns: () => KeyLabel[];
    };

    expect(list.visibleColumns()).toEqual(columns);
    expect(list.itemsList().total).toBe(APPLICATION_SERVERS_SEED_DATA.length);
  });

  it('should sort and paginate records from table events', () => {
    const list = component as unknown as {
      itemsList: () => PaginatedList<ApplicationInfrastructureResource>;
      onPageChange: (event: TableLazyLoadEvent) => void;
    };

    list.onPageChange({ first: 0, rows: 1, sortField: 'server', sortOrder: -1 });

    expect(list.itemsList().total).toBe(3);
    expect(list.itemsList().items).toHaveLength(1);
    expect(list.itemsList().items[0]?.id).toBe('server-3');
  });

  it('should show pending messages for add and row actions without mutating data', () => {
    const addSpy = vi.spyOn(messageService, 'add');
    const list = component as unknown as {
      itemsList: () => PaginatedList<ApplicationInfrastructureResource>;
      onAdd: () => void;
      onTableAction: (event: ActionParams<ApplicationInfrastructureResource>) => void;
    };

    list.onAdd();
    list.onTableAction({
      action: ApplicationInfrastructureTableAction.Edit,
      params: APPLICATION_SERVERS_SEED_DATA[0],
    });

    expect(addSpy).toHaveBeenNthCalledWith(1, {
      severity: 'info',
      summary: 'Informació',
      detail: "L'alta de servidor encara no està implementada.",
    });
    expect(addSpy).toHaveBeenNthCalledWith(2, {
      severity: 'info',
      summary: 'Informació',
      detail: "L'edició de servidor encara no està implementada.",
    });
    expect(list.itemsList().total).toBe(APPLICATION_SERVERS_SEED_DATA.length);
  });
});
