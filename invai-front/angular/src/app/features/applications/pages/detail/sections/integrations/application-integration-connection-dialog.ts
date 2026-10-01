import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  computed,
  inject,
  input,
  model,
  output,
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import {
  CrudEntityDialog,
  CrudEntityDialogMode,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { ExternalSystemCatalogOption } from '@features/external-systems/external-systems.model';
import { SoffidPersonOption } from '@features/maintenances/responsibles/responsibles.model';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { MultiSelect } from 'primeng/multiselect';
import { RadioButton } from 'primeng/radiobutton';
import { Select } from 'primeng/select';
import { MultiSelectFilterEvent, MultiSelectPassThrough } from 'primeng/types/multiselect';
import { SelectFilterEvent, SelectPassThrough } from 'primeng/types/select';
import { Subject, debounceTime, switchMap } from 'rxjs';

import { ApplicationIntegrationConnectionOutput } from '../../../../applications.model';
import {
  ApplicationIntegrationConnectionFormGroup,
  ApplicationIntegrationRoleOption,
  ApplicationIntegrationSystemKind,
} from '../../../../forms/application-integration-connection-form.factory';
import { ApplicationSoffidPersonField } from '../responsible/application-soffid-person-field';
import { ApplicationIntegrationApplicationOption } from './application-integration-connections-state';
import {
  APPLICATION_INTEGRATION_DIALOG_ARIA_LABELS,
  APPLICATION_INTEGRATION_DIALOG_LABELS,
  APPLICATION_INTEGRATION_DIALOG_MESSAGES,
  APPLICATION_INTEGRATION_DIALOG_TITLES,
  APPLICATION_INTEGRATIONS_EMPTY_VALUE,
  APPLICATION_INTEGRATIONS_ROLES_TEXTS,
  APPLICATION_INTEGRATIONS_SYSTEM_KINDS,
} from './application-integrations-section.i18n';
import {
  connectionSystemKind,
  connectionSystemName,
  grantedRoleNames,
  rolesMatch,
  soffidRoleNames,
} from './application-integrations-table';

export interface ApplicationIntegrationSearchView<TOption> {
  options: TOption[];
  loading: boolean;
  searched: boolean;
  error: boolean;
  total: number;
}

const ID = 'application-integration-dialog';
const FILTER_DEBOUNCE_MS = 300;

@Component({
  selector: 'app-application-integration-connection-dialog',
  standalone: true,
  imports: [
    ApplicationSoffidPersonField,
    CrudEntityDialog,
    FloatLabel,
    InputText,
    MultiSelect,
    RadioButton,
    ReactiveFormsModule,
    Select,
  ],
  templateUrl: './application-integration-connection-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationIntegrationConnectionDialog {
  private readonly applicationFilters = new Subject<string>();
  private readonly roleFilters = new Subject<string>();

  visible = model(false);
  form = input.required<ApplicationIntegrationConnectionFormGroup>();
  mode = input.required<CrudEntityDialogMode>();
  selected = input<ApplicationIntegrationConnectionOutput | null>(null);
  canEdit = input(false);
  isSaving = input(false);
  isDeleting = input(false);
  catalogsLoading = input(false);
  technologyOptions = input.required<{ id: number; label: string }[]>();
  externalSystemOptions = input.required<ExternalSystemCatalogOption[]>();
  applicationSearch = input.required<ApplicationIntegrationSearchView<ApplicationIntegrationApplicationOption>>();
  userSearch = input.required<ApplicationIntegrationSearchView<SoffidPersonOption>>();
  roleSearch = input.required<ApplicationIntegrationSearchView<ApplicationIntegrationRoleOption>>();

  submitForm = output<void>();
  closed = output<void>();
  edit = output<void>();
  cancelEdit = output<void>();
  deactivate = output<void>();
  applicationSearchRequested = output<string>();
  userSearchRequested = output<string>();
  roleSearchRequested = output<string>();

  protected readonly ids = {
    systemKindLegend: `${ID}-system-kind-legend`,
    kindApplication: `${ID}-kind-application`,
    kindExternal: `${ID}-kind-external`,
    application: `${ID}-application`,
    applicationLabel: `${ID}-application-label`,
    applicationInstruction: `${ID}-application-instruction`,
    applicationStatus: `${ID}-application-status`,
    applicationError: `${ID}-application-error`,
    externalSystem: `${ID}-external-system`,
    externalSystemLabel: `${ID}-external-system-label`,
    externalSystemError: `${ID}-external-system-error`,
    technology: `${ID}-technology`,
    technologyLabel: `${ID}-technology-label`,
    technologyError: `${ID}-technology-error`,
    roles: `${ID}-roles`,
    rolesLabel: `${ID}-roles-label`,
    rolesInstruction: `${ID}-roles-instruction`,
    rolesStatus: `${ID}-roles-status`,
    rolesError: `${ID}-roles-error`,
    view: (field: string) => `${ID}-${field}-view`,
    viewLabel: (field: string) => `${ID}-${field}-view-label`,
  };
  protected readonly idPrefix = ID;
  protected readonly title = computed(() => APPLICATION_INTEGRATION_DIALOG_TITLES[this.mode()]);
  protected readonly labels = APPLICATION_INTEGRATION_DIALOG_LABELS;
  protected readonly messages = APPLICATION_INTEGRATION_DIALOG_MESSAGES;
  protected readonly kinds = APPLICATION_INTEGRATIONS_SYSTEM_KINDS;
  protected readonly rolesTexts = APPLICATION_INTEGRATIONS_ROLES_TEXTS;
  protected readonly emptyValue = APPLICATION_INTEGRATIONS_EMPTY_VALUE;
  protected readonly actionAriaLabels = APPLICATION_INTEGRATION_DIALOG_ARIA_LABELS;
  protected readonly hasUnsavedChanges = () =>
    this.mode() !== 'view' && this.form().dirty;
  protected readonly systemKinds: ApplicationIntegrationSystemKind[] = ['application', 'external'];

  protected readonly applicationPanelMessage = computed(() => {
    const search = this.applicationSearch();
    if (search.loading) return this.messages.applicationsSearching;
    if (search.error) return this.messages.applicationsError;
    return search.searched ? this.messages.applicationsEmpty : this.messages.applicationInstruction;
  });
  protected readonly applicationStatusMessage = computed(() => {
    const search = this.applicationSearch();
    if (search.loading) return this.messages.applicationsSearching;
    if (search.error) return this.messages.applicationsError;
    return search.searched && search.options.length === 0 ? this.messages.applicationsEmpty : '';
  });
  protected readonly rolePanelMessage = computed(() => {
    const search = this.roleSearch();
    if (search.loading) return this.messages.rolesSearching;
    if (search.error) return this.messages.rolesError;
    return search.searched ? this.messages.rolesEmpty : this.messages.rolesInstruction;
  });
  protected readonly roleStatusMessage = computed(() => {
    const search = this.roleSearch();
    if (search.loading) return this.messages.rolesSearching;
    if (search.error) return this.messages.rolesError;
    return search.searched && search.options.length === 0 ? this.messages.rolesEmpty : '';
  });

  // View mode renders the persisted row, never the editable draft.
  protected readonly view = computed(() => {
    const row = this.selected();
    if (!row) return null;
    return {
      system: connectionSystemName(row),
      systemKind: connectionSystemKind(row) || this.emptyValue,
      technology: row.technology?.name?.trim() || this.emptyValue,
      user: row.username?.trim() || this.emptyValue,
      requiredRoles: soffidRoleNames(row.requiredRoles),
      grantedRoles: grantedRoleNames(row),
      rolesMatch: this.rolesTexts[rolesMatch(row)],
    };
  });

  constructor() {
    const destroyRef = inject(DestroyRef);
    this.applicationFilters
      .pipe(debounceTime(FILTER_DEBOUNCE_MS), takeUntilDestroyed(destroyRef))
      .subscribe((query) => this.applicationSearchRequested.emit(query));
    this.roleFilters
      .pipe(debounceTime(FILTER_DEBOUNCE_MS), takeUntilDestroyed(destroyRef))
      .subscribe((query) => this.roleSearchRequested.emit(query));

    // The owner validates and saves from outside this view, so repaint on control events.
    const changeDetector = inject(ChangeDetectorRef);
    toObservable(this.form)
      .pipe(
        switchMap((form) => form.events),
        takeUntilDestroyed(destroyRef),
      )
      .subscribe(() => changeDetector.markForCheck());
  }

  protected isExternal(): boolean {
    return this.form().controls.systemKind.value === 'external';
  }

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected selectPassThrough(
    control: AbstractControl,
    errorId: string,
    describedBy: string[] = [],
  ): SelectPassThrough {
    const invalid = this.isInvalid(control);
    const ids = [...describedBy, invalid ? errorId : null].filter(Boolean);
    return {
      label: {
        'aria-describedby': ids.length ? ids.join(' ') : undefined,
        'aria-invalid': invalid || undefined,
      },
    };
  }

  // The focusable combobox of p-multiselect is its hidden input, not typed by PrimeNG.
  protected rolesPassThrough(): MultiSelectPassThrough {
    const invalid = this.isInvalid(this.form().controls.requiredRoles);
    return {
      hiddenInput: {
        'aria-describedby': [
          this.ids.rolesInstruction,
          this.ids.rolesStatus,
          invalid ? this.ids.rolesError : null,
        ]
          .filter(Boolean)
          .join(' '),
        'aria-invalid': invalid || undefined,
      },
    } as MultiSelectPassThrough;
  }

  protected onApplicationFilter(event: SelectFilterEvent): void {
    this.applicationFilters.next(String(event.filter ?? '').trim());
  }

  protected onApplicationPanelOpen(): void {
    this.applicationSearchRequested.emit('');
  }

  protected onRoleFilter(event: MultiSelectFilterEvent): void {
    this.roleFilters.next(String(event.filter ?? '').trim());
  }

  protected onRolePanelOpen(): void {
    this.roleSearchRequested.emit('');
  }

  protected onSubmit(): void {
    if (!this.isSaving() && !this.isDeleting() && this.mode() !== 'view') {
      this.submitForm.emit();
    }
  }
}
