import {
  ChangeDetectionStrategy,
  Component,
  ViewEncapsulation,
  computed,
  input,
} from '@angular/core';
import { PrimeIcons } from 'primeng/api';
import { TableModule } from 'primeng/table';
import { TablePassThrough } from 'primeng/types/table';

import { ApplicationDataEndpoint, ApplicationDataParameter } from '../../../../applications.model';
import {
  APPLICATION_DATA_EMPTY_VALUE,
  APPLICATION_DATA_ENDPOINT_COLUMNS,
  APPLICATION_DATA_LABELS,
  APPLICATION_DATA_PARAMETER_COLUMNS,
  APPLICATION_DATA_TABLE_TEXTS,
} from './application-data-section.i18n';

// Read-only display of the GET endpoints published by an application's OpenAPI document.
@Component({
  selector: 'app-application-data-endpoints-table',
  standalone: true,
  imports: [TableModule],
  templateUrl: './application-data-endpoints-table.html',
  styleUrls: [
    '../../../../../../shared/styles/development-maintenance-table.scss',
    './application-data-endpoints-table.scss',
  ],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationDataEndpointsTable {
  endpoints = input.required<ApplicationDataEndpoint[]>();
  idPrefix = input.required<string>();
  label = input.required<string>();
  isLoading = input(false);

  protected readonly columns = APPLICATION_DATA_ENDPOINT_COLUMNS;
  protected readonly parameterColumns = APPLICATION_DATA_PARAMETER_COLUMNS;
  protected readonly texts = APPLICATION_DATA_TABLE_TEXTS;
  protected readonly icons = PrimeIcons;
  protected readonly columnCount = computed(() => this.columns.length + 1);
  protected readonly tablePassThrough = computed<TablePassThrough>(() => ({
    table: { 'aria-label': this.label() },
  }));
  private readonly rowIndexes = computed(
    () => new Map(this.endpoints().map((endpoint, index) => [endpoint, index])),
  );

  // Stable pass-through objects avoid re-binding the nested tables on every check.
  private readonly parameterPassThroughs = computed(
    () =>
      new Map<ApplicationDataEndpoint, TablePassThrough>(
        this.endpoints().map((endpoint) => [
          endpoint,
          { table: { 'aria-label': this.texts.parametersOf(endpoint.path) } },
        ]),
      ),
  );

  protected parametersPassThrough(endpoint: ApplicationDataEndpoint): TablePassThrough | undefined {
    return this.parameterPassThroughs().get(endpoint);
  }

  protected parameters(endpoint: ApplicationDataEndpoint): ApplicationDataParameter[] {
    return endpoint.operation?.parameters ?? [];
  }

  protected expansionId(endpoint: ApplicationDataEndpoint): string {
    return `${this.idPrefix()}-endpoint-${this.rowIndexes().get(endpoint) ?? 0}-parameters`;
  }

  protected cellValue(endpoint: ApplicationDataEndpoint, key: string): string {
    switch (key) {
      case 'path':
        return endpoint.path || APPLICATION_DATA_EMPTY_VALUE;
      case 'summary':
        return endpoint.operation?.summary?.trim() || APPLICATION_DATA_EMPTY_VALUE;
      case 'description':
        return endpoint.operation?.description?.trim() || APPLICATION_DATA_EMPTY_VALUE;
      case 'parameters': {
        const count = this.parameters(endpoint).length;
        return count ? String(count) : this.texts.noParameters;
      }
      default:
        return APPLICATION_DATA_EMPTY_VALUE;
    }
  }

  protected parameterValue(parameter: ApplicationDataParameter, key: string): string {
    switch (key) {
      case 'required':
        return parameter.required ? APPLICATION_DATA_LABELS.yes : APPLICATION_DATA_LABELS.no;
      case 'defaultValue':
        return this.formatDefault(parameter.defaultValue);
      case 'enumValues':
        return parameter.enumValues?.length
          ? parameter.enumValues.join(', ')
          : APPLICATION_DATA_EMPTY_VALUE;
      case 'name':
      case 'in':
      case 'type':
      case 'format':
      case 'description':
        return parameter[key]?.trim() || APPLICATION_DATA_EMPTY_VALUE;
      default:
        return APPLICATION_DATA_EMPTY_VALUE;
    }
  }

  private formatDefault(value: unknown): string {
    if (value === null || value === undefined || value === '') return APPLICATION_DATA_EMPTY_VALUE;
    return typeof value === 'object' ? JSON.stringify(value) : String(value);
  }
}
