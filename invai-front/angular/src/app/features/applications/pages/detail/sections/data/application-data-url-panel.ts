import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  computed,
  inject,
  input,
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { PrimeIcons } from 'primeng/api';
import { Checkbox } from 'primeng/checkbox';
import { InputText } from 'primeng/inputtext';
import { CheckboxPassThrough } from 'primeng/types/checkbox';
import { merge, switchMap } from 'rxjs';

import { ApplicationDataUrlState } from '../../../../applications.model';
import { APPLICATION_DATA_URL_MAX_LENGTH } from '../../../../forms/application-data-form.factory';
import { APPLICATION_DATA_LABELS, APPLICATION_DATA_URL_MESSAGES } from './application-data-section.i18n';

// Renders the URL of one data source ("Dades obertes" or "Reutilització") and, while editing, the
// checkbox to type in another one. The detail state owns the form and saves it.
@Component({
  selector: 'app-application-data-url-panel',
  standalone: true,
  imports: [Checkbox, InputText, ReactiveFormsModule],
  templateUrl: './application-data-url-panel.html',
  styleUrl: './application-data-url-panel.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationDataUrlPanel {
  title = input.required<string>();
  idPrefix = input.required<string>();
  // URL the source uses and the outcome of checking it; null while the back-end has not given it.
  state = input<ApplicationDataUrlState | null>(null);
  // Shown instead of the URL when `state` is null.
  pendingMessage = input('');
  canEdit = input(false);
  isEditing = input(false);
  manualControl = input.required<FormControl<boolean>>();
  urlControl = input.required<FormControl<string>>();

  protected readonly labels = APPLICATION_DATA_LABELS;
  protected readonly messages = APPLICATION_DATA_URL_MESSAGES;
  protected readonly icons = PrimeIcons;
  protected readonly urlMaxLength = APPLICATION_DATA_URL_MAX_LENGTH;
  protected readonly ids = computed(() => {
    const prefix = this.idPrefix();
    return {
      title: `${prefix}-title`,
      warning: `${prefix}-warning`,
      manual: `${prefix}-manual`,
      url: `${prefix}-url`,
      urlError: `${prefix}-url-error`,
      urlHelp: `${prefix}-url-help`,
    };
  });
  protected readonly warning = computed(() => {
    const check = this.state()?.check;
    if (check !== 'invalid' && check !== 'failed') return null;
    const problem = check === 'invalid' ? this.messages.notResponding : this.messages.notChecked;
    if (this.isEditing()) return `${problem} ${this.messages.manualHintEditing}`;
    return this.canEdit() ? `${problem} ${this.messages.manualHint}` : problem;
  });
  protected readonly checkboxPassThrough = computed<CheckboxPassThrough>(() => ({
    input: { 'aria-describedby': this.warning() ? this.ids().warning : undefined },
  }));

  constructor() {
    // The owner validates and saves from outside this view, so repaint on control events.
    const changeDetector = inject(ChangeDetectorRef);
    merge(toObservable(this.manualControl), toObservable(this.urlControl))
      .pipe(
        switchMap(() => merge(this.manualControl().events, this.urlControl().events)),
        takeUntilDestroyed(),
      )
      .subscribe(() => changeDetector.markForCheck());
  }

  protected urlError(): string | null {
    const control = this.urlControl();
    if (!control.invalid || !(control.dirty || control.touched)) return null;
    if (control.hasError('required')) return this.messages.required;
    if (control.hasError('maxlength')) return this.messages.maxLength;
    return this.messages.pattern;
  }

  protected urlDescribedBy(): string {
    return [this.urlError() ? this.ids().urlError : null, this.ids().urlHelp]
      .filter(Boolean)
      .join(' ');
  }
}
