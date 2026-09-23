import { DestroyRef, Injectable, LOCALE_ID, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder } from '@angular/forms';
import { CrudEntityDialogMode } from '@components/crud-entity-dialog/crud-entity-dialog';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { ActionParams, PaginatedList } from '@models/table.model';
import { PAGINATOR_ROWS } from '@shared/constants/table.constants';
import { localizedName } from '@shared/utils/localized-name.utils';
import { MessageService } from 'primeng/api';
import { TableLazyLoadEvent } from 'primeng/table';
import { Subject, finalize, takeUntil } from 'rxjs';
import {
  ApplicationSecurityResourceOutput,
  ApplicationWebContextInput,
  ApplicationWebContextOutput,
  SecurityCatalogItem,
} from '../../../../applications.model';
import {
  configureApplicationSecurityResourceForm,
  createApplicationSecurityResourceForm,
} from '../../../../forms/application-security-form.factory';
import { ApplicationWebContextsService } from '../../../../services/application-security.service';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationSecurityTableAction } from '../security/application-security-resource-table';
import {
  APPLICATION_SECURITY_ERROR_TITLE,
  APPLICATION_SECURITY_SUCCESS_TITLE,
  APPLICATION_SECURITY_RESOURCE_SAVE_ERROR,
  APPLICATION_SECURITY_RESOURCE_SAVE_SUCCESS,
  APPLICATION_SECURITY_RESOURCE_DELETE_ERROR,
  APPLICATION_SECURITY_RESOURCE_DELETE_SUCCESS,
} from '../security/application-security-section.i18n';
import { APPLICATION_DEVELOPMENT_WEB_CONTEXTS_LOAD_ERROR } from './application-development-section.i18n';
import { ApplicationDevelopmentWebContextsData } from './application-development-web-contexts.resolver';

/** Scoped to Development: owns the context workflow, while table and dialog remain renderers. */
@Injectable()
export class ApplicationDevelopmentWebContextsState {
  private readonly detail = inject(ApplicationDetailState);
  private readonly service = inject(ApplicationWebContextsService);
  private readonly messages = inject(MessageService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly locale = inject(LOCALE_ID);
  private readonly cancelPage = new Subject<void>();
  private pageRequestVersion = 0;
  private tableState: TableLazyLoadEvent = { first: 0, rows: PAGINATOR_ROWS };
  private readonly resolvedAnchor = signal<number | null>(null);
  private readonly contextsCatalog = signal<SecurityCatalogItem[]>([]);
  private readonly fieldsCatalog = signal<SecurityCatalogItem[]>([]);

  readonly appSecurityId = computed(() => this.resolvedAnchor() ?? this.detail.appSecurityId());
  readonly items = signal<PaginatedList<ApplicationWebContextOutput>>({ items: [], total: 0 });
  readonly first = signal(0);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly deleting = signal(false);
  readonly sectionBusy = signal(false);
  readonly visible = signal(false);
  readonly mode = signal<CrudEntityDialogMode>('view');
  readonly selected = signal<ApplicationWebContextOutput | null>(null);
  readonly pendingDelete = signal<ApplicationWebContextOutput | null>(null);
  readonly deleteVisible = signal(false);
  readonly form = createApplicationSecurityResourceForm(inject(FormBuilder));
  readonly showActions = computed(
    () => this.detail.canEdit() && this.detail.isEditing('development'),
  );
  readonly canManage = computed(
    () =>
      this.showActions() &&
      this.appSecurityId() != null &&
      !this.sectionBusy() &&
      !this.saving() &&
      !this.deleting(),
  );
  readonly canEdit = computed(() => this.canManage() && !this.selected()?.deletedAt);
  readonly webContextOptions = computed(() =>
    this.options(this.contextsCatalog(), this.selected()?.webContext),
  );
  readonly fieldOptions = computed(() =>
    this.options(this.fieldsCatalog(), this.selected()?.field),
  );

  initialize(data: ApplicationDevelopmentWebContextsData): void {
    this.resolvedAnchor.set(data.appSecurityId);
    this.items.set({ items: data.page?.content ?? [], total: data.page?.totalElements ?? 0 });
    if (data.page) this.detail.updateWebContextCount(data.page.totalElements);
    this.contextsCatalog.set(data.webContextOptions);
    this.fieldsCatalog.set(data.fieldOptions);
    if (data.loadFailed) this.error(APPLICATION_DEVELOPMENT_WEB_CONTEXTS_LOAD_ERROR);
  }

  onAction(event: ActionParams<ApplicationSecurityResourceOutput>): void {
    const row = event.params as ApplicationWebContextOutput;
    if (event.action === ApplicationSecurityTableAction.View) this.open(row, 'view');
    if (event.action === ApplicationSecurityTableAction.Edit && this.canManage() && !row.deletedAt)
      this.open(row, 'edit');
    if (event.action === ApplicationSecurityTableAction.Delete) this.requestDelete(row);
  }

  create(): void {
    if (!this.canManage()) return;
    configureApplicationSecurityResourceForm(this.form, 'web-context');
    this.selected.set(null);
    this.mode.set('create');
    this.visible.set(true);
  }

  edit(): void {
    if (this.canEdit()) this.mode.set('edit');
  }

  cancelEdit(): void {
    const selected = this.selected();
    if (selected) this.open(selected, 'view');
  }

  close(): void {
    this.visible.set(false);
    this.selected.set(null);
    this.form.reset();
  }

  save(): void {
    if (!this.canEdit() || this.mode() === 'view') return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const payload: ApplicationWebContextInput = {
      appSecurityId: this.appSecurityId()!,
      webContextId: value.webContextId!,
      fieldId: value.fieldId!,
      url: value.url.trim() || null,
      observation: value.observation.trim() || null,
    };
    const id = this.selected()?.id;
    const request = id == null ? this.service.create(payload) : this.service.update(id, payload);
    this.saving.set(true);
    request
      .pipe(
        finalize(() => this.saving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.close();
          this.loadPage();
          this.detail.refreshCompletenessAfterMutation();
          this.success(APPLICATION_SECURITY_RESOURCE_SAVE_SUCCESS);
        },
        error: () => this.error(APPLICATION_SECURITY_RESOURCE_SAVE_ERROR),
      });
  }

  requestDelete(row = this.selected()): void {
    if (!this.canManage() || !row || row.deletedAt) return;
    this.pendingDelete.set(row);
    this.deleteVisible.set(true);
  }

  closeDelete(): void {
    this.deleteVisible.set(false);
    this.pendingDelete.set(null);
  }

  confirmDelete(): void {
    const row = this.pendingDelete();
    if (!this.canManage() || !row || row.deletedAt) return;
    this.deleting.set(true);
    this.service
      .delete(row.id)
      .pipe(
        finalize(() => this.deleting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.closeDelete();
          this.close();
          this.loadPage();
          this.detail.refreshCompletenessAfterMutation();
          this.success(APPLICATION_SECURITY_RESOURCE_DELETE_SUCCESS);
        },
        error: () => this.error(APPLICATION_SECURITY_RESOURCE_DELETE_ERROR),
      });
  }

  onPage(event: TableLazyLoadEvent): void {
    this.tableState = event;
    this.first.set(event.first ?? 0);
    this.loadPage();
  }

  private loadPage(): void {
    const appSecurityId = this.appSecurityId();
    if (appSecurityId == null) return;
    const requestVersion = ++this.pageRequestVersion;
    this.cancelPage.next();
    this.loading.set(true);
    const rows = this.tableState.rows ?? PAGINATOR_ROWS;
    const sortField =
      typeof this.tableState.sortField === 'string' ? this.tableState.sortField : 'id';
    this.service
      .getPage({
        appSecurityId,
        page: Math.floor(this.first() / rows),
        size: rows,
        sort: `${sortField},${this.tableState.sortOrder === -1 ? 'desc' : 'asc'}`,
        statusId: SoftDeleteStatus.ACTIVE,
      })
      .pipe(
        takeUntil(this.cancelPage),
        finalize(() => {
          if (requestVersion === this.pageRequestVersion) this.loading.set(false);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (page) => {
          this.detail.updateWebContextCount(page.totalElements);
          if (page.content.length === 0 && page.number > 0) {
            this.onPage({ ...this.tableState, first: (page.number - 1) * rows });
            return;
          }
          this.items.set({ items: page.content, total: page.totalElements });
        },
        error: () => this.error(APPLICATION_DEVELOPMENT_WEB_CONTEXTS_LOAD_ERROR),
      });
  }

  private open(row: ApplicationWebContextOutput, mode: CrudEntityDialogMode): void {
    configureApplicationSecurityResourceForm(this.form, 'web-context');
    this.selected.set(row);
    this.form.patchValue({
      webContextId: row.webContext?.id ?? null,
      fieldId: row.field?.id ?? null,
      url: row.url ?? '',
      observation: row.observation ?? '',
    });
    this.form.markAsPristine();
    this.mode.set(mode);
    this.visible.set(true);
  }

  private options(catalog: SecurityCatalogItem[], selected?: SecurityCatalogItem | null) {
    const items =
      selected && !catalog.some((item) => item.id === selected.id)
        ? [...catalog, selected]
        : catalog;
    return items.map((item) => ({
      value: item.id,
      label: localizedName(item, this.locale, `#${item.id}`),
    }));
  }

  private error(detail: string): void {
    this.messages.add({ severity: 'error', summary: APPLICATION_SECURITY_ERROR_TITLE, detail });
  }
  private success(detail: string): void {
    this.messages.add({ severity: 'success', summary: APPLICATION_SECURITY_SUCCESS_TITLE, detail });
  }
}
