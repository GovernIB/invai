import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';

import { SpringPage } from '@models/page.model';
import { ActionParams } from '@models/table.model';
import { Environment, EnvironmentStatus } from '../../environments.model';
import { EnvironmentTableAction } from '../../components/environments-table/environments-table';
import { EnvironmentsService } from '../../services/environments.service';
import { EnvironmentsList } from './environments-list';
import {
  ENVIRONMENTS_LIST_RESOLVE_KEY,
  EnvironmentsListResolvedData,
} from './environments-list.resolver';

const ENVIRONMENT: Environment = {
  id: 1,
  code: 'DEV',
  name: 'Desenvolupament',
  nameEs: 'Desarrollo',
  deletedAt: null,
};

interface EnvironmentsListHarness {
  filtersForm: FormGroup;
  entityForm: FormGroup;
  isDialogVisible: () => boolean;
  isDeleteDialogVisible: () => boolean;
  dialogMode: () => 'create' | 'view' | 'edit';
  selectedEntityCanRestore: () => boolean;
  cancelEntityEdit: () => void;
  confirmDelete: () => void;
  deactivateSelectedEntity: () => void;
  openCreateDialog: () => void;
  onFilterSearch: () => void;
  onTableAction: (event: ActionParams<Environment>) => void;
  restoreSelectedEntity: () => void;
  startEntityEdit: () => void;
  submitEntity: () => void;
}

describe('EnvironmentsList', () => {
  let fixture: ComponentFixture<EnvironmentsList>;
  let getAll: ReturnType<typeof vi.fn>;
  let getById: ReturnType<typeof vi.fn>;
  let create: ReturnType<typeof vi.fn>;
  let update: ReturnType<typeof vi.fn>;
  let deleteEnvironment: ReturnType<typeof vi.fn>;
  let reactivate: ReturnType<typeof vi.fn>;
  let routeData: EnvironmentsListResolvedData;

  beforeEach(async () => {
    getAll = vi.fn(() => of(page([ENVIRONMENT])));
    getById = vi.fn(() => of(ENVIRONMENT));
    create = vi.fn(() => of(ENVIRONMENT));
    update = vi.fn(() => of(ENVIRONMENT));
    deleteEnvironment = vi.fn(() => of(undefined));
    reactivate = vi.fn(() => of(ENVIRONMENT));
    routeData = { page: page([ENVIRONMENT]), pageLoadFailed: false };

    await TestBed.configureTestingModule({
      imports: [EnvironmentsList],
      providers: [
        MessageService,
        {
          provide: EnvironmentsService,
          useValue: {
            getAll,
            getById,
            create,
            update,
            delete: deleteEnvironment,
            reactivate,
          },
        },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              data: { [ENVIRONMENTS_LIST_RESOLVE_KEY]: routeData },
            },
          },
        },
      ],
    })
      .overrideComponent(EnvironmentsList, { set: { template: '' } })
      .compileComponents();

    fixture = TestBed.createComponent(EnvironmentsList);
    fixture.detectChanges();
  });

  it('consumes the resolved active page without requesting it again', () => {
    expect(getAll).not.toHaveBeenCalled();
    expect(fixture.componentInstance.itemsList()).toEqual({
      items: [ENVIRONMENT],
      total: 1,
    });
    expect(fixture.componentInstance.selectedFilters()).toBe(1);
  });

  it('sends field filters and the mandatory status in later searches', () => {
    harness().filtersForm.setValue({
      code: ' DEV ',
      name: ' Desenvolupament ',
      nameEs: ' Desarrollo ',
      status: EnvironmentStatus.INACTIVE,
    });

    harness().onFilterSearch();

    expect(getAll).toHaveBeenCalledWith({
      page: 0,
      size: 10,
      sort: undefined,
      code: 'DEV',
      name: 'Desenvolupament',
      nameEs: 'Desarrollo',
      statusId: EnvironmentStatus.INACTIVE,
      search: undefined,
    });
  });

  it('omits statusId when all statuses are applied', () => {
    harness().filtersForm.controls['status'].setValue(null);

    harness().onFilterSearch();

    expect(getAll).toHaveBeenCalledWith(
      expect.objectContaining({ statusId: undefined }),
    );
    expect(fixture.componentInstance.selectedFilters()).toBe(0);
  });

  it('restores an inactive environment directly from its table action', () => {
    const add = vi.spyOn(TestBed.inject(MessageService), 'add');
    const inactiveEnvironment = { ...ENVIRONMENT, deletedAt: '2026-07-22T08:00:00' };

    harness().onTableAction({
      action: EnvironmentTableAction.Restore,
      params: inactiveEnvironment,
    });

    expect(getById).not.toHaveBeenCalled();
    expect(reactivate).toHaveBeenCalledWith(inactiveEnvironment.id);
    expect(add).toHaveBeenCalledWith(
      expect.objectContaining({ severity: 'success', detail: expect.stringContaining('restaurat') }),
    );
  });

  it('marks every entity field as touched and does not submit an invalid form', () => {
    harness().openCreateDialog();
    harness().submitEntity();

    expect(harness().entityForm.touched).toBe(true);
    expect(create).not.toHaveBeenCalled();
  });

  it('trims and creates a valid environment before resetting to active', () => {
    harness().openCreateDialog();
    harness().entityForm.setValue({
      code: ' DEV ',
      name: ' Desenvolupament ',
      nameEs: ' Desarrollo ',
    });

    harness().submitEntity();

    expect(create).toHaveBeenCalledWith({
      code: 'DEV',
      name: 'Desenvolupament',
      nameEs: 'Desarrollo',
    });
    expect(harness().filtersForm.getRawValue()).toEqual({
      code: null,
      name: null,
      nameEs: null,
      status: EnvironmentStatus.ACTIVE,
    });
  });

  it('loads an existing environment in read-only view mode', () => {
    harness().onTableAction({
      action: EnvironmentTableAction.View,
      params: ENVIRONMENT,
    });

    expect(getById).toHaveBeenCalledWith(ENVIRONMENT.id);
    expect(harness().entityForm.getRawValue()).toEqual({
      code: ENVIRONMENT.code,
      name: ENVIRONMENT.name,
      nameEs: ENVIRONMENT.nameEs,
    });
    expect(harness().entityForm.disabled).toBe(true);
  });

  it('restores an inactive environment from its read-only view', () => {
    const add = vi.spyOn(TestBed.inject(MessageService), 'add');
    const inactiveEnvironment = { ...ENVIRONMENT, deletedAt: '2026-07-22T08:00:00' };
    getById.mockReturnValue(of(inactiveEnvironment));

    harness().onTableAction({
      action: EnvironmentTableAction.View,
      params: inactiveEnvironment,
    });

    expect(harness().isDialogVisible()).toBe(true);
    expect(harness().selectedEntityCanRestore()).toBe(true);
    expect(harness().entityForm.disabled).toBe(true);

    harness().restoreSelectedEntity();

    expect(reactivate).toHaveBeenCalledWith(inactiveEnvironment.id);
    expect(add).toHaveBeenCalledWith(
      expect.objectContaining({ severity: 'success', detail: expect.stringContaining('restaurat') }),
    );
    expect(harness().isDialogVisible()).toBe(false);
    expect(harness().selectedEntityCanRestore()).toBe(false);
  });

  it('loads, trims and updates an existing environment', () => {
    harness().onTableAction({
      action: EnvironmentTableAction.Edit,
      params: ENVIRONMENT,
    });
    harness().entityForm.setValue({
      code: ' PRE ',
      name: ' Preproducció ',
      nameEs: ' Preproducción ',
    });

    harness().submitEntity();

    expect(getById).toHaveBeenCalledWith(ENVIRONMENT.id);
    expect(update).toHaveBeenCalledWith(ENVIRONMENT.id, {
      code: 'PRE',
      name: 'Preproducció',
      nameEs: 'Preproducción',
    });
  });

  it('edits from consultation and restores the loaded snapshot on cancel', () => {
    harness().onTableAction({
      action: EnvironmentTableAction.View,
      params: ENVIRONMENT,
    });

    harness().startEntityEdit();
    expect(harness().dialogMode()).toBe('edit');
    expect(harness().entityForm.enabled).toBe(true);

    harness().entityForm.patchValue({ code: 'PRE', name: 'Preproducció' });
    harness().cancelEntityEdit();

    expect(harness().dialogMode()).toBe('view');
    expect(harness().entityForm.disabled).toBe(true);
    expect(harness().entityForm.getRawValue()).toEqual({
      code: ENVIRONMENT.code,
      name: ENVIRONMENT.name,
      nameEs: ENVIRONMENT.nameEs,
    });
    expect(harness().entityForm.pristine).toBe(true);
  });

  it('opens deactivation from editing and closes the entity dialog only after success', () => {
    harness().onTableAction({
      action: EnvironmentTableAction.View,
      params: ENVIRONMENT,
    });
    harness().startEntityEdit();
    harness().deactivateSelectedEntity();

    expect(harness().isDeleteDialogVisible()).toBe(true);
    expect(harness().isDialogVisible()).toBe(true);

    harness().confirmDelete();

    expect(deleteEnvironment).toHaveBeenCalledWith(ENVIRONMENT.id);
    expect(harness().isDialogVisible()).toBe(false);
  });

  it('keeps the edited dialog open when persistence fails', () => {
    update.mockReturnValue(throwError(() => new Error('Save failed')));
    harness().onTableAction({
      action: EnvironmentTableAction.Edit,
      params: ENVIRONMENT,
    });
    harness().entityForm.patchValue({ code: 'PRE' });

    harness().submitEntity();

    expect(harness().isDialogVisible()).toBe(true);
    expect(harness().dialogMode()).toBe('edit');
  });

  it('deletes the selected active environment and resets the list', () => {
    harness().onTableAction({
      action: EnvironmentTableAction.Delete,
      params: ENVIRONMENT,
    });

    harness().confirmDelete();

    expect(deleteEnvironment).toHaveBeenCalledWith(ENVIRONMENT.id);
    expect(getAll).toHaveBeenCalledWith({
      page: 0,
      size: 10,
      sort: undefined,
      code: undefined,
      name: undefined,
      nameEs: undefined,
      statusId: EnvironmentStatus.ACTIVE,
      search: undefined,
    });
  });

  function harness(): EnvironmentsListHarness {
    return fixture.componentInstance as unknown as EnvironmentsListHarness;
  }
});

function page<TItem>(content: TItem[]): SpringPage<TItem> {
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
