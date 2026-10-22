import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { BreadcrumbService } from '@core/components/breadcrumbs';
import { MessageService } from 'primeng/api';
import { BehaviorSubject, firstValueFrom, isObservable, Observable, of, throwError } from 'rxjs';

import { DocumentCategoryTableAction } from '../../components/document-categories-table/document-categories-table';
import { DocumentCategory, DocumentCategoryRolePermissions } from '../../document-categories.model';
import { documentCategory } from '../../document-categories.testing';
import { DocumentCategoryFormGroup } from '../../forms/document-category-form.factory';
import { DocumentCategoryPermissionsFormArray } from '../../forms/document-category-permissions-form.factory';
import { DocumentCategoriesService } from '../../services/document-categories.service';
import { DocumentCategoryCatalogService } from '../../services/document-category-catalog.service';
import { DocumentCategoryPermissionsService } from '../../services/document-category-permissions.service';
import { DocumentCategoryDetail } from './document-category-detail';
import {
  DOCUMENT_CATEGORY_DETAIL_RESOLVE_KEY,
  DocumentCategoryDetailResolvedData,
} from './document-category-detail.resolver';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

const MANUALS = documentCategory({ id: 11, name: 'Manuals', nameEs: 'Manuales', parentId: 1 });
const TECHNICAL = documentCategory({ id: 1, subcategories: [MANUALS] });
const PERMISSIONS: DocumentCategoryRolePermissions[] = [
  { roleId: 1, roleName: 'Administració funcional', read: true, upload: true, modify: true, delete: true },
  { roleId: 4, roleName: 'Consulta', read: true, upload: false, modify: false, delete: false },
];

interface DetailInternals {
  form: DocumentCategoryFormGroup;
  permissionsForm: DocumentCategoryPermissionsFormArray;
  subcategoryForm: DocumentCategoryFormGroup;
  isLeaveDialogVisible: () => boolean;
  isSubcategoryDialogVisible: () => boolean;
  subcategoryDialogMode: () => string;
  save(): void;
  decideLeave(leave: boolean): void;
  goBack(): void;
  isEditing: () => boolean;
  startEditing(): void;
  cancelEditing(): void;
  openNewSubcategoryDialog(): void;
  onSubcategoryAction(event: { action: number; params: DocumentCategory }): void;
  submitSubcategory(): void;
}

describe('DocumentCategoryDetail', () => {
  let fixture: ComponentFixture<DocumentCategoryDetail>;
  let detail: DetailInternals;
  let add: ReturnType<typeof vi.spyOn>;
  const route: { data: Observable<Record<string, unknown>>; snapshot: { data: Record<string, unknown> } } = {
    data: of({}),
    snapshot: { data: {} },
  };
  const service = {
    getById: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
    reactivate: vi.fn(),
  };
  const permissions = { getByCategory: vi.fn(), save: vi.fn() };
  const getActiveCategoryOptions = vi.fn();

  const root = () => fixture.nativeElement as HTMLElement;
  const input = (id: string) => root().querySelector<HTMLInputElement>(`#${id}`)!;
  async function render(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  async function create(resolved: DocumentCategoryDetailResolvedData): Promise<void> {
    const data = { [DOCUMENT_CATEGORY_DETAIL_RESOLVE_KEY]: resolved };
    route.data = new BehaviorSubject<Record<string, unknown>>(data);
    route.snapshot.data = data;
    fixture = TestBed.createComponent(DocumentCategoryDetail);
    detail = fixture.componentInstance as unknown as DetailInternals;
    await render();
  }

  beforeEach(async () => {
    for (const mock of [...Object.values(service), ...Object.values(permissions)]) mock.mockReset();
    service.create.mockImplementation((input) => of(documentCategory({ id: 30, ...input })));
    service.update.mockImplementation((id, input) => of(documentCategory({ id, ...input })));
    service.getById.mockReturnValue(of(TECHNICAL));
    permissions.save.mockImplementation((_id, saved) => of(saved));
    getActiveCategoryOptions.mockReset().mockReturnValue(of([{ id: 2, label: 'Normativa' }]));

    await TestBed.configureTestingModule({
      imports: [DocumentCategoryDetail],
      providers: [
        MessageService,
        provideRouter([]),
        { provide: ActivatedRoute, useValue: route },
        { provide: DocumentCategoriesService, useValue: service },
        { provide: DocumentCategoryPermissionsService, useValue: permissions },
        { provide: DocumentCategoryCatalogService, useValue: { getActiveCategoryOptions } },
      ],
    }).compileComponents();
    add = vi.spyOn(TestBed.inject(MessageService), 'add');
  });

  it('shows the resolved category, its subcategories and permissions without requesting them again', async () => {
    await create({ category: TECHNICAL, permissions: PERMISSIONS, loadFailed: false });

    expect(root().querySelector('h1')?.textContent?.trim()).toBe('Tècnica');
    expect([input('document-category-detail-name').value, input('document-category-detail-name-es').value]).toEqual([
      'Tècnica',
      'Técnica',
    ]);
    expect([...root().querySelectorAll('h2')].map((h2) => h2.textContent?.trim())).toEqual([
      'Subcategories',
      "Permisos d'accés",
    ]);
    expect(root().querySelector('table[aria-label="Subcategories de «Tècnica»"]')?.textContent).toContain(
      'Manuals',
    );
    expect(detail.permissionsForm.getRawValue()).toEqual(PERMISSIONS);
    expect(detail.form.pristine && detail.permissionsForm.pristine).toBe(true);
    expect(service.getById).not.toHaveBeenCalled();
    expect(permissions.getByCategory).not.toHaveBeenCalled();
  });

  it('consults the names and permissions until the user starts editing them', async () => {
    await create({ category: TECHNICAL, permissions: PERMISSIONS, loadFailed: false });
    const checkboxes = () => [...root().querySelectorAll<HTMLInputElement>('input[type="checkbox"]')];

    expect(root().textContent).not.toContain('Tots els camps són obligatoris');
    expect(input('document-category-detail-name').readOnly).toBe(true);
    expect(checkboxes().every((checkbox) => checkbox.disabled)).toBe(true);
    expect(root().querySelector('button[aria-label="Desa els canvis de la categoria"]')).toBeNull();
    const edit = root().querySelector<HTMLButtonElement>('#document-category-detail-edit')!;
    expect([edit.textContent?.trim(), edit.getAttribute('aria-label')]).toEqual([
      'Editar',
      'Edita la categoria «Tècnica»',
    ]);
    // Enter in a field does not save while consulting.
    detail.save();
    expect(permissions.save).not.toHaveBeenCalled();

    edit.click();
    await render();

    expect(detail.isEditing()).toBe(true);
    expect(document.activeElement?.id).toBe('document-category-detail-name');
    expect(input('document-category-detail-name').readOnly).toBe(false);
    expect(checkboxes().some((checkbox) => checkbox.disabled)).toBe(false);
    expect(root().querySelector('button[aria-label="Desa els canvis de la categoria"]')).not.toBeNull();
    expect(root().querySelector('#document-category-detail-edit')).toBeNull();
    expect(root().querySelector('#document-category-detail-cancel')?.textContent?.trim()).toBe('Cancel·lar');
  });

  it('discards the changes when the edition is cancelled', async () => {
    await create({ category: TECHNICAL, permissions: PERMISSIONS, loadFailed: false });
    detail.startEditing();
    await render();
    detail.form.controls.name.setValue('Una altra');
    detail.form.markAsDirty();
    detail.permissionsForm.at(1).controls.delete.setValue(true);
    detail.permissionsForm.markAsDirty();

    root().querySelector<HTMLButtonElement>('#document-category-detail-cancel')!.click();
    await render();

    expect(detail.isEditing()).toBe(false);
    expect(detail.form.controls.name.value).toBe('Tècnica');
    expect(detail.permissionsForm.getRawValue()).toEqual(PERMISSIONS);
    expect(detail.permissionsForm.disabled).toBe(true);
    expect(fixture.componentInstance.canDeactivate()).toBe(true);
    expect(document.activeElement?.id).toBe('document-category-detail-edit');
    expect(service.update).not.toHaveBeenCalled();
  });

  it('opens in edit mode when the category was edited from the list', async () => {
    vi.spyOn(TestBed.inject(Router), 'currentNavigation').mockReturnValue({
      extras: { info: { documentCategoryMode: 'edit' } },
    } as unknown as ReturnType<Router['currentNavigation']>);

    await create({ category: TECHNICAL, permissions: PERMISSIONS, loadFailed: false });

    expect(detail.isEditing()).toBe(true);
    expect(input('document-category-detail-name').readOnly).toBe(false);
    expect(detail.permissionsForm.enabled).toBe(true);
  });

  it('places the category under the Documentació maintenance in the breadcrumbs', async () => {
    await create({ category: TECHNICAL, permissions: PERMISSIONS, loadFailed: false });

    expect(TestBed.inject(BreadcrumbService).breadcrumbs()).toEqual([
      { label: 'Manteniments', routerLink: ['/', 'manteniments'] },
      { label: 'Documentació', routerLink: ['/', 'manteniments', 'documentacio'] },
      {
        label: 'Categories documentals',
        routerLink: ['/', 'manteniments', 'documentacio'],
        fragment: 'document-categories',
      },
      { label: 'Tècnica' },
    ]);

    fixture.destroy();
    expect(TestBed.inject(BreadcrumbService).breadcrumbs()).toEqual([]);
  });

  it('does not save invalid names and relates each error to its field', async () => {
    await create({ category: TECHNICAL, permissions: PERMISSIONS, loadFailed: false });
    detail.startEditing();
    detail.form.controls.name.setValue(' ');

    detail.save();
    await render();

    expect(service.update).not.toHaveBeenCalled();
    expect(permissions.save).not.toHaveBeenCalled();
    const name = input('document-category-detail-name');
    expect(name.getAttribute('aria-invalid')).toBe('true');
    expect(root().querySelector(`#${name.getAttribute('aria-describedby')}`)?.textContent?.trim()).toBe(
      'Camp obligatori',
    );
  });

  it('saves the names and the permissions together and leaves the page pristine', async () => {
    await create({ category: TECHNICAL, permissions: PERMISSIONS, loadFailed: false });
    detail.startEditing();
    detail.form.controls.name.setValue(' Tècnica i arquitectura ');
    detail.form.markAsDirty();
    detail.permissionsForm.at(1).controls.upload.setValue(true);
    detail.permissionsForm.markAsDirty();

    detail.save();
    await render();

    expect(service.update).toHaveBeenCalledWith(1, {
      name: 'Tècnica i arquitectura',
      nameEs: 'Técnica',
      parentId: null,
    });
    expect(permissions.save).toHaveBeenCalledWith(1, [
      PERMISSIONS[0],
      { ...PERMISSIONS[1], upload: true },
    ]);
    expect(add).toHaveBeenCalledWith(
      expect.objectContaining({ severity: 'success', summary: 'Categoria actualitzada' }),
    );
    expect(root().querySelector('h1')?.textContent?.trim()).toBe('Tècnica i arquitectura');
    expect(detail.form.dirty || detail.permissionsForm.dirty).toBe(false);
    // Saving goes back to consulting the category.
    expect(detail.isEditing()).toBe(false);
    expect(input('document-category-detail-name').readOnly).toBe(true);
    expect(document.activeElement?.id).toBe('document-category-detail-edit');
    expect(root().textContent).toContain('Manuals');
  });

  it('only saves the permissions when the names did not change', async () => {
    await create({ category: TECHNICAL, permissions: PERMISSIONS, loadFailed: false });
    detail.startEditing();
    detail.permissionsForm.at(1).controls.read.setValue(false);
    detail.permissionsForm.markAsDirty();

    detail.save();

    expect(service.update).not.toHaveBeenCalled();
    expect(permissions.save).toHaveBeenCalledTimes(1);
  });

  it('reports a failed save other than a validation error', async () => {
    await create({ category: TECHNICAL, permissions: PERMISSIONS, loadFailed: false });
    detail.startEditing();
    detail.form.markAsDirty();
    service.update.mockReturnValueOnce(throwError(() => new HttpErrorResponse({ status: 500 })));

    detail.save();

    expect(add).toHaveBeenCalledWith(
      expect.objectContaining({ severity: 'error', detail: "No s'ha pogut desar la categoria." }),
    );
    expect(detail.form.dirty).toBe(true);
    expect(detail.isEditing()).toBe(true);
  });

  it('asks before leaving with unsaved changes and stays or leaves as decided', async () => {
    await create({ category: TECHNICAL, permissions: PERMISSIONS, loadFailed: false });
    expect(fixture.componentInstance.canDeactivate()).toBe(true);

    detail.startEditing();
    detail.permissionsForm.markAsDirty();
    const stay = fixture.componentInstance.canDeactivate();
    expect(isObservable(stay)).toBe(true);
    const stayDecision = firstValueFrom(stay as Observable<boolean>);
    expect(detail.isLeaveDialogVisible()).toBe(true);
    detail.decideLeave(false);
    await expect(stayDecision).resolves.toBe(false);
    expect(detail.isLeaveDialogVisible()).toBe(false);

    const leaveDecision = firstValueFrom(fixture.componentInstance.canDeactivate() as Observable<boolean>);
    detail.decideLeave(true);
    await expect(leaveDecision).resolves.toBe(true);
  });

  it('goes back to the list of categories', async () => {
    await create({ category: TECHNICAL, permissions: PERMISSIONS, loadFailed: false });
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    detail.goBack();

    expect(navigate).toHaveBeenCalledWith(['/', 'manteniments', 'documentacio'], {
      fragment: 'document-categories',
    });
  });

  it('shows an inactive category without letting it change', async () => {
    await create({
      category: { ...TECHNICAL, deletedAt: '2026-10-01T10:00:00' },
      permissions: PERMISSIONS,
      loadFailed: false,
    });

    expect(root().textContent).toContain('Aquesta categoria està donada de baixa');
    expect(input('document-category-detail-name').readOnly).toBe(true);
    expect(input('document-category-detail-name-es').readOnly).toBe(true);
    const checkboxes = [...root().querySelectorAll<HTMLInputElement>('input[type="checkbox"]')];
    expect(checkboxes.length).toBeGreaterThan(0);
    expect(checkboxes.every((checkbox) => checkbox.disabled)).toBe(true);
    expect(root().querySelector('button[aria-label="Desa els canvis de la categoria"]')).toBeNull();
    expect(root().querySelector('#document-category-detail-edit')).toBeNull();

    detail.startEditing();
    expect(detail.isEditing()).toBe(false);
    detail.form.markAsDirty();
    detail.save();
    expect(service.update).not.toHaveBeenCalled();
    expect(fixture.componentInstance.canDeactivate()).toBe(true);
  });

  it('reports a category that could not be read and offers to go back', async () => {
    await create({ category: null, permissions: [], loadFailed: true });

    expect(root().querySelector('h1')?.textContent?.trim()).toBe('Categoria documental');
    expect(root().querySelector('[role="alert"]')?.textContent).toContain(
      "No s'ha pogut carregar la categoria documental.",
    );
    expect(root().querySelector('form')).toBeNull();
    expect(root().querySelector('button[aria-label="Torna al llistat de categories documentals"]')).not.toBeNull();
    expect(add).toHaveBeenCalledWith(expect.objectContaining({ severity: 'error' }));
  });

  it('edits a subcategory in the dialog and reads the subcategories again keeping unsaved names', async () => {
    await create({ category: TECHNICAL, permissions: PERMISSIONS, loadFailed: false });
    detail.form.controls.nameEs.setValue('Técnica nueva');
    detail.form.markAsDirty();
    service.getById
      .mockReturnValueOnce(of(MANUALS))
      .mockReturnValueOnce(
        of({ ...TECHNICAL, subcategories: [{ ...MANUALS, name: 'Manuals d’usuari' }] }),
      );

    detail.onSubcategoryAction({ action: DocumentCategoryTableAction.Edit, params: MANUALS });
    expect([detail.isSubcategoryDialogVisible(), detail.subcategoryDialogMode()]).toEqual([true, 'edit']);
    expect(getActiveCategoryOptions).toHaveBeenCalled();
    detail.subcategoryForm.controls.name.setValue('Manuals d’usuari');
    detail.submitSubcategory();
    await render();

    expect(service.update).toHaveBeenCalledWith(11, {
      name: 'Manuals d’usuari',
      nameEs: 'Manuales',
      parentId: 1,
    });
    expect(detail.isSubcategoryDialogVisible()).toBe(false);
    expect(root().querySelector('table[aria-label="Subcategories de «Tècnica»"]')?.textContent).toContain(
      'Manuals d’usuari',
    );
    expect(detail.form.controls.nameEs.value).toBe('Técnica nueva');
  });

  it('adds a subcategory to this category from its subcategories table', async () => {
    await create({ category: TECHNICAL, permissions: PERMISSIONS, loadFailed: false });
    service.getById.mockReturnValueOnce(
      of({ ...TECHNICAL, subcategories: [MANUALS, documentCategory({ id: 30, name: 'API', parentId: 1 })] }),
    );
    const button = root().querySelector<HTMLButtonElement>(
      'button[aria-label="Afegeix una subcategoria a «Tècnica»"]',
    )!;
    expect(button.textContent?.trim()).toBe('Afegeix subcategoria');

    button.click();
    await render();
    expect([detail.isSubcategoryDialogVisible(), detail.subcategoryDialogMode()]).toEqual([true, 'create']);
    expect(detail.subcategoryForm.getRawValue()).toEqual({ name: '', nameEs: '', parentId: 1 });
    expect(getActiveCategoryOptions).not.toHaveBeenCalled();
    detail.subcategoryForm.patchValue({ name: ' API ', nameEs: 'API' });
    detail.submitSubcategory();
    await render();

    expect(service.create).toHaveBeenCalledWith({ name: 'API', nameEs: 'API', parentId: 1 });
    expect(add).toHaveBeenCalledWith(
      expect.objectContaining({ severity: 'success', summary: 'Subcategoria afegida' }),
    );
    expect(detail.isSubcategoryDialogVisible()).toBe(false);
    expect(root().querySelector('table[aria-label="Subcategories de «Tècnica»"]')?.textContent).toContain('API');
  });

  it('does not offer to add subcategories to an inactive category', async () => {
    await create({
      category: { ...TECHNICAL, deletedAt: '2026-10-01T10:00:00' },
      permissions: PERMISSIONS,
      loadFailed: false,
    });

    expect(root().querySelector('button[aria-label^="Afegeix una subcategoria"]')).toBeNull();
    detail.openNewSubcategoryDialog();
    expect(detail.isSubcategoryDialogVisible()).toBe(false);
  });
});
