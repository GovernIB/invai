import { StatusTagComponent } from '@components/status-tag/status-tag.component';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  signal,
  ViewEncapsulation,
} from '@angular/core';
import { TableComponentBase } from '@shared/classes/table-component-base';
import { Application, ApplicationStatus } from '../../applications.model';
import { Skeleton } from 'primeng/skeleton';
import { MenuItem, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Menu } from 'primeng/menu';
import { TableModule } from 'primeng/table';
import { APPLICATION_STATUS_LABELS } from '../../applications.constants';
import { APPLICATION_TABLE_LABELS } from './applications-table.i18n';

export enum ApplicationTableAction {
  Detail = 1,
  Edit,
}

@Component({
  selector: 'app-applications-table',
  standalone: true,
  imports: [StatusTagComponent, Skeleton, TableModule, Button, Menu],
  templateUrl: './applications-table.html',
  styleUrls: [
    '../../../../shared/styles/development-maintenance-table.scss',
    './applications-table.scss',
  ],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationsTable extends TableComponentBase<Application> {
  first = input(0);
  isInitialLoading = input(false);

  private readonly selectedRow = signal<Application | null>(null);
  protected readonly icons = PrimeIcons;
  protected readonly labels = APPLICATION_TABLE_LABELS;
  protected readonly menuVisible = signal(false);
  protected readonly selectedApplicationId = computed(() => this.selectedRow()?.id);
  protected readonly rowActions = computed<MenuItem[]>(() => [
    {
      label: this.labels.view,
      icon: PrimeIcons.EYE,
      command: () => this.emitRowAction(ApplicationTableAction.Detail),
    },
    {
      label: this.labels.edit,
      icon: PrimeIcons.PENCIL,
      disabled: this.selectedRow()?.status === ApplicationStatus.INACTIVE,
      command: () => this.emitRowAction(ApplicationTableAction.Edit),
    },
  ]);

  protected readonly activeStatus = ApplicationStatus.ACTIVE;
  protected readonly ApplicationTableAction = ApplicationTableAction;
  protected readonly applicationStatusLabels = APPLICATION_STATUS_LABELS;
  protected readonly skeletonRows = Array.from({ length: this.PAGINATOR_ROWS });
  protected readonly isRefreshing = computed(
    () => this.isLoading() && !this.isInitialLoading(),
  );

  protected getApplicationStatusLabel(application: Application): string {
    return application.status ? this.applicationStatusLabels[application.status] : '';
  }

  protected openActionsMenu(event: Event, row: Application, menu: Menu): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  private emitRowAction(action: ApplicationTableAction): void {
    const row = this.selectedRow();
    if (
      !row ||
      (action === ApplicationTableAction.Edit && row.status === ApplicationStatus.INACTIVE)
    ) return;

    this.onSelectedAction(action, row);
  }
}
