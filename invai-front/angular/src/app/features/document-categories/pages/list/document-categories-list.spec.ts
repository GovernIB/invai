import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { ActionParams } from '@models/table.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';

import { DocumentCategoryTableAction } from '../../components/document-categories-table/document-categories-table';
import { DocumentCategory } from '../../document-categories.model';
import { documentCategoriesPage, documentCategory } from '../../document-categories.testing';
import { DocumentCategoryFiltersFormGroup } from '../../forms/document-category-filters-form.factory';
import { DocumentCategoryFormGroup } from '../../forms/document-category-form.factory';
import { DocumentCategoriesService } from '../../services/document-categories.service';
import { DocumentCategoryCatalogService } from '../../services/document-category-catalog.service';
import { DocumentCategoriesList } from './document-categories-list';
import { DOCUMENT_CATEGORIES_LIST_RESOLVE_KEY } from './document-categories-list.resolver';

const MANUALS = documentCategory({ id: 11, name: 'Manuals', nameEs: 'Manuales', parentId: 1 });
const TECHNICAL = documentCategory({ id: 1, subcategories: [MANUALS] });
const AUDITS = documentCategory({
  id: 21,
  name: 'Auditories',
  nameEs: 'Auditorías',
  parentId: 2,
  deletedAt: '2026-10-01T10:00:00',
});
const SECURITY = documentCategory({
  id: 2,
  name: 'Seguretat',
  nameEs: 'Seguridad',
  subcategories: [AUDITS],
  deletedAt: '2026-10-02T10:00:00',
});
const RESOLVED_PAGE = documentCategoriesPage([TECHNICAL, SECURITY]);

interface ListInternals {
  filtersForm: DocumentCategoryFiltersFormGroup;
  entityForm: DocumentCategoryFormGroup;
  hasTextSearch: () => boolean;
  dialogKind: () => string;
  dialogMode: () => string;
  dialogCategoryLabel: () => string;
  categoryOptions: () => { id: number; label: string }[];
  isDialogVisible: () => boolean;
  selectedEntityCanEdit: () => boolean;
  selectedEntityCanRestore: () => boolean;
  tableFirst: () => number;
  onFilterSearch(): void;
  openCreateDialog(): void;
  onTableAction(event: ActionParams<DocumentCategory>): void;
  submitEntity(): void;
  confirmDelete(): void;
}

describe('DocumentCategoriesList', () => {
  let fixture: ComponentFixture<DocumentCategoriesList>;
  let list: ListInternals;
  let add: ReturnType<typeof vi.spyOn>;
  const service = {
    getAll: vi.fn(),
    getById: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
    reactivate: vi.fn(),
  };
  const getActiveCategoryOptions = vi.fn();

  beforeEach(async () => {
    for (const mock of Object.values(service)) mock.mockReset();
    service.getAll.mockReturnValue(of(RESOLVED_PAGE));
    service.create.mockImplementation((input) => of(documentCategory({ id: 30, ...input })));
    service.update.mockImplementation((id, input) => of(documentCategory({ id, ...input })));
    service.delete.mockReturnValue(of(undefined));
    service.reactivate.mockImplementation((id) => of(documentCategory({ id })));
    getActiveCategoryOptions.mockReset().mockReturnValue(of([{ id: 3, label: 'Normativa' }]));

    await TestBed.configureTestingModule({
      imports: [DocumentCategoriesList],
      providers: [
        MessageService,
        { provide: DocumentCategoriesService, useValue: service },
        { provide: DocumentCategoryCatalogService, useValue: { getActiveCategoryOptions } },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              data: {
                [DOCUMENT_CATEGORIES_LIST_RESOLVE_KEY]: { page: RESOLVED_PAGE, pageLoadFailed: false },
              },
            },
          },
        },
      ],
    })
      .overrideComponent(DocumentCategoriesList, { set: { template: '' } })
      .compileComponents();

    fixture = TestBed.createComponent(DocumentCategoriesList);
    add = vi.spyOn(TestBed.inject(MessageService), 'add');
    fixture.detectChanges();
    list = fixture.componentInstance as unknown as ListInternals;
  });

  const act = (action: DocumentCategoryTableAction, params: DocumentCategory) =>
    list.onTableAction({ action, params });

  it('shows the resolved categories without requesting them again', () => {
    expect(service.getAll).not.toHaveBeenCalled();
    expect(fixture.componentInstance.itemsList()).toEqual({ items: [TECHNICAL, SECURITY], total: 2 });
    expect(fixture.componentInstance.selectedFilters()).toBe(1);
    expect(list.hasTextSearch()).toBe(false);
  });

  it('expands the categories found while a name is searched', () => {
    list.filtersForm.patchValue({ name: ' man ', status: null });
    list.onFilterSearch();

    expect(service.getAll).toHaveBeenCalledWith({ page: 0, size: 10, name: 'man' });
    expect(list.hasTextSearch()).toBe(true);

    list.filtersForm.reset();
    list.onFilterSearch();
    expect(list.hasTextSearch()).toBe(false);
  });

  it('creates a category without a parent and reads the same page again', () => {
    list.openCreateDialog();
    expect([list.dialogKind(), list.dialogMode()]).toEqual(['category', 'create']);
    list.entityForm.patchValue({ name: ' Protecció de dades ', nameEs: 'Protección de datos' });

    list.submitEntity();

    expect(service.create).toHaveBeenCalledWith({
      name: 'Protecció de dades',
      nameEs: 'Protección de datos',
      parentId: null,
    });
    expect(add).toHaveBeenCalledWith(expect.objectContaining({ severity: 'success', summary: 'Categoria afegida' }));
    expect(list.isDialogVisible()).toBe(false);
    expect(service.getAll).toHaveBeenCalledWith({
      page: 0,
      size: 10,
      statusId: SoftDeleteStatus.ACTIVE,
    });
  });

  it('does not submit invalid names', () => {
    list.openCreateDialog();
    list.submitEntity();

    expect(service.create).not.toHaveBeenCalled();
    expect(list.entityForm.controls.name.touched).toBe(true);
  });

  it('moves an edited subcategory to another active category', () => {
    service.getById.mockReturnValue(of(MANUALS));

    act(DocumentCategoryTableAction.Edit, MANUALS);

    expect(getActiveCategoryOptions).toHaveBeenCalled();
    expect(list.categoryOptions()).toEqual([
      { id: 1, label: 'Tècnica' },
      { id: 3, label: 'Normativa' },
    ]);
    expect(list.entityForm.getRawValue()).toEqual({ name: 'Manuals', nameEs: 'Manuales', parentId: 1 });

    list.entityForm.controls.parentId.setValue(3);
    list.submitEntity();

    expect(service.update).toHaveBeenCalledWith(11, { name: 'Manuals', nameEs: 'Manuales', parentId: 3 });
  });

  it('keeps the current category when the active categories cannot be read', () => {
    service.getById.mockReturnValue(of(MANUALS));
    getActiveCategoryOptions.mockReturnValue(throwError(() => new Error('offline')));

    act(DocumentCategoryTableAction.View, MANUALS);

    expect(list.categoryOptions()).toEqual([{ id: 1, label: 'Tècnica' }]);
    expect(add).toHaveBeenCalledWith(expect.objectContaining({ severity: 'error' }));
  });

  it('consults and edits a category in its detail page and a subcategory in the dialog', () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    service.getById.mockReturnValue(of(MANUALS));

    act(DocumentCategoryTableAction.View, TECHNICAL);
    act(DocumentCategoryTableAction.Edit, SECURITY);

    expect(navigate.mock.calls).toEqual([
      [['/', 'manteniments', 'documentacio', 'categories-documentals', 1], undefined],
      [
        ['/', 'manteniments', 'documentacio', 'categories-documentals', 2],
        { info: { documentCategoryMode: 'edit' } },
      ],
    ]);
    expect(list.isDialogVisible()).toBe(false);
    expect(service.getById).not.toHaveBeenCalled();

    act(DocumentCategoryTableAction.Edit, MANUALS);
    expect([list.dialogKind(), list.dialogMode(), list.isDialogVisible()]).toEqual([
      'subcategory',
      'edit',
      true,
    ]);
    expect(navigate).toHaveBeenCalledTimes(2);
  });

  it('only offers to restore a subcategory whose category is active', () => {
    const api = documentCategory({ id: 12, name: 'API', parentId: 1, deletedAt: '2026-10-01T10:00:00' });
    service.getById.mockReturnValueOnce(of(api)).mockReturnValueOnce(of(AUDITS));

    act(DocumentCategoryTableAction.View, api);
    expect([list.selectedEntityCanEdit(), list.selectedEntityCanRestore()]).toEqual([false, true]);
    expect(getActiveCategoryOptions).not.toHaveBeenCalled();

    act(DocumentCategoryTableAction.View, AUDITS);
    expect([list.selectedEntityCanEdit(), list.selectedEntityCanRestore()]).toEqual([false, false]);
  });

  it('deletes and restores records and reads the page again after each change', () => {
    act(DocumentCategoryTableAction.Delete, MANUALS);
    list.confirmDelete();
    act(DocumentCategoryTableAction.Restore, SECURITY);

    expect(service.delete).toHaveBeenCalledWith(11);
    expect(service.reactivate).toHaveBeenCalledWith(2);
    expect(add).toHaveBeenCalledWith(
      expect.objectContaining({ summary: 'Subcategoria eliminada' }),
    );
    expect(add).toHaveBeenCalledWith(expect.objectContaining({ summary: 'Categoria restaurada' }));
    expect(service.getAll).toHaveBeenCalledTimes(2);
  });

  it('leaves validation errors to the server error dialog and reports any other failure', () => {
    service.delete
      .mockReturnValueOnce(
        throwError(
          () => new HttpErrorResponse({ status: 400, error: { error: 'Error', message: 'Té subcategories actives' } }),
        ),
      )
      .mockReturnValueOnce(throwError(() => new HttpErrorResponse({ status: 500 })));

    act(DocumentCategoryTableAction.Delete, TECHNICAL);
    list.confirmDelete();
    expect(add).not.toHaveBeenCalled();

    act(DocumentCategoryTableAction.Delete, TECHNICAL);
    list.confirmDelete();
    expect(add).toHaveBeenCalledWith(
      expect.objectContaining({ severity: 'error', detail: "No s'ha pogut eliminar la categoria." }),
    );
    expect(service.getAll).not.toHaveBeenCalled();
  });

  it('goes back a page when a change empties the page read', () => {
    fixture.componentInstance.onSearch({ first: 10, rows: 10 });
    service.getAll.mockClear();
    service.getAll
      .mockReturnValueOnce(of(documentCategoriesPage([], { number: 1, totalElements: 10 })))
      .mockReturnValueOnce(of(RESOLVED_PAGE));

    act(DocumentCategoryTableAction.Delete, MANUALS);
    list.confirmDelete();

    expect(service.getAll).toHaveBeenNthCalledWith(1, expect.objectContaining({ page: 1 }));
    expect(service.getAll).toHaveBeenNthCalledWith(2, expect.objectContaining({ page: 0 }));
    expect(list.tableFirst()).toBe(0);
  });
});
