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

import { ApplicationLink } from '../../../../application-documentation.model';
import {
  APPLICATION_DOCUMENTATION_TEXTS,
  APPLICATION_LINKS_TEXTS,
} from './application-documentation.i18n';
import { ApplicationDocumentationAction } from './application-documentation-table-action';
import { documentationLabel } from './application-documentation.utils';

// Paginated links of an application. The URL opens in a new tab; deleting a link requires
// permission to manage the application.
@Component({
  selector: 'app-application-links-table',
  standalone: true,
  imports: [Button, Menu, Skeleton, TableModule],
  templateUrl: './application-links-table.html',
  styleUrls: ['../../../../../../shared/styles/development-maintenance-table.scss'],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationLinksTable extends TableComponentBase<ApplicationLink> {
  first = input(0);
  isInitialLoading = input(false);
  canManage = input(false);

  protected readonly texts = APPLICATION_LINKS_TEXTS;
  protected readonly common = APPLICATION_DOCUMENTATION_TEXTS;
  protected readonly icons = PrimeIcons;
  protected readonly Action = ApplicationDocumentationAction;
  protected readonly skeletonRows = Array.from({ length: this.PAGINATOR_ROWS });
  protected readonly isRefreshing = computed(() => this.isLoading() && !this.isInitialLoading());
  protected readonly selectedRow = signal<ApplicationLink | null>(null);
  protected readonly menuVisible = signal(false);
  protected readonly rowActions = computed(() => this.actionsFor(this.selectedRow()));
  private readonly locale = inject(LOCALE_ID);

  protected cellText(row: ApplicationLink, key: string): string {
    if (key === 'section') return documentationLabel(row.section, this.locale);
    const value = row[key as keyof ApplicationLink];
    return value == null || value === '' ? '—' : String(value);
  }

  protected openActionsMenu(event: Event, row: ApplicationLink, menu: Menu): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  private actionsFor(row: ApplicationLink | null): MenuItem[] {
    if (!row) return [];
    return [
      this.action(this.common.view, PrimeIcons.EYE, ApplicationDocumentationAction.View),
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
