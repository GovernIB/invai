import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { MessageService } from 'primeng/api';
import { Table } from 'primeng/table';
import { of, Subject, throwError } from 'rxjs';
import { AccessibilityResource } from '../../accessibility.model';
import {
  AccessibilityResourceFormGroup,
  AccessibilityResourceFiltersFormGroup,
} from '../../forms/accessibility-resource-forms.factory';
import {
  ClassificationSegmentsService,
  ComplianceSituationsService,
} from '../../services/accessibility-resource.services';
import { AccessibilityResourceTableAction as Action } from '../../components/accessibility-resource-table/accessibility-resource-table';
import { AccessibilityResourceList } from './accessibility-resource-list';

interface ListActions {
  entityForm: AccessibilityResourceFormGroup;
  filtersForm: AccessibilityResourceFiltersFormGroup;
  dialogMode(): string;
  isDialogVisible(): boolean;
  openCreateDialog(): void;
  startEntityEdit(): void;
  cancelEntityEdit(): void;
  submitEntity(): void;
  onFilterSearch(): void;
  onTableAction(event: { action: number; params: AccessibilityResource }): void;
  confirmDelete(): void;
}

describe.each([
  ['classification-segment', ClassificationSegmentsService],
  ['compliance-situation', ComplianceSituationsService],
] as const)('%s maintenance flow', (resource, serviceType) => {
  let fixture: ComponentFixture<AccessibilityResourceList>;
  let actions: ListActions;
  const row: AccessibilityResource = {
    id: 17,
    name: 'Nom original',
    nameEs: 'Nombre original',
    deletedAt: null,
  };
  const page = { content: [row], totalElements: 1 };
  const service = {
    getAll: vi.fn(),
    getById: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    deactivate: vi.fn(),
    reactivate: vi.fn(),
  };
  const other = { getAll: vi.fn() };
  const add = vi.fn();

  beforeEach(async () => {
    Object.values(service).forEach((mock) => mock.mockReset());
    other.getAll.mockClear();
    add.mockClear();
    service.getAll.mockReturnValue(of(page));
    service.getById.mockReturnValue(of(row));
    service.create.mockReturnValue(of(row));
    service.update.mockReturnValue(of(row));
    service.deactivate.mockReturnValue(of(null));
    service.reactivate.mockReturnValue(of(row));
    await TestBed.configureTestingModule({
      imports: [AccessibilityResourceList],
      providers: [
        { provide: MessageService, useValue: { add } },
        {
          provide: ClassificationSegmentsService,
          useValue: serviceType === ClassificationSegmentsService ? service : other,
        },
        {
          provide: ComplianceSituationsService,
          useValue: serviceType === ComplianceSituationsService ? service : other,
        },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(AccessibilityResourceList);
    fixture.componentRef.setInput('resource', resource);
    fixture.componentRef.setInput('initialPage', page);
    fixture.detectChanges();
    actions = fixture.componentInstance as unknown as ListActions;
  });

  it('consumes its resolved page and renders a striped consultable table', () => {
    expect(service.getAll).not.toHaveBeenCalled();
    expect(other.getAll).not.toHaveBeenCalled();
    expect(fixture.debugElement.query(By.directive(Table)).componentInstance.stripedRows).toBe(
      true,
    );
    const element = fixture.nativeElement.querySelector('tbody tr[tabindex]') as HTMLElement;
    expect(element.textContent).toContain('17');
    element.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    fixture.detectChanges();
    expect(service.getById).toHaveBeenCalledExactlyOnceWith(17);
    expect(actions.dialogMode()).toBe('view');
    expect(
      (fixture.nativeElement.querySelector('input[formControlName="name"]') as HTMLInputElement)
        .readOnly,
    ).toBe(true);
  });

  it('restores the original snapshot when cancelling an edit', () => {
    actions.onTableAction({ action: Action.View, params: row });
    actions.startEntityEdit();
    actions.entityForm.controls.name.setValue('Canvi');
    actions.entityForm.markAsDirty();
    actions.cancelEntityEdit();
    expect(actions.dialogMode()).toBe('view');
    expect(actions.entityForm.getRawValue()).toEqual({ name: row.name, nameEs: row.nameEs });
    expect(service.update).not.toHaveBeenCalled();
  });

  it('validates, trims both names and refreshes only its own resource on create', () => {
    actions.openCreateDialog();
    actions.submitEntity();
    expect(service.create).not.toHaveBeenCalled();
    expect(actions.entityForm.controls.name.touched).toBe(true);
    actions.entityForm.setValue({ name: '  Nou  ', nameEs: '  Nuevo  ' });
    actions.submitEntity();
    expect(service.create).toHaveBeenCalledExactlyOnceWith({ name: 'Nou', nameEs: 'Nuevo' });
    expect(actions.isDialogVisible()).toBe(false);
    expect(service.getAll).toHaveBeenCalledOnce();
    expect(other.getAll).not.toHaveBeenCalled();
  });

  it('keeps an edit open on a denied save and shows one permission toast', () => {
    service.update.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 403 })));
    actions.onTableAction({ action: Action.Edit, params: row });
    actions.entityForm.controls.name.setValue('Canvi');
    actions.entityForm.markAsDirty();
    actions.submitEntity();
    expect(actions.isDialogVisible()).toBe(true);
    expect(actions.entityForm.controls.name.value).toBe('Canvi');
    expect(add).toHaveBeenCalledExactlyOnceWith(
      expect.objectContaining({
        severity: 'error',
        detail: 'No tens permisos per fer aquesta operació.',
      }),
    );
  });

  it('preserves rows during and after a failed refresh, then allows retry', () => {
    const pending = new Subject<unknown>();
    service.getAll.mockReturnValue(pending);
    fixture.componentInstance.onSearch();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[aria-busy="true"]')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.p-datatable-mask')).toBeNull();
    expect(fixture.componentInstance.itemsList().items).toEqual([row]);
    pending.error(new HttpErrorResponse({ status: 403 }));
    expect(fixture.componentInstance.itemsList().items).toEqual([row]);
    expect(add).toHaveBeenCalledExactlyOnceWith(
      expect.objectContaining({ detail: 'No tens permisos per fer aquesta operació.' }),
    );
    service.getAll.mockReturnValue(of(page));
    fixture.componentInstance.onSearch();
    expect(service.getAll).toHaveBeenCalledTimes(2);
  });

  it('applies all-status criteria and synchronizes the status column only on search', () => {
    actions.filtersForm.controls.status.setValue(null);
    expect(
      fixture.componentInstance.selectedColumns().some((column) => column.key === 'status'),
    ).toBe(false);
    actions.onFilterSearch();
    expect(service.getAll).toHaveBeenLastCalledWith(
      expect.objectContaining({ statusId: undefined }),
    );
    expect(
      fixture.componentInstance.selectedColumns().some((column) => column.key === 'status'),
    ).toBe(true);
    fixture.componentInstance.reset();
    expect(service.getAll).toHaveBeenLastCalledWith(expect.objectContaining({ statusId: 1 }));
    expect(
      fixture.componentInstance.selectedColumns().some((column) => column.key === 'status'),
    ).toBe(false);
  });

  it('confirms deactivation and restores inactive records through their endpoint', () => {
    actions.onTableAction({ action: Action.Deactivate, params: row });
    expect(service.deactivate).not.toHaveBeenCalled();
    actions.confirmDelete();
    expect(service.deactivate).toHaveBeenCalledExactlyOnceWith(17);
    actions.onTableAction({
      action: Action.Restore,
      params: { ...row, deletedAt: '2026-09-09T10:00:00' },
    });
    expect(service.reactivate).toHaveBeenCalledExactlyOnceWith(17);
  });
});
