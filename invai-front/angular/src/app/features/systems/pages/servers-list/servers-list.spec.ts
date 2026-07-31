import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormGroup } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { of } from 'rxjs';

import { SpringPage } from '@models/page.model';
import { InfrastructureTableAction } from '../../components/infrastructure-table/infrastructure-table';
import { ServerCatalogService } from '../../services/server-catalog.service';
import { SystemsService } from '../../services/systems.service';
import {
  InfrastructureServer,
  InfrastructureSystem,
  ServerCatalogOption,
} from '../../systems.model';
import { ServersList } from './servers-list';

const SERVER: InfrastructureServer = {
  id: 3,
  name: 'app01.caib.es',
  environment: {
    id: 1,
    code: 'PRO',
    name: 'Producció',
    nameEs: 'Producción',
    deletedAt: null,
  },
  serverType: {
    id: 101,
    code: 'APPLICATION',
    name: 'Aplicació',
    nameEs: 'Aplicación',
  },
  deletedAt: null,
};
const SERVER_OPTION: ServerCatalogOption = {
  id: SERVER.id,
  name: SERVER.name,
  environment: SERVER.environment,
  label: `${SERVER.name} · Producció`,
};
const SYSTEM: InfrastructureSystem = {
  id: 7,
  server: SERVER,
  instance: 'jboss',
  port: 8080,
  version: '7.1',
  description: null,
  deletedAt: null,
};

interface Harness {
  hostForm: FormGroup;
  openCreateDialog(): void;
  onTableAction(event: {
    action: InfrastructureTableAction;
    params: Record<string, unknown>;
  }): void;
  submitEntity(): void;
}

describe('ServersList', () => {
  let fixture: ComponentFixture<ServersList>;
  let getAll: ReturnType<typeof vi.fn>;
  let create: ReturnType<typeof vi.fn>;
  let reactivate: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    getAll = vi.fn(() => of(page([SYSTEM])));
    create = vi.fn(() => of(SYSTEM));
    reactivate = vi.fn(() => of(SYSTEM));

    await TestBed.configureTestingModule({
      imports: [ServersList],
      providers: [
        MessageService,
        {
          provide: SystemsService,
          useValue: {
            getAll,
            getById: vi.fn(() => of(SYSTEM)),
            create,
            update: vi.fn(() => of(SYSTEM)),
            delete: vi.fn(() => of(undefined)),
            reactivate,
          },
        },
        {
          provide: ServerCatalogService,
          useValue: { getActiveOptions: vi.fn(() => of([SERVER_OPTION])) },
        },
      ],
    })
      .overrideComponent(ServersList, { set: { template: '' } })
      .compileComponents();

    fixture = TestBed.createComponent(ServersList);
    fixture.detectChanges();
  });

  it('loads backend System hosts with active status by default', () => {
    expect(getAll).toHaveBeenCalledWith(
      expect.objectContaining({
        page: 0,
        size: 10,
        serverId: undefined,
        instance: undefined,
        version: undefined,
        statusId: 1,
      }),
    );
  });

  it('creates a System using the selected application server name and id', () => {
    harness().openCreateDialog();
    harness().hostForm.setValue({
      server: SERVER_OPTION,
      instance: ' jboss ',
      port: 8080,
      version: ' 7.1 ',
      description: ' ',
    });

    harness().submitEntity();

    expect(create).toHaveBeenCalledWith({
      name: SERVER.name,
      serverId: SERVER.id,
      instance: 'jboss',
      port: 8080,
      version: '7.1',
      description: null,
    });
  });

  it('uses the real reactivate endpoint through the service', () => {
    harness().onTableAction({
      action: InfrastructureTableAction.Restore,
      params: { id: SYSTEM.id, deletedAt: '2026-07-29T08:00:00' },
    });

    expect(reactivate).toHaveBeenCalledWith(SYSTEM.id);
  });

  function harness(): Harness {
    return fixture.componentInstance as unknown as Harness;
  }
});

function page<T>(content: T[]): SpringPage<T> {
  return {
    content,
    empty: !content.length,
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
