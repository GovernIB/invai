import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  LOCALE_ID,
  signal,
  ViewEncapsulation,
} from '@angular/core';
import { TableComponentBase } from '@shared/classes/table-component-base';
import { MenuItem, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Menu } from 'primeng/menu';
import { Skeleton } from 'primeng/skeleton';
import { TableModule } from 'primeng/table';

import { ApplicationAttachment } from '../../../../application-documentation.model';
import { ApplicationAttachmentStateTag } from './application-attachment-state-tag';
import {
  APPLICATION_ATTACHMENTS_TEXTS,
  APPLICATION_DOCUMENTATION_TEXTS,
} from './application-documentation.i18n';
import { ApplicationDocumentationAction } from './application-documentation-table-action';
import { documentationLabel } from './application-documentation.utils';

// Paginated attachments of an application. Every attachment can be consulted and its current
// version downloaded; deleting one requires permission to manage the application.
@Component({
  selector: 'app-application-attachments-table',
  standalone: true,
  imports: [ApplicationAttachmentStateTag, Button, Menu, Skeleton, TableModule],
  templateUrl: './application-attachments-table.html',
  styleUrls: ['../../../../../../shared/styles/development-maintenance-table.scss'],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationAttachmentsTable extends TableComponentBase<ApplicationAttachment> {
  first = input(0);
  isInitialLoading = input(false);
  canManage = input(false);

  private readonly locale = inject(LOCALE_ID);
  protected readonly texts = APPLICATION_ATTACHMENTS_TEXTS;
  protected readonly common = APPLICATION_DOCUMENTATION_TEXTS;
  protected readonly icons = PrimeIcons;
  protected readonly Action = ApplicationDocumentationAction;
  protected readonly skeletonRows = Array.from({ length: this.PAGINATOR_ROWS });
  protected readonly isRefreshing = computed(() => this.isLoading() && !this.isInitialLoading());
  protected readonly selectedRow = signal<ApplicationAttachment | null>(null);
  protected readonly menuVisible = signal(false);
  protected readonly rowActions = computed(() => this.actionsFor(this.selectedRow()));

  protected cellText(row: ApplicationAttachment, key: string): string {
    switch (key) {
      case 'section':
      case 'category':
      case 'subcategory':
        return documentationLabel(row[key], this.locale);
      case 'mimeType':
        return row.mimeType?.name || '—';
      default: {
        const value = row[key as keyof ApplicationAttachment];
        return value == null || value === '' ? '—' : String(value);
      }
    }
  }

  protected openActionsMenu(event: Event, row: ApplicationAttachment, menu: Menu): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  private actionsFor(row: ApplicationAttachment | null): MenuItem[] {
    if (!row) return [];
    return [
      this.action(this.common.view, PrimeIcons.EYE, ApplicationDocumentationAction.View),
      ...(row.currentAttachmentId == null
        ? []
        : [this.action(this.common.download, PrimeIcons.DOWNLOAD, ApplicationDocumentationAction.Download)]),
      ...(this.canManage()
        ? [this.action(this.common.delete, PrimeIcons.TRASH, ApplicationDocumentationAction.Delete)]
        : []),
    ];
  }

  private action(label: string, icon: string, action: ApplicationDocumentationAction): MenuItem {
    return {
      label,
      icon,
      command: () => {
        const row = this.selectedRow();
        if (row) this.onSelectedAction(action, row);
      },
    };
  }
}
