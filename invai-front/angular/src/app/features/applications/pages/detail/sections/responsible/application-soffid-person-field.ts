import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import {
  SoffidPersonControlValue,
  SoffidPersonOption,
} from '@features/maintenances/responsibles/responsibles.model';
import { AutoComplete } from 'primeng/autocomplete';
import { FloatLabel } from 'primeng/floatlabel';
import { AutoCompletePassThrough, AutoCompleteSelectEvent } from 'primeng/types/autocomplete';

import { APPLICATION_RESPONSIBLE_COPY } from './application-responsible-section.i18n';

@Component({
  selector: 'app-application-soffid-person-field',
  standalone: true,
  imports: [AutoComplete, FloatLabel, ReactiveFormsModule],
  templateUrl: './application-soffid-person-field.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationSoffidPersonField {
  control = input.required<import('@angular/forms').FormControl<SoffidPersonControlValue>>();
  idPrefix = input.required<string>();
  options = input.required<SoffidPersonOption[]>();
  loading = input(false);
  searched = input(false);
  searchError = input(false);
  total = input(0);

  searchRequested = output<string>();

  protected readonly copy = APPLICATION_RESPONSIBLE_COPY;
  protected readonly inputId = computed(() => `${this.idPrefix()}-soffid-person`);
  protected readonly labelId = computed(() => `${this.inputId()}-label`);
  protected readonly instructionId = computed(() => `${this.inputId()}-instruction`);
  protected readonly statusId = computed(() => `${this.inputId()}-status`);
  protected readonly searchErrorId = computed(() => `${this.inputId()}-search-error`);
  protected readonly errorId = computed(() => `${this.inputId()}-error`);
  protected readonly statusMessage = computed(() => {
    if (this.loading()) return this.copy.soffidSearching;
    if (this.searched() && this.total() === 0) return this.copy.soffidEmpty;
    if (this.total() > 20) return this.copy.soffidRefine(this.total());
    return '';
  });
  protected readonly searchErrorMessage = computed(() =>
    this.searchError() ? this.copy.soffidSearchError : '',
  );

  protected invalid(): boolean {
    const control = this.control();
    return control.invalid && (control.dirty || control.touched);
  }

  protected validationErrorMessage(): string {
    if (!this.invalid()) return '';
    return this.control().hasError('required')
      ? this.copy.soffidRequired
      : this.copy.soffidInvalidSelection;
  }

  protected passThrough(): AutoCompletePassThrough {
    return {
      pcInputText: {
        root: {
          'aria-describedby': [
            this.instructionId(),
            this.statusId(),
            this.searchErrorId(),
            this.errorId(),
          ].join(' '),
          'aria-invalid': this.invalid() || undefined,
        },
      },
    };
  }

  protected onSearch(event: { query: string }): void {
    this.searchRequested.emit(event.query.trim());
  }

  protected onPersonSelected(event: AutoCompleteSelectEvent): void {
    const person = event.value as SoffidPersonOption;
    this.control().setValue(
      {
        ...person,
        label: `${person.firstName} ${person.lastName}`.trim(),
      },
      { emitEvent: false },
    );
  }
}
