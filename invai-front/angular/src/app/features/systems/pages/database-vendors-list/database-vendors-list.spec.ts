import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormGroup } from '@angular/forms';
import { SpringPage } from '@models/page.model';
import { MessageService } from 'primeng/api';
import { of } from 'rxjs';

import { InfrastructureTableAction } from '../../components/infrastructure-table/infrastructure-table';
import { DatabaseVendorsService } from '../../services/database-vendors.service';
import { DatabaseVendor, InfrastructureDialogMode } from '../../systems.model';
import { DatabaseVendorsList } from './database-vendors-list';

const VENDOR: DatabaseVendor = {
  id: 2,
  name: 'PostgreSQL',
  defaultPort: 5432,
  deletedAt: null,
};

interface Harness {
  cancelEntityEdit(): void;
  dialogMode(): InfrastructureDialogMode;
  entityForm: FormGroup;
  openCreateDialog(): void;
  onTableAction(event: {
    action: InfrastructureTableAction;
    params: Record<string, unknown>;
  }): void;
  startEntityEdit(): void;
  submitEntity(): void;
}

describe('DatabaseVendorsList', () => {
  let fixture: ComponentFixture<DatabaseVendorsList>;
  let getAll: ReturnType<typeof vi.fn>;
  let create: ReturnType<typeof vi.fn>;
  let reactivate: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    getAll = vi.fn(() => of(page([VENDOR])));
    create = vi.fn(() => of(VENDOR));
    reactivate = vi.fn(() => of(VENDOR));

    await TestBed.configureTestingModule({
      imports: [DatabaseVendorsList],
      providers: [
        MessageService,
        {
          provide: DatabaseVendorsService,
          useValue: {
            getAll,
            getById: vi.fn(() => of(VENDOR)),
            create,
            update: vi.fn(() => of(VENDOR)),
            delete: vi.fn(() => of(undefined)),
            reactivate,
          },
        },
      ],
    })
      .overrideComponent(DatabaseVendorsList, { set: { template: '' } })
      .compileComponents();

    fixture = TestBed.createComponent(DatabaseVendorsList);
    fixture.detectChanges();
  });

  it('loads active vendors by default', () => {
    expect(getAll).toHaveBeenCalledWith(
      expect.objectContaining({ page: 0, size: 10, statusId: 1 }),
    );
  });

  it('creates a trimmed vendor payload', () => {
    harness().openCreateDialog();
    harness().entityForm.setValue({
      name: ' PostgreSQL ',
      defaultPort: 5432,
    });

    harness().submitEntity();

    expect(create).toHaveBeenCalledWith({
      name: 'PostgreSQL',
      defaultPort: 5432,
    });
  });

  it('restores an inactive vendor through the backend service', () => {
    harness().onTableAction({
      action: InfrastructureTableAction.Restore,
      params: { id: VENDOR.id, deletedAt: '2026-07-29T08:00:00' },
    });

    expect(reactivate).toHaveBeenCalledWith(VENDOR.id);
  });

  it('restores the stable vendor snapshot when edit is cancelled', () => {
    const component = harness();
    component.onTableAction({
      action: InfrastructureTableAction.View,
      params: { id: VENDOR.id, deletedAt: null },
    });
    component.startEntityEdit();
    component.entityForm.setValue({ name: 'Edited vendor', defaultPort: 15499 });
    component.entityForm.markAsDirty();
    component.entityForm.markAllAsTouched();

    component.cancelEntityEdit();

    expect(component.dialogMode()).toBe('view');
    expect(component.entityForm.getRawValue()).toEqual({
      name: VENDOR.name,
      defaultPort: VENDOR.defaultPort,
    });
    expect(component.entityForm.enabled).toBe(true);
    expect(component.entityForm.pristine).toBe(true);
    expect(component.entityForm.untouched).toBe(true);
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
