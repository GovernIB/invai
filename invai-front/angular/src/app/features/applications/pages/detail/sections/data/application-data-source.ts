import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  computed,
  inject,
  input,
  output,
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { ToggleSwitch } from 'primeng/toggleswitch';
import { merge, switchMap } from 'rxjs';

import { ApplicationDataEndpoint } from '../../../../applications.model';
import { APPLICATION_DATA_URL_MAX_LENGTH } from '../../../../forms/application-data-form.factory';
import { ApplicationDataEndpointsTable } from './application-data-endpoints-table';
import {
  APPLICATION_DATA_LABELS,
  APPLICATION_DATA_MESSAGES,
  APPLICATION_DATA_URL_MESSAGES,
} from './application-data-section.i18n';

// Renders one data source (Open Data or Reutilització); the detail state owns the form.
@Component({
  selector: 'app-application-data-source',
  standalone: true,
  imports: [ApplicationDataEndpointsTable, Button, InputText, ReactiveFormsModule, ToggleSwitch],
  templateUrl: './application-data-source.html',
  styleUrl: './application-data-source.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationDataSourcePanel {
  useUrlControl = input.required<FormControl<boolean>>();
  urlControl = input.required<FormControl<string>>();
  endpoints = input<ApplicationDataEndpoint[] | null>(null);
  showEndpoints = input(true);
  // True when the backend could not fetch this source while the other one succeeded.
  unavailable = input(false);
  isEditing = input(false);
  isLoading = input(false);
  idPrefix = input.required<string>();
  title = input.required<string>();
  readonly retry = output<void>();

  protected readonly labels = APPLICATION_DATA_LABELS;
  protected readonly urlMessages = APPLICATION_DATA_URL_MESSAGES;
  protected readonly absentEndpoints = APPLICATION_DATA_MESSAGES.absentEndpoints;
  protected readonly unavailableMessage = APPLICATION_DATA_MESSAGES.sourceUnavailable;
  protected readonly retryLabel = APPLICATION_DATA_MESSAGES.retry;
  protected readonly icons = PrimeIcons;
  protected readonly urlMaxLength = APPLICATION_DATA_URL_MAX_LENGTH;
  protected readonly endpointsLabel = computed(() => `${this.title()}: ${this.labels.endpoints}`);
  protected readonly ids = computed(() => {
    const prefix = this.idPrefix();
    return {
      useUrl: `${prefix}-use-url`,
      useUrlLabel: `${prefix}-use-url-label`,
      url: `${prefix}-url`,
      urlError: `${prefix}-url-error`,
      urlHelp: `${prefix}-url-help`,
      endpointsTitle: `${prefix}-endpoints-title`,
      unavailable: `${prefix}-unavailable`,
    };
  });

  constructor() {
    // The owner validates and saves from outside this view, so repaint on control events.
    const changeDetector = inject(ChangeDetectorRef);
    merge(toObservable(this.useUrlControl), toObservable(this.urlControl))
      .pipe(
        switchMap(() => merge(this.useUrlControl().events, this.urlControl().events)),
        takeUntilDestroyed(),
      )
      .subscribe(() => changeDetector.markForCheck());
  }

  protected isUrlReadonly(): boolean {
    return !this.isEditing() || !this.useUrlControl().value;
  }

  protected showDetectedHelp(): boolean {
    return this.isEditing() && !this.useUrlControl().value;
  }

  protected urlError(): string | null {
    const control = this.urlControl();
    if (!control.invalid || !(control.dirty || control.touched)) return null;
    if (control.hasError('required')) return this.urlMessages.required;
    if (control.hasError('maxlength')) return this.urlMessages.maxLength;
    return this.urlMessages.pattern;
  }

  protected urlDescribedBy(): string | null {
    const ids = [
      this.urlError() ? this.ids().urlError : null,
      this.showDetectedHelp() ? this.ids().urlHelp : null,
    ].filter(Boolean);
    return ids.length ? ids.join(' ') : null;
  }
}
