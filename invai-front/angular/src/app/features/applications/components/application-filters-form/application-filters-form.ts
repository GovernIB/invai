import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { ResponsiblePersonOption } from '@features/maintenances/responsibles/responsibles.model';
import { AutoComplete } from 'primeng/autocomplete';
import { Button } from 'primeng/button';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { ToggleSwitch } from 'primeng/toggleswitch';

import { APPLICATION_STATUS_OPTIONS } from '../../applications.constants';
import { ApplicationInfrastructureFilterOptions } from '../../applications.model';
import { ApplicationFiltersFormGroup } from '../../forms/application-form.factory';
import { ApplicationSelectOptions } from '../../services/application-options.service';

export interface ApplicationFilterLabels {
  prefix: string;
  application: string;
  category: string;
  informationSystem: string;
  scope: string;
  commission: string;
  conselleria: string;
  administrativeUnit: string;
  departmentsLoading: string;
  departmentsLoadError: string;
  administrativeUnitsLoading: string;
  administrativeUnitsLoadError: string;
  administrativeUnitsEmpty: string;
  selectConselleriaFirst: string;
  retry: string;
  status: string;
  responsible: string;
  database: string;
  server: string;
  environment: string;
  incomplete: string;
  responsibleEmpty: string;
  responsibleLoading: string;
}

@Component({
  selector: 'app-application-filters-form',
  standalone: true,
  imports: [
    AutoComplete,
    Button,
    FloatLabel,
    InputText,
    ReactiveFormsModule,
    SearchFilterGridDirective,
    Select,
    ToggleSwitch,
  ],
  templateUrl: './application-filters-form.html',
  styleUrl: './application-filters-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationFiltersForm {
  form = input.required<ApplicationFiltersFormGroup>();
  labels = input.required<ApplicationFilterLabels>();
  options = input<Partial<ApplicationSelectOptions> | null>(null);
  infrastructureOptions = input<Partial<ApplicationInfrastructureFilterOptions> | null>(null);
  responsibleOptions = input<ResponsiblePersonOption[]>([]);
  responsibleLoading = input(false);
  departmentsLoading = input(false);
  departmentsLoadFailed = input(false);
  administrativeUnitsLoading = input(false);
  administrativeUnitsLoadFailed = input(false);
  responsibleSearch = output<string>();
  conselleriaSelected = output<string | null>();
  departmentsRetry = output<void>();
  administrativeUnitsRetry = output<void>();

  protected readonly statusOptions = APPLICATION_STATUS_OPTIONS;

  protected onResponsibleSearch(event: { query: string }): void {
    this.responsibleSearch.emit(event.query);
  }

  protected onResponsibleClear(): void {
    this.responsibleSearch.emit('');
  }

  protected selectConselleria(code: string | null): void {
    this.conselleriaSelected.emit(code);
  }

  protected conselleriaDescribedBy(): string | null {
    return this.departmentsLoading() || this.departmentsLoadFailed()
      ? 'applications-filter-conselleria-status'
      : null;
  }

  protected conselleriaStatus(): string | null {
    if (this.departmentsLoading()) return this.labels().departmentsLoading;
    if (this.departmentsLoadFailed()) return this.labels().departmentsLoadError;
    return null;
  }

  protected administrativeUnitStatus(): string | null {
    if (this.administrativeUnitsLoading()) return this.labels().administrativeUnitsLoading;
    if (this.administrativeUnitsLoadFailed()) {
      return this.labels().administrativeUnitsLoadError;
    }
    if (!this.form().controls.conselleria.value) return this.labels().selectConselleriaFirst;
    if (!(this.options()?.administrativeUnits?.length ?? 0)) {
      return this.labels().administrativeUnitsEmpty;
    }
    return null;
  }

  protected administrativeUnitDescribedBy(): string | null {
    return this.administrativeUnitStatus()
      ? 'applications-filter-administrative-unit-status'
      : null;
  }
}
