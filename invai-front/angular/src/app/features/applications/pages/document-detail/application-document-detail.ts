import { afterNextRender, ChangeDetectionStrategy, Component, computed, DestroyRef, effect, ElementRef, HostListener, inject, Injector, LOCALE_ID, OnDestroy, signal, untracked } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { isStructuredBadRequest } from '@core/models/api-error.model';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { BreadcrumbService } from '@core/components/breadcrumbs';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { CrudEntityDialog } from '@components/crud-entity-dialog/crud-entity-dialog';
import { SectionContainerComponent } from '@components/section-container/section-container.component';
import { ActionParams } from '@models/table.model';
import { PAGINATOR_ROWS } from '@shared/constants/table.constants';
import { MessageService, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { SelectPassThrough } from 'primeng/types/select';
import { TableLazyLoadEvent } from 'primeng/table';
import { finalize, map, Observable, Subject, take } from 'rxjs';
import { ApplicationDocument, ApplicationDocumentDetailData, ApplicationDocumentMimeType, ApplicationDocumentVersion } from '../../application-documentation.model';
import { APPLICATIONS_ROUTES_LABELS, APPLICATIONS_ROUTES_LOC } from '../../applications.routes.i18n';
import { createApplicationDocumentDetailForm } from '../../forms/application-document-detail-form.factory';
import { APPLICATION_ATTACHMENT_VERSION_VALIDATORS, applicationAttachmentFileValidator } from '../../forms/application-documentation-form.factory';
import { ApplicationDocumentMimeTypeCatalogService } from '../../services/application-document-mime-type-catalog.service';
import { ApplicationDocumentationService } from '../../services/application-documentation.service';
import { APPLICATION_ATTACHMENT_VERSION_MAX_LENGTH, APPLICATION_DOCUMENTATION_TITLE_MAX_LENGTH } from '../detail/sections/documentation/application-documentation.constants';
import { APPLICATION_ATTACHMENT_DIALOG_TEXTS, APPLICATION_DOCUMENTATION_TEXTS } from '../detail/sections/documentation/application-documentation.i18n';
import {
  ApplicationDocumentFileRejection,
  documentationLabel,
  documentFileRejectionMessage,
  saveDocumentFile,
} from '../detail/sections/documentation/application-documentation.utils';
import { ApplicationDocumentDetailLoader } from './application-document-detail.loader';
import { DOCUMENT_DETAIL_TEXTS } from './application-document-detail.i18n';
import { ApplicationDocumentDetailResolvedData, DOCUMENT_DETAIL_RESOLVE_KEY } from './application-document-detail.resolver';
import { ApplicationDocumentVersionsTable, DOCUMENT_VERSION_COLUMNS, DocumentVersionAction } from './application-document-versions-table';
import { ApplicationDetailSectionActions } from '../detail/components/application-detail-section-actions/application-detail-section-actions';

@Component({
  selector: 'app-application-document-detail',
  imports: [Button, ConfirmationDialogComponent, CrudEntityDialog, InputText, ReactiveFormsModule, SectionContainerComponent, Select, ApplicationDocumentVersionsTable, ApplicationDetailSectionActions],
  templateUrl: './application-document-detail.html',
  styleUrl: './application-document-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationDocumentDetail implements OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly injector = inject(Injector);
  private readonly locale = inject(LOCALE_ID);
  private readonly service = inject(ApplicationDocumentationService);
  private readonly loader = inject(ApplicationDocumentDetailLoader);
  private readonly messages = inject(MessageService);
  private readonly breadcrumbs = inject(BreadcrumbService);
  private readonly browserTitle = inject(Title);
  private readonly previousTitle = this.browserTitle.getTitle();
  private readonly destroyRef = inject(DestroyRef);
  private readonly leaveDecisions = new Subject<boolean>();
  private readonly objectUrls = new Set<string>();
  private readonly resolved = toSignal(this.route.data.pipe(
    map((data) => data[DOCUMENT_DETAIL_RESOLVE_KEY] as ApplicationDocumentDetailResolvedData),
  ), { initialValue: this.route.snapshot.data[DOCUMENT_DETAIL_RESOLVE_KEY] as ApplicationDocumentDetailResolvedData });

  protected readonly texts = DOCUMENT_DETAIL_TEXTS;
  protected readonly labels = APPLICATION_ATTACHMENT_DIALOG_TEXTS.labels;
  protected readonly common = APPLICATION_DOCUMENTATION_TEXTS;
  protected readonly icons = PrimeIcons;
  protected readonly columns = DOCUMENT_VERSION_COLUMNS;
  protected readonly titleMaxLength = APPLICATION_DOCUMENTATION_TITLE_MAX_LENGTH;
  protected readonly versionMaxLength = APPLICATION_ATTACHMENT_VERSION_MAX_LENGTH;
  protected readonly form = createApplicationDocumentDetailForm(this.fb);
  protected readonly detail = signal<ApplicationDocumentDetailData | null>(null);
  protected readonly isEditing = signal(false);
  protected readonly isSaving = signal(false);
  // The document carries every version, so they are sorted and paged here.
  protected readonly versionFirst = signal(0);
  private readonly versionRows = signal(PAGINATOR_ROWS);
  private readonly versionSort = signal<{ field: string; order: number }>({ field: 'uploadedAt', order: -1 });
  protected readonly versions = computed(() => {
    const versions = this.detail()?.document.attachments ?? [];
    const { field, order } = this.versionSort();
    const sorted = [...versions].sort((left, right) => order * compareVersions(left, right, field));
    const first = this.versionFirst();
    return { items: sorted.slice(first, first + this.versionRows()), total: sorted.length };
  });
  protected readonly statusOptions = computed(() =>
    (this.detail()?.statuses ?? []).map((status) => ({ value: status.id, label: documentationLabel(status, this.locale) })),
  );
  private readonly mimeTypeCatalog = inject(ApplicationDocumentMimeTypeCatalogService);
  // Accepted file formats: a version file outside them is refused before it reaches the form, and
  // without them no file can be chosen.
  private readonly mimeTypes = signal<ApplicationDocumentMimeType[]>([]);
  private readonly mimeTypesFailed = signal(false);
  protected readonly canAttachVersion = computed(() => this.mimeTypes().length > 0);
  // The last refused version file; the one already chosen, if any, is kept.
  protected readonly versionFileRejection = signal<ApplicationDocumentFileRejection | null>(null);
  protected readonly accept = computed(() => this.mimeTypes().map(({ extension }) => '.' + extension).join(',') || null);
  protected readonly versionDialogVisible = signal(false);
  protected readonly versionSaving = signal(false);
  protected readonly leaveDialogVisible = signal(false);
  protected readonly versionForm = this.fb.group({
    version: this.fb.nonNullable.control('', APPLICATION_ATTACHMENT_VERSION_VALIDATORS),
    file: this.fb.control<File | null>(null, [
      Validators.required,
      applicationAttachmentFileValidator(() => this.mimeTypes().map(({ extension }) => extension)),
    ]),
  });
  protected readonly versionDialogAria = {
    ...APPLICATION_ATTACHMENT_DIALOG_TEXTS.ariaLabels,
    add: this.texts.newVersion, save: this.texts.save, cancel: this.texts.cancel,
    close: this.texts.cancel, accept: this.texts.save,
  };

  constructor() {
    afterNextRender(() => {
      const heading = this.host.nativeElement.querySelector('h1');
      heading?.setAttribute('tabindex', '-1');
      heading?.focus();
    });
    effect(() => {
      const resolved = this.resolved();
      untracked(() => this.initialize(resolved?.detail ?? null));
    });
    this.loadMimeTypes();
  }

  ngOnDestroy(): void {
    this.breadcrumbs.clear();
    this.browserTitle.setTitle(this.previousTitle);
    this.objectUrls.forEach((url) => URL.revokeObjectURL(url));
    this.leaveDecisions.next(false);
    this.leaveDecisions.complete();
  }

  @HostListener('window:beforeunload', ['$event'])
  protected beforeUnload(event: BeforeUnloadEvent): void {
    if (this.hasUnsavedChanges()) {
      event.preventDefault();
      event.returnValue = '';
    }
  }

  canDeactivate(): boolean | Observable<boolean> {
    if (this.isSaving() || this.versionSaving()) return false;
    if (!this.hasUnsavedChanges()) return true;
    this.leaveDialogVisible.set(true);
    return this.leaveDecisions.pipe(take(1));
  }

  protected decideLeave(leave: boolean): void {
    this.leaveDialogVisible.set(false);
    this.leaveDecisions.next(leave);
  }

  protected goBack(): void {
    const id = this.resolved()?.applicationId;
    void this.router.navigate(id ? ['/', APPLICATIONS_ROUTES_LOC.BASE, id, 'documentation'] : ['/', APPLICATIONS_ROUTES_LOC.BASE]);
  }

  protected retry(): void {
    const resolved = this.resolved();
    if (!resolved?.applicationId || !resolved.documentId) return;
    this.loader.load(resolved.applicationId, resolved.documentId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (detail) => this.initialize(detail), error: () => this.showError(this.texts.loadError) });
  }

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected errorOf(control: AbstractControl): string {
    if (control.hasError('duplicate')) return this.texts.duplicateVersion;
    if (control.hasError('fileTooLarge')) return this.common.fileTooLarge;
    if (control.hasError('fileType')) return this.common.fileTypeNotAllowed;
    return control.hasError('maxlength') ? this.common.maxLength(control === this.form.controls.title ? this.titleMaxLength : this.versionMaxLength) : this.common.required;
  }

  protected selectPt(control: AbstractControl, id: string, required = true): SelectPassThrough {
    return { label: {
      'aria-invalid': this.isInvalid(control), 'aria-required': required,
      'aria-describedby': this.isInvalid(control) ? id + '-error' : undefined,
    } };
  }

  protected subcategories(): { id: number; label: string }[] {
    return this.detail()?.categories.find(({ id }) => id === this.form.controls.categoryId.value)?.subcategories ?? [];
  }

  protected categoryChanged(): void { this.form.controls.subcategoryId.setValue(null); }

  protected categoryLabel(): string {
    return this.detail()?.categories.find(({ id }) => id === this.form.controls.categoryId.value)?.label ?? '—';
  }

  protected subcategoryLabel(): string {
    return this.subcategories().find(({ id }) => id === this.form.controls.subcategoryId.value)?.label ?? '—';
  }

  protected stateLabel(): string {
    return this.statusOptions().find(({ value }) => value === this.form.controls.statusId.value)?.label ?? '—';
  }

  protected startEditing(): void {
    if (!this.detail()?.application.canManage || this.isSaving() || this.versionSaving()) return;
    this.isEditing.set(true);
    this.focusAfterRender('#document-detail-title');
  }

  protected cancelEditing(): void {
    if (!this.isEditing() || this.isSaving()) return;
    this.initialize(this.detail());
    this.focusAfterRender('.document-detail-toolbar button');
  }

  protected save(): void {
    const detail = this.detail();
    if (!detail?.application.canManage || !this.isEditing() || this.isSaving()) return;
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const value = this.form.getRawValue();
    if (value.categoryId == null || value.statusId == null) return;
    this.isSaving.set(true);
    // The section is not edited here, but the back-end requires it.
    this.service.updateDocument(detail.document.id, {
      title: value.title.trim(),
      sectionId: detail.document.section.id,
      categoryId: value.subcategoryId ?? value.categoryId,
      statusId: value.statusId,
    }).pipe(finalize(() => this.isSaving.set(false)), takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (document) => {
        this.initialize({ ...detail, document });
        this.focusAfterRender('.document-detail-toolbar button');
        this.messages.add({ severity: 'success', summary: this.texts.saved });
      },
      error: (error: unknown) => {
        if (!isStructuredBadRequest(error)) this.showError(this.texts.saveError);
      },
    });
  }

  protected openNewVersion(): void {
    if (!this.detail()?.application.canManage || this.versionSaving()) return;
    this.versionForm.reset();
    this.versionFileRejection.set(null);
    if (this.mimeTypesFailed()) this.loadMimeTypes();
    this.versionDialogVisible.set(true);
  }

  protected hasVersionFileError(): boolean {
    return Boolean(this.versionFileRejection()) || this.isInvalid(this.versionForm.controls.file);
  }

  // The refusal of the last picked file or, without one, the validation error of the file.
  protected versionFileAlerts(): { key: string; message: string }[] {
    const rejection = this.versionFileRejection();
    if (rejection) return [{ key: 'refused-' + rejection.attempt, message: rejection.message }];
    const file = this.versionForm.controls.file;
    return this.isInvalid(file) ? [{ key: 'invalid', message: this.errorOf(file) }] : [];
  }

  protected versionFileDescribedBy(): string {
    return 'document-version-file-hint' + (this.hasVersionFileError() ? ' document-version-file-error' : '');
  }

  protected versionFileHint(): string {
    if (this.canAttachVersion()) return APPLICATION_ATTACHMENT_DIALOG_TEXTS.fileLimitsHint;
    return this.mimeTypesFailed() ? this.common.formatsUnavailable : this.common.formatsLoading;
  }

  protected selectVersionFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file || this.versionSaving() || !this.canAttachVersion()) return;
    const control = this.versionForm.controls.file;
    const message = documentFileRejectionMessage(file, this.mimeTypes());
    if (message) {
      this.versionFileRejection.set({ message, attempt: (this.versionFileRejection()?.attempt ?? 0) + 1 });
      showChosenFile(input, control.value);
      return;
    }
    this.versionFileRejection.set(null);
    control.setValue(file);
    control.markAsDirty();
    control.markAsTouched();
  }

  protected saveVersion(): void {
    const detail = this.detail();
    if (!detail?.application.canManage || this.versionSaving()) return;
    if (this.versionForm.invalid) { this.versionForm.markAllAsTouched(); return; }
    const { version, file } = this.versionForm.getRawValue();
    if (!file) return;
    const versionLabel = version.trim();
    if (detail.document.attachments.some((item) => item.versionLabel === versionLabel)) {
      this.versionForm.controls.version.setErrors({ duplicate: true }); return;
    }
    this.versionSaving.set(true);
    this.service.addDocumentVersion(detail.document.id, { versionLabel }, file)
      .pipe(finalize(() => this.versionSaving.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (document) => {
          this.versionDialogVisible.set(false);
          this.versionForm.reset();
          this.showDocumentVersions(document);
          this.messages.add({ severity: 'success', summary: this.texts.versionAdded });
        },
        error: (error: unknown) => {
          if (!isStructuredBadRequest(error)) this.showError(this.texts.versionError);
        },
      });
  }

  protected versionPage(event: TableLazyLoadEvent): void {
    const field = Array.isArray(event.sortField) ? event.sortField[0] : event.sortField;
    this.versionRows.set(event.rows ?? PAGINATOR_ROWS);
    this.versionSort.set({ field: field ?? 'uploadedAt', order: event.sortOrder === 1 ? 1 : -1 });
    this.versionFirst.set(event.first ?? 0);
  }

  protected versionAction(event: ActionParams<ApplicationDocumentVersion>): void {
    const detail = this.detail();
    if (!detail) return;
    const view = event.action === DocumentVersionAction.View;
    this.service.getDocumentVersionFile(detail.document.id, event.params.id, event.params.fileName, view)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (file) => {
          if (!view) {
            saveDocumentFile(file);
            return;
          }
          // The tab may still be reading the file, so its URL lives as long as this page.
          const url = URL.createObjectURL(file.blob);
          this.objectUrls.add(url);
          window.open(url, '_blank', 'noopener,noreferrer');
        },
        error: () => this.showError(this.texts.fileError),
      });
  }

  private initialize(detail: ApplicationDocumentDetailData | null): void {
    this.detail.set(detail);
    this.isEditing.set(false);
    if (!detail) return;
    this.form.reset({
      title: detail.document.title, categoryId: detail.document.category.id,
      subcategoryId: detail.document.subcategory?.id ?? null, statusId: detail.document.status.id,
    });
    const base = '/' + APPLICATIONS_ROUTES_LOC.BASE;
    this.breadcrumbs.setCustomBreadcrumbs([
      { label: APPLICATIONS_ROUTES_LABELS.BASE, routerLink: base },
      { label: $localize`Codi: ${detail.application.code}`, routerLink: [base, detail.application.id, 'general'] },
      { label: this.common.title, routerLink: [base, detail.application.id, 'documentation'] },
      { label: detail.document.title },
    ]);
    this.browserTitle.setTitle(detail.document.title + ' · ' + detail.application.name);
  }

  // Shows the versions of the document as the back-end returned them, the newest first.
  private showDocumentVersions(document: ApplicationDocument): void {
    this.detail.update((current) => current ? { ...current, document: { ...current.document, attachments: document.attachments } } : null);
    this.versionSort.set({ field: 'uploadedAt', order: -1 });
    this.versionFirst.set(0);
  }

  private hasUnsavedChanges(): boolean {
    return (this.isEditing() && this.form.dirty) || (this.versionDialogVisible() && this.versionForm.dirty);
  }

  private focusAfterRender(selector: string): void {
    afterNextRender(() => {
      this.host.nativeElement.querySelector<HTMLElement>(selector)?.focus();
    }, { injector: this.injector });
  }

  private loadMimeTypes(): void {
    this.mimeTypesFailed.set(false);
    this.mimeTypeCatalog.getAll().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (mimeTypes) => {
        this.mimeTypes.set(mimeTypes);
        this.mimeTypesFailed.set(!mimeTypes.length);
      },
      error: () => this.mimeTypesFailed.set(true),
    });
  }

  private showError(detail: string): void {
    this.messages.add({ severity: 'error', summary: this.common.errorSummary, detail });
  }
}

// Puts back in the native picker the file the form keeps, so that it never shows a refused one.
function showChosenFile(input: HTMLInputElement, file: File | null): void {
  try {
    const transfer = new DataTransfer();
    if (file) transfer.items.add(file);
    input.files = transfer.files;
  } catch {
    input.value = '';
  }
}

function compareVersions(left: ApplicationDocumentVersion, right: ApplicationDocumentVersion, field: string): number {
  if (field === 'fileSize') return left.fileSize - right.fileSize;
  const value = (version: ApplicationDocumentVersion) => String(version[field as keyof ApplicationDocumentVersion] ?? '');
  return value(left).localeCompare(value(right), 'ca', { numeric: true }) || left.id - right.id;
}
