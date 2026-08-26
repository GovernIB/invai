import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  OnChanges,
  OnInit,
  output,
  SimpleChanges,
  signal,
} from '@angular/core';
import { SectionActionsComponent } from '@components/section-actions/section-actions.component';
import { SearchFiltersComponent } from '@components/search-filters/search-filters.component';
import { ActionParams, KeyLabel, PaginatedList } from '@models/table.model';
import { fnGetVisibleColumns, fnSetColumnVisibility } from '@shared/utils/table.utils';
import { TableLazyLoadEvent } from 'primeng/table';

import {
  ApplicationInfrastructureResource,
  ApplicationInfrastructureStatus,
} from '../../applications.model';
import { ApplicationInfrastructureTable } from '../application-infrastructure-table/application-infrastructure-table';
import { APPLICATION_INFRASTRUCTURE_LIST_ADD_ARIA_LABEL } from './application-infrastructure-list.i18n';

@Component({
  selector: 'app-application-infrastructure-list',
  standalone: true,
  imports: [ApplicationInfrastructureTable, SearchFiltersComponent, SectionActionsComponent],
  templateUrl: './application-infrastructure-list.html',
  styleUrl: './application-infrastructure-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationInfrastructureList implements OnChanges, OnInit {
  title = input.required<string>();
  resourceName = input.required<string>();
  itemsList = input.required<PaginatedList<ApplicationInfrastructureResource>>();
  columns = input.required<KeyLabel[]>();
  appliedStatus = input.required<ApplicationInfrastructureStatus | null>();
  isLoading = input(false);
  isReadOnly = input(false);
  showActions = input(true);
  first = input(0);
  filtersSelected = input(0);
  filtersButtonAriaLabel = input($localize`Mostra o oculta els filtres`);
  pageChange = output<TableLazyLoadEvent>();
  filterSearch = output<void>();
  filterReset = output<void>();
  addRequested = output<void>();
  rowAction = output<ActionParams<ApplicationInfrastructureResource>>();

  protected readonly isFiltersCollapsed = signal(true);
  protected readonly selectedColumns = signal<KeyLabel[]>([]);
  protected readonly selectableColumns = computed(() => this.columns());
  protected readonly visibleColumns = computed(() =>
    fnGetVisibleColumns(this.columns(), this.selectedColumns()),
  );
  protected readonly addAriaLabel = computed(() =>
    APPLICATION_INFRASTRUCTURE_LIST_ADD_ARIA_LABEL(this.resourceName()),
  );
  private initialized = false;
  private appliedStatusValue: ApplicationInfrastructureStatus | null | undefined;

  ngOnChanges(changes: SimpleChanges): void {
    if (!this.initialized || !changes['appliedStatus']) return;
    this.synchronizeStatusColumnSelection();
  }

  ngOnInit(): void {
    this.selectedColumns.set(this.columns());
    this.initialized = true;
    this.synchronizeStatusColumnSelection(true);
  }

  protected onPageChange(event: TableLazyLoadEvent): void {
    this.pageChange.emit(event);
  }

  protected onFilterSearch(): void {
    this.filterSearch.emit();
  }

  protected onFilterReset(): void {
    this.filterReset.emit();
  }

  private synchronizeStatusColumnSelection(force = false): void {
    const status = this.appliedStatus();
    if (!force && Object.is(status, this.appliedStatusValue)) return;

    this.selectedColumns.set(
      fnSetColumnVisibility(this.columns(), this.selectedColumns(), 'status', status == null),
    );
    this.appliedStatusValue = status;
  }

  protected onAdd(): void {
    if (this.isReadOnly()) return;
    this.addRequested.emit();
  }

  protected onTableAction(event: ActionParams<ApplicationInfrastructureResource>): void {
    this.rowAction.emit(event);
  }
}
