import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormGroup } from '@angular/forms';
import { EnvironmentCatalogOption } from '@features/environments/environments.model';
import { EnvironmentCatalogService } from '@features/environments/services/environment-catalog.service';
import { SpringPage } from '@models/page.model';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';

import { ServerTypesService } from '../../services/server-types.service';
import { ServersService } from '../../services/servers.service';
import { InfrastructureServer, ServerTypeOutput } from '../../systems.model';
import { PhysicalServersList } from './physical-servers-list';

const ENVIRONMENT: EnvironmentCatalogOption = {
  id: 1,
  code: 'PRO',
  label: 'Producció',
};
const SERVER_TYPE: ServerTypeOutput = {
  id: 101,
  code: 'APPLICATION',
  name: 'Aplicació',
  nameEs: 'Aplicación',
};
const DATABASE_SERVER_TYPE: ServerTypeOutput = {
  id: 202,
  code: 'DATABASE',
  name: 'Base de dades',
  nameEs: 'Base de datos',
};
const SERVER: InfrastructureServer = {
  id: 3,
  name: 'app01.caib.es',
  environment: {
    id: ENVIRONMENT.id,
    code: ENVIRONMENT.code,
    name: 'Producció',
    nameEs: 'Producción',
    deletedAt: null,
  },
  serverType: SERVER_TYPE,
  deletedAt: null,
};

interface Harness {
  entityForm: FormGroup;
  openCreateDialog(): void;
  submitEntity(): void;
}

describe('PhysicalServersList', () => {
  let fixture: ComponentFixture<PhysicalServersList>;
  let getAll: ReturnType<typeof vi.fn>;
  let create: ReturnType<typeof vi.fn>;
  let getByCode: ReturnType<typeof vi.fn>;
  let addMessage: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    getAll = vi.fn(() => of(page([SERVER])));
    create = vi.fn(() => of(SERVER));
    getByCode = vi.fn((code: string) =>
      of(code === 'APPLICATION' ? SERVER_TYPE : DATABASE_SERVER_TYPE),
    );
    addMessage = vi.fn();

    await TestBed.configureTestingModule({
      imports: [PhysicalServersList],
      providers: [
        { provide: MessageService, useValue: { add: addMessage } },
        {
          provide: ServersService,
          useValue: {
            getAll,
            getById: vi.fn(() => of(SERVER)),
            create,
            update: vi.fn(() => of(SERVER)),
            delete: vi.fn(() => of(undefined)),
            reactivate: vi.fn(() => of(SERVER)),
          },
        },
        {
          provide: EnvironmentCatalogService,
          useValue: { getActiveOptions: vi.fn(() => of([ENVIRONMENT])) },
        },
        {
          provide: ServerTypesService,
          useValue: { getByCode },
        },
      ],
    })
      .overrideComponent(PhysicalServersList, { set: { template: '' } })
      .compileComponents();

    fixture = TestBed.createComponent(PhysicalServersList);
    fixture.componentRef.setInput('serverTypeCode', 'APPLICATION');
    fixture.detectChanges();
  });

  it('keeps the APPLICATION discriminator fixed in list requests', () => {
    expect(getAll).toHaveBeenCalledWith(
      expect.objectContaining({
        page: 0,
        size: 10,
        serverTypeCode: 'APPLICATION',
        statusId: 1,
      }),
    );
  });

  it('resolves APPLICATION from the API catalog and submits its returned identifier', () => {
    harness().openCreateDialog();
    harness().entityForm.setValue({
      name: ' app01.caib.es ',
      environment: ENVIRONMENT,
    });

    harness().submitEntity();

    expect(getByCode).toHaveBeenCalledWith('APPLICATION');
    expect(create).toHaveBeenCalledWith({
      name: SERVER.name,
      environmentId: ENVIRONMENT.id,
      serverTypeId: SERVER_TYPE.id,
    });
  });

  it('resolves DATABASE from the API catalog for the database servers panel', () => {
    fixture.componentRef.setInput('serverTypeCode', 'DATABASE');
    fixture.detectChanges();

    harness().openCreateDialog();
    harness().entityForm.setValue({
      name: ' db01.caib.es ',
      environment: ENVIRONMENT,
    });
    harness().submitEntity();

    expect(getByCode).toHaveBeenCalledWith('DATABASE');
    expect(create).toHaveBeenCalledWith({
      name: 'db01.caib.es',
      environmentId: ENVIRONMENT.id,
      serverTypeId: DATABASE_SERVER_TYPE.id,
    });
  });

  it('does not open creation when the server type catalog cannot be loaded', () => {
    getByCode.mockReturnValueOnce(
      throwError(() => new Error('Catalog unavailable')),
    );

    harness().openCreateDialog();

    expect(addMessage).toHaveBeenCalledWith(
      expect.objectContaining({ severity: 'error' }),
    );
    expect(create).not.toHaveBeenCalled();
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
