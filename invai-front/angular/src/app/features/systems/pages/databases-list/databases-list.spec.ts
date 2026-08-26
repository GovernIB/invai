import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormGroup } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { of } from 'rxjs';

import { SpringPage } from '@models/page.model';
import { InfrastructureTableAction } from '../../components/infrastructure-table/infrastructure-table';
import { DatabaseVendorCatalogService } from '../../services/database-vendor-catalog.service';
import { DatabasesService } from '../../services/databases.service';
import { ServerCatalogService } from '../../services/server-catalog.service';
import {
  DatabaseRecord,
  DatabaseVendor,
  DatabaseVendorCatalogOption,
  InfrastructureServer,
  ServerCatalogOption,
} from '../../systems.model';
import { DatabasesList } from './databases-list';

const SERVER: InfrastructureServer = {
  id: 4,
  name: 'db01.caib.es',
  environment: {
    id: 1,
    code: 'PRO',
    name: 'Producció',
    nameEs: 'Producción',
    deletedAt: null,
  },
  serverType: {
    id: 202,
    code: 'DATABASE',
    name: 'Base de dades',
    nameEs: 'Base de datos',
  },
  deletedAt: null,
};
const SERVER_OPTION: ServerCatalogOption = {
  id: SERVER.id,
  name: SERVER.name,
  environment: SERVER.environment,
  label: `${SERVER.name} · Producció`,
};
const VENDOR: DatabaseVendor = {
  id: 2,
  name: 'PostgreSQL',
  defaultPort: 5432,
  deletedAt: null,
};
const VENDOR_OPTION: DatabaseVendorCatalogOption = {
  id: VENDOR.id,
  name: VENDOR.name,
  defaultPort: VENDOR.defaultPort,
};
const DATABASE: DatabaseRecord = {
  id: 9,
  server: SERVER,
  service: 'INVAI',
  port: 5432,
  databaseType: VENDOR,
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

describe('DatabasesList', () => {
  let fixture: ComponentFixture<DatabasesList>;
  let getAll: ReturnType<typeof vi.fn>;
  let create: ReturnType<typeof vi.fn>;
  let reactivate: ReturnType<typeof vi.fn>;
  let getServerOptions: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    getAll = vi.fn(() => of(page([DATABASE])));
    create = vi.fn(() => of(DATABASE));
    reactivate = vi.fn(() => of(DATABASE));
    getServerOptions = vi.fn(() => of([SERVER_OPTION]));

    await TestBed.configureTestingModule({
      imports: [DatabasesList],
      providers: [
        MessageService,
        {
          provide: DatabasesService,
          useValue: {
            getAll,
            getById: vi.fn(() => of(DATABASE)),
            create,
            update: vi.fn(() => of(DATABASE)),
            delete: vi.fn(() => of(undefined)),
            reactivate,
          },
        },
        {
          provide: ServerCatalogService,
          useValue: { getActiveOptions: getServerOptions },
        },
        {
          provide: DatabaseVendorCatalogService,
          useValue: { getActiveOptions: vi.fn(() => of([VENDOR_OPTION])) },
        },
      ],
    })
      .overrideComponent(DatabasesList, { set: { template: '' } })
      .compileComponents();

    fixture = TestBed.createComponent(DatabasesList);
    fixture.detectChanges();
  });

  it('loads only DATABASE servers for the relation catalog', () => {
    expect(getServerOptions).toHaveBeenCalledWith('DATABASE');
    expect(getAll).toHaveBeenCalledWith(
      expect.objectContaining({ page: 0, size: 10, statusId: 1 }),
    );
  });

  it('creates a Database with backend relation identifiers', () => {
    harness().openCreateDialog();
    harness().hostForm.setValue({
      server: SERVER_OPTION,
      service: ' INVAI ',
      port: 6432,
      databaseType: VENDOR_OPTION,
      description: ' ',
    });

    harness().submitEntity();

    expect(create).toHaveBeenCalledWith({
      serverId: SERVER.id,
      service: 'INVAI',
      port: 6432,
      databaseTypeId: VENDOR.id,
      description: null,
    });
  });

  it('restores an inactive Database through the backend service', () => {
    harness().onTableAction({
      action: InfrastructureTableAction.Restore,
      params: { id: DATABASE.id, deletedAt: '2026-07-29T08:00:00' },
    });

    expect(reactivate).toHaveBeenCalledWith(DATABASE.id);
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
