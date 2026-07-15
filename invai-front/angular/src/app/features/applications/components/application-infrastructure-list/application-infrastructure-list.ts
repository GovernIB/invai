import { ChangeDetectionStrategy, Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { SectionActionsComponent } from '@components/section-actions/section-actions.component';
import { ActionParams, KeyLabel, PaginatedList } from '@models/table.model';
import { fnGetVisibleColumns } from '@shared/utils/table.utils';
import { MessageService } from 'primeng/api';
import { TableLazyLoadEvent } from 'primeng/table';

import { ApplicationInfrastructureResource } from '../../applications.model';
import {
  ApplicationInfrastructureTable,
  ApplicationInfrastructureTableAction,
} from '../application-infrastructure-table/application-infrastructure-table';
import {
  APPLICATION_INFRASTRUCTURE_LIST_ADD_ARIA_LABEL,
  APPLICATION_INFRASTRUCTURE_LIST_ADD_PENDING_MESSAGE,
  APPLICATION_INFRASTRUCTURE_LIST_EDIT_PENDING_MESSAGE,
  APPLICATION_INFRASTRUCTURE_LIST_INFO_TITLE,
  APPLICATION_INFRASTRUCTURE_LIST_WITHDRAW_PENDING_MESSAGE,
} from './application-infrastructure-list.i18n';

@Component({
  selector: 'app-application-infrastructure-list',
  standalone: true,
  imports: [ApplicationInfrastructureTable, SectionActionsComponent],
  templateUrl: './application-infrastructure-list.html',
  styleUrl: './application-infrastructure-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationInfrastructureList implements OnInit {
  private readonly messageService = inject(MessageService);
  private readonly pageEvent = signal<TableLazyLoadEvent>({ first: 0, rows: 10 });

  title = input.required<string>();
  resourceName = input.required<string>();
  sourceItems = input.required<ApplicationInfrastructureResource[]>();
  columns = input.required<KeyLabel[]>();

  protected readonly selectedColumns = signal<KeyLabel[]>([]);
  protected readonly selectableColumns = computed(() => this.columns());
  protected readonly visibleColumns = computed(() =>
    fnGetVisibleColumns(this.columns(), this.selectedColumns()),
  );
  protected readonly addAriaLabel = computed(() =>
    APPLICATION_INFRASTRUCTURE_LIST_ADD_ARIA_LABEL(this.resourceName()),
  );
  protected readonly itemsList = computed<PaginatedList<ApplicationInfrastructureResource>>(() => {
    const event = this.pageEvent();
    const sortedItems = this.sortItems(this.sourceItems(), event);
    const first = event.first ?? 0;
    const rows = event.rows ?? 10;

    return {
      items: sortedItems.slice(first, first + rows),
      total: sortedItems.length,
    };
  });

  ngOnInit(): void {
    this.selectedColumns.set(this.columns());
  }

  protected onPageChange(event: TableLazyLoadEvent): void {
    this.pageEvent.set(event);
  }

  protected onAdd(): void {
    this.showPendingMessage(
      APPLICATION_INFRASTRUCTURE_LIST_ADD_PENDING_MESSAGE(this.resourceName()),
    );
  }

  protected onTableAction(event: ActionParams<ApplicationInfrastructureResource>): void {
    const message =
      event.action === ApplicationInfrastructureTableAction.Edit
        ? APPLICATION_INFRASTRUCTURE_LIST_EDIT_PENDING_MESSAGE(this.resourceName())
        : APPLICATION_INFRASTRUCTURE_LIST_WITHDRAW_PENDING_MESSAGE(this.resourceName());

    this.showPendingMessage(message);
  }

  private sortItems(
    items: ApplicationInfrastructureResource[],
    event: TableLazyLoadEvent,
  ): ApplicationInfrastructureResource[] {
    const sortField = Array.isArray(event.sortField) ? event.sortField[0] : event.sortField;
    if (!sortField) return items;

    const direction = event.sortOrder === -1 ? -1 : 1;
    return [...items].sort(
      (left, right) =>
        this.normalize(this.getItemValue(left, sortField)).localeCompare(
          this.normalize(this.getItemValue(right, sortField)),
        ) * direction,
    );
  }

  private getItemValue(item: ApplicationInfrastructureResource, key: string): string {
    const value = (item as unknown as Record<string, unknown>)[key];
    return value == null ? '' : String(value);
  }

  private normalize(value: string): string {
    return value.trim().toLocaleLowerCase();
  }

  private showPendingMessage(detail: string): void {
    this.messageService.add({
      severity: 'info',
      summary: APPLICATION_INFRASTRUCTURE_LIST_INFO_TITLE,
      detail,
    });
  }
}
