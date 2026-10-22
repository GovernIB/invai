import {
  afterNextRender,
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  Injector,
  OnDestroy,
  signal,
  untracked,
} from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { SectionContainerComponent } from '@components/section-container/section-container.component';
import { BreadcrumbService } from '@core/components/breadcrumbs';
import { isStructuredBadRequest } from '@core/models/api-error.model';
import {
  MAINTENANCES_ROUTES_LABELS,
  MAINTENANCES_ROUTES_LOC,
} from '@features/maintenances/maintenances.routes.i18n';
import { ActionParams } from '@models/table.model';
import { MessageService, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import {
  catchError,
  finalize,
  forkJoin,
  map,
  Observable,
  of,
  Subject,
  switchMap,
  take,
  tap,
} from 'rxjs';

import { DocumentCategoryTableAction } from '../../components/document-categories-table/document-categories-table';
import {
  DocumentCategoryDialog,
  DocumentCategoryDialogMode,
} from '../../components/document-category-dialog/document-category-dialog';
import {
  DOCUMENT_CATEGORY_DIALOG_ERRORS,
  DOCUMENT_CATEGORY_DIALOG_LABELS,
} from '../../components/document-category-dialog/document-category-dialog.i18n';
import { DocumentCategoryPermissionsTable } from '../../components/document-category-permissions-table/document-category-permissions-table';
import { DocumentSubcategoriesTable } from '../../components/document-subcategories-table/document-subcategories-table';
import {
  DOCUMENT_CATEGORIES_LIST_LINK,
  DOCUMENT_CATEGORIES_PANEL_FRAGMENT,
  DOCUMENT_CATEGORY_NAME_MAX_LENGTH,
  DOCUMENT_SUBCATEGORIES_TABLE_COLUMNS,
  isDocumentCategoryEditNavigation,
} from '../../document-categories.constants';
import {
  DocumentCategory,
  DocumentCategoryInput,
  DocumentCategoryOption,
  DocumentCategoryRolePermissions,
} from '../../document-categories.model';
import { createDocumentCategoryForm } from '../../forms/document-category-form.factory';
import {
  createDocumentCategoryPermissionsForm,
  resetDocumentCategoryPermissionsForm,
} from '../../forms/document-category-permissions-form.factory';
import { DocumentCategoriesService } from '../../services/document-categories.service';
import { DocumentCategoryCatalogService } from '../../services/document-category-catalog.service';
import { DocumentCategoryPermissionsService } from '../../services/document-category-permissions.service';
import {
  DOCUMENT_CATEGORIES_ERROR_SUMMARY,
  DOCUMENT_CATEGORY_DELETE_DIALOG_CANCEL_LABEL,
  DOCUMENT_CATEGORY_DELETE_DIALOG_CONFIRM_LABEL,
  DOCUMENT_CATEGORY_OPTIONS_LOAD_ERROR,
  DOCUMENT_CATEGORY_TEXTS,
} from '../list/document-categories-list.i18n';
import { DOCUMENT_CATEGORY_DETAIL_TEXTS } from './document-category-detail.i18n';
import {
  DOCUMENT_CATEGORY_DETAIL_RESOLVE_KEY,
  DocumentCategoryDetailResolvedData,
} from './document-category-detail.resolver';

// Detail of a category: its names, its subcategories and the permissions of each role over its
// documents. The names and the permissions are consulted until "Editar" is pressed, and "Desa"
// saves them together; the subcategories are added and changed one by one through the shared
// dialog and confirmation, in either mode.
@Component({
  selector: 'app-document-category-detail',
  standalone: true,
  imports: [
    Button,
    ConfirmationDialogComponent,
    DocumentCategoryDialog,
    DocumentCategoryPermissionsTable,
    DocumentSubcategoriesTable,
    FloatLabel,
    InputText,
    ReactiveFormsModule,
    SectionContainerComponent,
  ],
  templateUrl: './document-category-detail.html',
  styleUrl: './document-category-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DocumentCategoryDetail implements OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly injector = inject(Injector);
  // "Edita" in the list opens the category in edit mode.
  private pendingInitialEdit = isDocumentCategoryEditNavigation(
    this.router.currentNavigation()?.extras.info,
  );
  private savedPermissions: DocumentCategoryRolePermissions[] = [];
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  private readonly messageService = inject(MessageService);
  private readonly breadcrumbService = inject(BreadcrumbService);
  private readonly categoriesService = inject(DocumentCategoriesService);
  private readonly catalogService = inject(DocumentCategoryCatalogService);
  private readonly permissionsService = inject(DocumentCategoryPermissionsService);
  private readonly resolved = toSignal(
    this.route.data.pipe(
      map(
        (data) => data[DOCUMENT_CATEGORY_DETAIL_RESOLVE_KEY] as DocumentCategoryDetailResolvedData,
      ),
    ),
    {
      initialValue: this.route.snapshot.data[
        DOCUMENT_CATEGORY_DETAIL_RESOLVE_KEY
      ] as DocumentCategoryDetailResolvedData,
    },
  );
  private readonly leaveDecisions = new Subject<boolean>();

  protected readonly texts = DOCUMENT_CATEGORY_DETAIL_TEXTS;
  protected readonly labels = DOCUMENT_CATEGORY_DIALOG_LABELS;
  protected readonly errors = DOCUMENT_CATEGORY_DIALOG_ERRORS;
  protected readonly nameMaxLength = DOCUMENT_CATEGORY_NAME_MAX_LENGTH;
  protected readonly subcategoryColumns = DOCUMENT_SUBCATEGORIES_TABLE_COLUMNS;
  protected readonly icons = PrimeIcons;
  protected readonly deleteDialogCancelLabel = DOCUMENT_CATEGORY_DELETE_DIALOG_CANCEL_LABEL;
  protected readonly deleteDialogConfirmLabel = DOCUMENT_CATEGORY_DELETE_DIALOG_CONFIRM_LABEL;
  protected readonly subcategoryTexts = DOCUMENT_CATEGORY_TEXTS.subcategory;

  protected readonly form = createDocumentCategoryForm(this.fb);
  protected readonly permissionsForm = createDocumentCategoryPermissionsForm(this.fb);
  protected readonly category = signal<DocumentCategory | null>(null);
  protected readonly isSaving = signal(false);
  protected readonly isLeaveDialogVisible = signal(false);
  protected readonly isActive = computed(() => !this.category()?.deletedAt);
  protected readonly isEditing = signal(false);
  protected readonly title = computed(
    () => this.category()?.name?.trim() || this.texts.fallbackTitle,
  );
  protected readonly subcategoriesList = computed(() => {
    const items = this.category()?.subcategories ?? [];
    return { items, total: items.length };
  });

  // Subcategory dialog and confirmation.
  protected readonly subcategoryForm = createDocumentCategoryForm(this.fb);
  protected readonly selectedSubcategory = signal<DocumentCategory | null>(null);
  protected readonly subcategoryDialogMode = signal<DocumentCategoryDialogMode>('view');
  protected readonly isSubcategoryDialogVisible = signal(false);
  protected readonly isSubcategoryLoading = signal(false);
  protected readonly isSubcategorySaving = signal(false);
  protected readonly isSubcategoriesRefreshing = signal(false);
  protected readonly categoryOptions = signal<DocumentCategoryOption[]>([]);
  protected readonly pendingDelete = signal<DocumentCategory | null>(null);
  protected readonly isDeleteDialogVisible = signal(false);
  protected readonly isDeleting = signal(false);
  // A subcategory can only be restored while its category is active.
  protected readonly canRestoreSelectedSubcategory = computed(
    () => Boolean(this.selectedSubcategory()?.deletedAt) && this.isActive(),
  );
  protected readonly deleteDialogMessage = computed(() =>
    this.subcategoryTexts.deleteDialogMessage(this.pendingDelete()?.name ?? ''),
  );

  constructor() {
    effect(() => {
      const resolved = this.resolved();
      untracked(() => this.initialize(resolved));
    });

    effect(() => {
      const title = this.title();
      untracked(() =>
        this.breadcrumbService.setCustomBreadcrumbs([
          {
            label: MAINTENANCES_ROUTES_LABELS.BASE,
            routerLink: ['/', MAINTENANCES_ROUTES_LOC.BASE],
          },
          {
            label: MAINTENANCES_ROUTES_LABELS.DOCUMENTATION,
            routerLink: DOCUMENT_CATEGORIES_LIST_LINK,
          },
          {
            label: MAINTENANCES_ROUTES_LABELS.DOCUMENT_CATEGORIES,
            routerLink: DOCUMENT_CATEGORIES_LIST_LINK,
            fragment: DOCUMENT_CATEGORIES_PANEL_FRAGMENT,
          },
          { label: title },
        ]),
      );
    });
  }

  ngOnDestroy(): void {
    this.breadcrumbService.clear();
  }

  protected startEditing(): void {
    if (!this.category() || !this.isActive() || this.isEditing()) return;
    this.isEditing.set(true);
    this.permissionsForm.enable({ emitEvent: false });
    this.focusAfterRender('document-category-detail-name');
  }

  protected cancelEditing(): void {
    const category = this.category();
    if (!category || !this.isEditing() || this.isSaving()) return;
    this.isEditing.set(false);
    this.resetForms(category, this.savedPermissions);
    this.focusAfterRender('document-category-detail-edit');
  }

  canDeactivate(): boolean | Observable<boolean> {
    if (!this.hasUnsavedChanges()) return true;
    this.isLeaveDialogVisible.set(true);
    return this.leaveDecisions.pipe(take(1));
  }

  protected decideLeave(leave: boolean): void {
    if (!this.isLeaveDialogVisible()) return;
    this.isLeaveDialogVisible.set(false);
    this.leaveDecisions.next(leave);
  }

  protected goBack(): void {
    void this.router.navigate(DOCUMENT_CATEGORIES_LIST_LINK, {
      fragment: DOCUMENT_CATEGORIES_PANEL_FRAGMENT,
    });
  }

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected nameError(control: AbstractControl, maxLengthError: string): string {
    return control.hasError('maxlength') ? maxLengthError : this.errors.required;
  }

  protected save(): void {
    const category = this.category();
    if (!category || !this.isEditing() || this.isSaving()) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const payload: DocumentCategoryInput = {
      name: value.name.trim(),
      nameEs: value.nameEs.trim(),
      parentId: null,
    };
    const permissions: DocumentCategoryRolePermissions[] = this.permissionsForm.getRawValue();
    const texts = DOCUMENT_CATEGORY_TEXTS.category;
    this.isSaving.set(true);
    forkJoin({
      category: this.form.dirty
        ? this.categoriesService.update(category.id, payload)
        : of(category),
      permissions: this.permissionsService.save(category.id, permissions),
    })
      .pipe(
        finalize(() => this.isSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (saved) => {
          this.category.set({ ...saved.category, subcategories: category.subcategories });
          this.isEditing.set(false);
          this.resetForms(saved.category, saved.permissions);
          this.focusAfterRender('document-category-detail-edit');
          this.messageService.add({
            severity: 'success',
            summary: texts.updatedTitle,
            detail: texts.updatedDetail,
          });
        },
        error: (error: unknown) => {
          if (!isStructuredBadRequest(error)) this.showError(texts.saveError);
        },
      });
  }

  protected onSubcategoryAction(event: ActionParams<DocumentCategory>): void {
    if (this.hasSubcategoryMutationInProgress()) return;
    const subcategory = event.params;
    switch (event.action) {
      case DocumentCategoryTableAction.View:
        this.openSubcategoryDialog(subcategory, 'view');
        break;
      case DocumentCategoryTableAction.Edit:
        this.openSubcategoryDialog(subcategory, 'edit');
        break;
      case DocumentCategoryTableAction.Delete:
        this.pendingDelete.set(subcategory);
        this.isDeleteDialogVisible.set(true);
        break;
      case DocumentCategoryTableAction.Restore:
        this.restoreSubcategory(subcategory);
        break;
    }
  }

  // A new subcategory always belongs to this category, which must be active.
  protected openNewSubcategoryDialog(): void {
    const category = this.category();
    if (!category || !this.isActive() || this.hasSubcategoryMutationInProgress()) return;
    this.selectedSubcategory.set(null);
    this.subcategoryForm.reset({ name: '', nameEs: '', parentId: category.id });
    this.subcategoryDialogMode.set('create');
    this.isSubcategoryDialogVisible.set(true);
  }

  protected closeSubcategoryDialog(): void {
    if (this.isSubcategoryLoading() || this.isSubcategorySaving()) return;
    this.selectedSubcategory.set(null);
    this.isSubcategoryDialogVisible.set(false);
  }

  protected startSubcategoryEdit(): void {
    const subcategory = this.selectedSubcategory();
    if (this.subcategoryDialogMode() !== 'view' || !subcategory || subcategory.deletedAt) return;
    this.subcategoryDialogMode.set('edit');
  }

  protected cancelSubcategoryEdit(): void {
    const subcategory = this.selectedSubcategory();
    if (this.subcategoryDialogMode() !== 'edit' || !subcategory || this.isSubcategorySaving()) {
      return;
    }
    this.resetSubcategoryForm(subcategory);
    this.subcategoryDialogMode.set('view');
  }

  protected deactivateSelectedSubcategory(): void {
    const subcategory = this.selectedSubcategory();
    if (!subcategory || subcategory.deletedAt || this.hasSubcategoryMutationInProgress()) return;
    this.pendingDelete.set(subcategory);
    this.isDeleteDialogVisible.set(true);
  }

  protected restoreSelectedSubcategory(): void {
    const subcategory = this.selectedSubcategory();
    if (subcategory) this.restoreSubcategory(subcategory);
  }

  protected submitSubcategory(): void {
    const subcategory = this.selectedSubcategory();
    const isCreate = this.subcategoryDialogMode() === 'create';
    if ((!isCreate && !subcategory) || this.isSubcategorySaving() || this.isSubcategoryLoading()) {
      return;
    }
    if (this.subcategoryForm.invalid) {
      this.subcategoryForm.markAllAsTouched();
      return;
    }
    const value = this.subcategoryForm.getRawValue();
    if (value.parentId == null) return;
    const payload: DocumentCategoryInput = {
      name: value.name.trim(),
      nameEs: value.nameEs.trim(),
      parentId: value.parentId,
    };
    this.isSubcategorySaving.set(true);
    (isCreate
      ? this.categoriesService.create(payload)
      : this.categoriesService.update(subcategory!.id, payload)
    )
      .pipe(
        finalize(() => this.isSubcategorySaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: isCreate ? this.subcategoryTexts.createdTitle : this.subcategoryTexts.updatedTitle,
            detail: isCreate
              ? this.subcategoryTexts.createdDetail
              : this.subcategoryTexts.updatedDetail,
          });
          this.isSubcategoryDialogVisible.set(false);
          this.selectedSubcategory.set(null);
          this.refreshSubcategories();
        },
        error: (error: unknown) => {
          if (!isStructuredBadRequest(error)) this.showError(this.subcategoryTexts.saveError);
        },
      });
  }

  protected closeDeleteDialog(): void {
    this.isDeleteDialogVisible.set(false);
    if (!this.isDeleting()) this.pendingDelete.set(null);
  }

  protected confirmDelete(): void {
    const subcategory = this.pendingDelete();
    if (!subcategory || this.isDeleting()) return;
    this.isDeleteDialogVisible.set(false);
    this.isDeleting.set(true);
    this.categoriesService
      .delete(subcategory.id)
      .pipe(
        finalize(() => this.isDeleting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.pendingDelete.set(null);
          if (this.selectedSubcategory()?.id === subcategory.id) {
            this.isSubcategoryDialogVisible.set(false);
            this.selectedSubcategory.set(null);
          }
          this.messageService.add({
            severity: 'success',
            summary: this.subcategoryTexts.deletedTitle,
            detail: this.subcategoryTexts.deletedDetail,
          });
          this.refreshSubcategories();
        },
        error: (error: unknown) => {
          this.pendingDelete.set(null);
          if (!isStructuredBadRequest(error)) this.showError(this.subcategoryTexts.deleteError);
        },
      });
  }

  private initialize(resolved: DocumentCategoryDetailResolvedData | undefined): void {
    const category = resolved?.category ?? null;
    this.category.set(category);
    if (!category) {
      if (resolved?.loadFailed) this.showError(this.texts.loadError);
      return;
    }
    this.isEditing.set(this.pendingInitialEdit && !category.deletedAt);
    this.pendingInitialEdit = false;
    this.resetForms(category, resolved?.permissions ?? []);
  }

  // Leaves both forms pristine with the saved values; the permissions can only change while
  // editing.
  private resetForms(
    category: DocumentCategory,
    permissions: DocumentCategoryRolePermissions[],
  ): void {
    this.savedPermissions = permissions.map((role) => ({ ...role }));
    this.form.reset({ name: category.name ?? '', nameEs: category.nameEs ?? '', parentId: null });
    resetDocumentCategoryPermissionsForm(this.fb, this.permissionsForm, permissions);
    if (this.isEditing()) this.permissionsForm.enable({ emitEvent: false });
    else this.permissionsForm.disable({ emitEvent: false });
  }

  private hasUnsavedChanges(): boolean {
    return this.isEditing() && (this.form.dirty || this.permissionsForm.dirty);
  }

  // The button that started or ended the edition disappears, so focus moves to its counterpart.
  private focusAfterRender(id: string): void {
    afterNextRender(() => document.getElementById(id)?.focus(), { injector: this.injector });
  }

  private openSubcategoryDialog(
    subcategory: DocumentCategory,
    mode: Exclude<DocumentCategoryDialogMode, 'create'>,
  ): void {
    this.selectedSubcategory.set(subcategory);
    this.subcategoryForm.reset({ name: '', nameEs: '', parentId: subcategory.parentId });
    this.subcategoryDialogMode.set(mode);
    this.isSubcategoryDialogVisible.set(true);
    this.isSubcategoryLoading.set(true);
    this.categoriesService
      .getById(subcategory.id)
      .pipe(
        switchMap((loaded) =>
          loaded.deletedAt ? of(loaded) : this.loadCategoryOptions().pipe(map(() => loaded)),
        ),
        finalize(() => this.isSubcategoryLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (loaded) => {
          this.selectedSubcategory.set(loaded);
          this.resetSubcategoryForm(loaded);
        },
        error: (error: unknown) => {
          this.isSubcategoryDialogVisible.set(false);
          this.selectedSubcategory.set(null);
          if (!isStructuredBadRequest(error)) this.showError(this.subcategoryTexts.loadError);
        },
      });
  }

  // An active subcategory can move to another active category; this one is always offered.
  private loadCategoryOptions(): Observable<DocumentCategoryOption[]> {
    const category = this.category();
    const current: DocumentCategoryOption | null = category
      ? { id: category.id, label: this.title() }
      : null;
    return this.catalogService.getActiveCategoryOptions().pipe(
      catchError(() => {
        this.showError(DOCUMENT_CATEGORY_OPTIONS_LOAD_ERROR);
        return of([]);
      }),
      tap((options) =>
        this.categoryOptions.set(
          !current || options.some(({ id }) => id === current.id) ? options : [current, ...options],
        ),
      ),
    );
  }

  private resetSubcategoryForm(subcategory: DocumentCategory): void {
    this.subcategoryForm.reset({
      name: subcategory.name ?? '',
      nameEs: subcategory.nameEs ?? '',
      parentId: subcategory.parentId,
    });
  }

  private restoreSubcategory(subcategory: DocumentCategory): void {
    if (this.hasSubcategoryMutationInProgress() || !this.isActive()) return;
    this.isSubcategorySaving.set(true);
    this.categoriesService
      .reactivate(subcategory.id)
      .pipe(
        finalize(() => this.isSubcategorySaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.isSubcategoryDialogVisible.set(false);
          this.selectedSubcategory.set(null);
          this.messageService.add({
            severity: 'success',
            summary: this.subcategoryTexts.restoredTitle,
            detail: this.subcategoryTexts.restoredDetail,
          });
          this.refreshSubcategories();
        },
        error: (error: unknown) => {
          if (!isStructuredBadRequest(error)) this.showError(this.subcategoryTexts.restoreError);
        },
      });
  }

  // Reads the category again to show its current subcategories, keeping the unsaved names and
  // permissions.
  private refreshSubcategories(): void {
    const category = this.category();
    if (!category) return;
    this.isSubcategoriesRefreshing.set(true);
    this.categoriesService
      .getById(category.id)
      .pipe(
        finalize(() => this.isSubcategoriesRefreshing.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (loaded) =>
          this.category.update((current) =>
            current ? { ...current, subcategories: loaded.subcategories } : current,
          ),
        error: () => this.showError(this.texts.loadError),
      });
  }

  private hasSubcategoryMutationInProgress(): boolean {
    return this.isSubcategorySaving() || this.isDeleting() || this.isSubcategoryLoading();
  }

  private showError(detail: string): void {
    this.messageService.add({
      severity: 'error',
      summary: DOCUMENT_CATEGORIES_ERROR_SUMMARY,
      detail,
    });
  }
}
