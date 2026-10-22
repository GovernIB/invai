import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  computed,
  DestroyRef,
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
import { TEXT_FIELD_MAX_BYTES_ERROR } from '@shared/constants/text-field.i18n';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { Textarea } from 'primeng/textarea';
import { SelectFilterEvent, SelectPassThrough } from 'primeng/types/select';
import { debounceTime, Subject, switchMap } from 'rxjs';
import { SelectOption } from '../../../../applications.model';
import {
  ApplicationSecurityResourceFormGroup,
  ApplicationSecurityResourceKind,
  ApplicationSecurityRoleOption,
} from '../../../../forms/application-security-form.factory';
import {
  APPLICATION_SECURITY_DIALOG_ARIA_LABELS,
  APPLICATION_SECURITY_DIALOG_TITLES,
  APPLICATION_SECURITY_EMPTY_VALUE,
  APPLICATION_SECURITY_LABELS,
  APPLICATION_SECURITY_REQUIRED_ERROR,
  APPLICATION_SECURITY_ROLE_SEARCH_COPY,
  APPLICATION_SECURITY_ROLE_VALIDATION,
} from './application-security-section.i18n';

export interface ApplicationSecurityRoleSearchState {
  options: ApplicationSecurityRoleOption[];
  loading: boolean;
  searched: boolean;
  error: boolean;
  total: number;
}

export const EMPTY_APPLICATION_SECURITY_ROLE_SEARCH: ApplicationSecurityRoleSearchState = {
  options: [],
  loading: false,
  searched: false,
  error: false,
  total: 0,
};

const ROLE_FILTER_DEBOUNCE_MS = 300;
const ROLE_IDS = {
  input: 'application-security-dialog-role',
  label: 'application-security-dialog-role-label',
  instruction: 'application-security-dialog-role-instruction',
  status: 'application-security-dialog-role-status',
  error: 'application-security-dialog-role-error',
  prefixNote: 'application-security-dialog-role-prefix-note',
} as const;

@Component({
  selector: 'app-application-security-resource-dialog',
  standalone: true,
  imports: [InputText, CrudEntityDialog, ReactiveFormsModule, Select, Textarea],
  templateUrl: './application-security-resource-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationSecurityResourceDialog {
  visible = model(false);
  kind = input.required<ApplicationSecurityResourceKind>();
  form = input.required<ApplicationSecurityResourceFormGroup>();
  mode = input.required<CrudEntityDialogMode>();
  webContextOptions = input<SelectOption<number>[]>([]);
  fieldOptions = input<SelectOption<number>[]>([]);
  securityLevelOptions = input<SelectOption<number>[]>([]);
  measureTypeOptions = input<SelectOption<number>[]>([]);
  ensRequirementOptions = input<SelectOption<number>[]>([]);
  roleSearch = input<ApplicationSecurityRoleSearchState>(EMPTY_APPLICATION_SECURITY_ROLE_SEARCH);
  // Application prefix the back-end validates role names against; blank when unknown.
  rolePrefix = input('');
  isSaving = input(false);
  isDeleting = input(false);
  canEdit = input(false);

  submitForm = output<void>();
  closed = output<void>();
  edit = output<void>();
  cancelEdit = output<void>();
  deactivate = output<void>();
  roleSearchRequested = output<string>();

  private readonly roleFilters = new Subject<string>();

  protected readonly labels = APPLICATION_SECURITY_LABELS;
  protected readonly roleCopy = APPLICATION_SECURITY_ROLE_SEARCH_COPY;
  protected readonly roleIds = ROLE_IDS;
  protected readonly requiredError = APPLICATION_SECURITY_REQUIRED_ERROR;
  protected readonly textMaxBytesError = TEXT_FIELD_MAX_BYTES_ERROR;
  protected readonly emptyValue = APPLICATION_SECURITY_EMPTY_VALUE;
  protected readonly actionAriaLabels = APPLICATION_SECURITY_DIALOG_ARIA_LABELS;
  protected readonly title = computed(
    () => APPLICATION_SECURITY_DIALOG_TITLES[this.kind()][this.mode()],
  );
  protected readonly hasUnsavedChanges = () => this.mode() === 'edit' && this.form().dirty;
  protected readonly rolePanelMessage = computed(() => {
    const search = this.roleSearch();
    if (search.loading) return this.roleCopy.searching;
    if (search.error) return this.roleCopy.error;
    return search.searched ? this.roleCopy.empty : this.roleCopy.instruction;
  });
  protected readonly roleStatusMessage = computed(() => {
    const search = this.roleSearch();
    if (search.loading) return this.roleCopy.searching;
    if (search.error) return this.roleCopy.error;
    if (!search.searched) return '';
    if (search.options.length === 0) return this.roleCopy.empty;
    return search.total > search.options.length
      ? this.roleCopy.refine(search.options.length, search.total)
      : '';
  });

  constructor() {
    const destroyRef = inject(DestroyRef);
    this.roleFilters
      .pipe(debounceTime(ROLE_FILTER_DEBOUNCE_MS), takeUntilDestroyed(destroyRef))
      .subscribe((query) => this.roleSearchRequested.emit(query));

    // The owner validates from outside this view, so repaint the role error on control events.
    const changeDetector = inject(ChangeDetectorRef);
    toObservable(this.form)
      .pipe(
        switchMap((form) => form.events),
        takeUntilDestroyed(destroyRef),
      )
      .subscribe(() => changeDetector.markForCheck());
  }

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected optionLabel(options: SelectOption<number>[], value: number | null): string {
    return options.find((option) => option.value === value)?.label ?? this.emptyValue;
  }

  protected selectedRole(): ApplicationSecurityRoleOption | null {
    return this.form().controls.role.value;
  }

  // Mirrors the back-end check (case-sensitive): a hand-picked role outside the prefix is saved
  // already validated manually on behalf of the current user.
  protected rolePrefixNote(): string {
    const prefix = this.rolePrefix().trim();
    const role = this.selectedRole();
    if (this.mode() === 'view' || !prefix || !role || role.name.startsWith(`${prefix}_`)) return '';
    return APPLICATION_SECURITY_ROLE_VALIDATION.prefixNote(prefix);
  }

  protected rolePassThrough(): SelectPassThrough {
    const invalid = this.isInvalid(this.form().controls.role);
    return {
      label: {
        'aria-describedby': [
          this.roleIds.instruction,
          this.roleIds.status,
          this.roleIds.prefixNote,
          invalid ? this.roleIds.error : null,
        ]
          .filter(Boolean)
          .join(' '),
        'aria-invalid': invalid || undefined,
      },
    };
  }

  protected onRoleFilter(event: SelectFilterEvent): void {
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
