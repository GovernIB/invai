import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  ViewEncapsulation,
} from '@angular/core';
import { TableComponentBase } from '@shared/classes/table-component-base';
import { Application } from '../../applications.model';
import { Skeleton } from 'primeng/skeleton';
import { TableModule } from 'primeng/table';
import { APPLICATION_STATUS_LABELS } from '../../applications.constants';

export enum ApplicationTableAction {
  Detail = 1,
}

@Component({
  selector: 'app-applications-table',
  standalone: true,
  imports: [Skeleton, TableModule],
  templateUrl: './applications-table.html',
  styleUrl: './applications-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationsTable extends TableComponentBase<Application> {
  first = input(0);
  isInitialLoading = input(false);

  protected readonly ApplicationTableAction = ApplicationTableAction;
  protected readonly applicationStatusLabels = APPLICATION_STATUS_LABELS;
  protected readonly skeletonRows = Array.from({ length: this.PAGINATOR_ROWS });
  protected readonly isRefreshing = computed(
    () => this.isLoading() && !this.isInitialLoading(),
  );

  protected getApplicationStatusLabel(application: Application): string {
    return application.status ? this.applicationStatusLabels[application.status] : '';
  }
}
