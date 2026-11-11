import {
  ChangeDetectionStrategy,
  Component,
  input,
  output,
  ViewEncapsulation,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PaginatedList } from '@models/table.model';
import {
  CURRENT_PAGE_REPORT_TEMPLATE,
  PAGINATOR_ROWS,
  PAGINATOR_STYLE_CLASS,
  ROWS_PER_PAGE_OPTIONS,
} from '@shared/constants/table.constants';
import { Checkbox } from 'primeng/checkbox';
import { Select } from 'primeng/select';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';

import { ApplicationAttachment } from '../../../../application-documentation.model';
import {
  ApplicationDocumentationEmailSelection,
  ApplicationDocumentationEmailVersionChange,
} from './application-documentation-email.types';
import { APPLICATION_DOCUMENTATION_EMAIL_TEXTS } from './application-documentation.i18n';

interface VersionOption {
  value: number;
  label: string;
}

// The row reacts to a click or Enter on itself, not on the controls it holds; PrimeNG wraps
// the select and the checkbox in elements without a role, so their hosts count as controls.
const INTERACTIVE_DESCENDANTS =
  'button, a, input, select, textarea, p-select, p-checkbox, [role="button"], [role="combobox"], [role="listbox"], [role="option"]';

// Selectable catalog of the attachments of an application in the e-mail dialog: a checkbox per
// document, a click or Enter on the row toggles it, and a chosen document shows the version that
// goes with it. A document without a current version has nothing to send and can't be chosen.
// Paging and the selection belong to the dialog.
@Component({
  selector: 'app-application-documentation-email-table',
  standalone: true,
  imports: [Checkbox, FormsModule, Select, TableModule],
  templateUrl: './application-documentation-email-table.html',
  styleUrl: './application-documentation-email-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationDocumentationEmailTable {
  itemsList = input.required<PaginatedList<ApplicationAttachment>>();
  selection = input.required<ReadonlyMap<number, ApplicationDocumentationEmailSelection>>();
  first = input(0);
  isLoading = input(false);

  // The document whose selection the user changed.
  selectionChange = output<ApplicationAttachment>();
  versionChange = output<ApplicationDocumentationEmailVersionChange>();
  pageChange = output<TableLazyLoadEvent>();

  protected readonly texts = APPLICATION_DOCUMENTATION_EMAIL_TEXTS;
  protected readonly paginatorRows = PAGINATOR_ROWS;
  protected readonly rowsPerPageOptions = ROWS_PER_PAGE_OPTIONS;
  protected readonly currentPageReportTemplate = CURRENT_PAGE_REPORT_TEMPLATE;
  protected readonly paginatorStyleClass = PAGINATOR_STYLE_CLASS;

  protected isSelectable(row: ApplicationAttachment): boolean {
    return row.currentAttachmentId != null;
  }

  protected onRowToggle(event: Event, row: ApplicationAttachment): void {
    const target = event.target;
    if (
      target instanceof Element &&
      target !== event.currentTarget &&
      target.closest(INTERACTIVE_DESCENDANTS)
    ) {
      return;
    }
    if (event instanceof KeyboardEvent) event.preventDefault();
    if (this.isSelectable(row)) this.selectionChange.emit(row);
  }

  // The versions of the document once read; until then, or without them, the current one.
  protected versionOptions(
    row: ApplicationAttachment,
    selected: ApplicationDocumentationEmailSelection,
  ): VersionOption[] {
    if (selected.versions?.length) {
      return selected.versions.map(({ id, label }) => ({ value: id, label }));
    }
    return [{ value: selected.versionId, label: row.versionLabel ?? '—' }];
  }

  protected onVersionChange(documentId: number, versionId: number): void {
    this.versionChange.emit({ documentId, versionId });
  }
}
