import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  input,
  output,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Select } from 'primeng/select';
import { SelectFilterEvent, SelectPassThrough } from 'primeng/types/select';
import { Subject, debounceTime } from 'rxjs';

import { ROLE_TRANSFER_COPY } from '../../responsibles.i18n';
import {
  ResponsiblePersonSearchSource,
  RoleTransferPersonControlValue,
  RoleTransferPersonOption,
} from '../../responsibles.model';

export type RoleTransferPersonFieldKind = 'source' | 'destination';

@Component({
  selector: 'app-role-transfer-person-field',
  standalone: true,
  imports: [ReactiveFormsModule, Select],
  templateUrl: './role-transfer-person-field.html',
  styleUrl: './role-transfer-person-field.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '[attr.aria-busy]': 'loading() || null' },
})
export class RoleTransferPersonField {
  private readonly destroyRef = inject(DestroyRef);
  private readonly filterRequests = new Subject<string>();

  kind = input.required<RoleTransferPersonFieldKind>();
  control = input.required<FormControl<RoleTransferPersonControlValue>>();
  options = input.required<RoleTransferPersonOption[]>();
  loading = input(false);
  searched = input(false);
  searchError = input(false);

  searchRequested = output<string>();

  protected readonly copy = ROLE_TRANSFER_COPY;
  protected readonly inputId = computed(() => `role-transfer-${this.kind()}`);
  protected readonly labelId = computed(() => `${this.inputId()}-label`);
  protected readonly instructionId = computed(() => `${this.inputId()}-instruction`);
  protected readonly statusId = computed(() => `${this.inputId()}-status`);
  protected readonly searchErrorId = computed(() => `${this.inputId()}-search-error`);
  protected readonly errorId = computed(() => `${this.inputId()}-error`);
  protected readonly label = computed(() =>
    this.kind() === 'source' ? this.copy.sourcePerson : this.copy.destinationPerson,
  );
  protected readonly placeholder = computed(() =>
    this.kind() === 'source' ? this.copy.sourcePlaceholder : this.copy.destinationPlaceholder,
  );
  protected readonly instruction = computed(() =>
    this.kind() === 'source' ? this.copy.sourceInstruction : this.copy.destinationInstruction,
  );
  protected readonly searchErrorMessage = computed(() =>
    this.kind() === 'source' ? this.copy.sourceSearchError : this.copy.destinationSearchError,
  );
  protected readonly emptyMessage = computed(() =>
    this.kind() === 'source' ? this.copy.noPeople : this.copy.destinationEmpty,
  );
  protected readonly panelEmptyMessage = computed(() => {
    if (this.loading()) {
      return this.kind() === 'source'
        ? this.copy.sourceSearching
        : this.copy.destinationSearching;
    }
    if (this.searchError()) return this.searchErrorMessage();
    if (!this.searched()) return this.instruction();
    return this.emptyMessage();
  });
  protected readonly statusMessage = computed(() => {
    if (this.loading()) {
      return this.kind() === 'source'
        ? this.copy.sourceSearching
        : this.copy.destinationSearching;
    }
    if (this.searched() && !this.searchError() && this.options().length === 0) {
      return this.emptyMessage();
    }
    return '';
  });

  constructor() {
    this.filterRequests
      .pipe(debounceTime(300), takeUntilDestroyed(this.destroyRef))
      .subscribe((query) => this.searchRequested.emit(query));
  }

  protected invalid(): boolean {
    const control = this.control();
    return control.invalid && (control.dirty || control.touched);
  }

  protected validationErrorMessage(): string {
    if (!this.invalid()) return '';
    const control = this.control();
    if (control.hasError('sourceWithoutLocalId')) return this.copy.sourceUnavailable;
    if (control.hasError('required')) {
      return this.kind() === 'source' ? this.copy.sourceRequired : '';
    }
    return this.kind() === 'source'
      ? this.copy.sourceInvalidSelection
      : this.copy.destinationInvalidSelection;
  }

  protected passThrough(): SelectPassThrough {
    return {
      label: {
        'aria-describedby': [
          this.instructionId(),
          this.statusId(),
          this.searchErrorId(),
          this.errorId(),
        ].join(' '),
        'aria-invalid': this.invalid() || undefined,
      },
    };
  }

  protected onFilter(event: SelectFilterEvent): void {
    this.filterRequests.next(String(event.filter ?? '').trim());
  }

  protected onOpen(): void {
    this.searchRequested.emit('');
  }

  protected sourceLabel(source: ResponsiblePersonSearchSource): string {
    return source === 'database'
      ? this.copy.destinationDatabaseSource
      : this.copy.destinationSoffidSource;
  }
}
