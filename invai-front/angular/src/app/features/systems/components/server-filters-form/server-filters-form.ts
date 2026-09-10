import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';

import { ServerFiltersFormGroup } from '../../forms/server-filters-form.factory';
import { INFRASTRUCTURE_STATUS_OPTIONS } from '../../systems.constants';
import { ServerCatalogOption } from '../../systems.model';

export interface ServerFilterLabels {
  server: string;
  instance: string;
  version: string;
  status: string;
}

@Component({
  selector: 'app-server-filters-form',
  standalone: true,
  imports: [FloatLabel, InputText, ReactiveFormsModule, SearchFilterGridDirective, Select],
  templateUrl: './server-filters-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ServerFiltersForm {
  form = input.required<ServerFiltersFormGroup>();
  labels = input.required<ServerFilterLabels>();
  serverOptions = input.required<ServerCatalogOption[]>();
  showStatus = input(true);
  idPrefix = input('systems-filter');

  protected readonly statusOptions = INFRASTRUCTURE_STATUS_OPTIONS;
}
