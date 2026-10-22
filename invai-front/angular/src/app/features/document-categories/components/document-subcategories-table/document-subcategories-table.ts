import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  signal,
  ViewEncapsulation,
} from '@angular/core';
import { TableComponentBase } from '@shared/classes/table-component-base';
import { MenuItem, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Menu } from 'primeng/menu';
import { TableModule } from 'primeng/table';

import { DocumentCategory } from '../../document-categories.model';
import { DocumentCategoryTableAction } from '../document-categories-table/document-categories-table';
import { DOCUMENT_CATEGORIES_TABLE_TEXTS } from '../document-categories-table/document-categories-table.i18n';
import { DOCUMENT_SUBCATEGORIES_TABLE_TEXTS } from './document-subcategories-table.i18n';

// Subcategories of the category shown in its detail, sorted in the browser. They offer the same
// row actions as in the list of categories.
@Component({
  selector: 'app-document-subcategories-table',
  standalone: true,
  imports: [Button, Menu, TableModule],
  templateUrl: './document-subcategories-table.html',
  styleUrls: ['../../../../shared/styles/development-maintenance-table.scss'],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DocumentSubcategoriesTable extends TableComponentBase<DocumentCategory> {
  categoryName = input.required<string>();
  // A subcategory can only be restored while its category is active.
  isCategoryActive = input(true);

  protected readonly texts = DOCUMENT_SUBCATEGORIES_TABLE_TEXTS;
  protected readonly rowTexts = DOCUMENT_CATEGORIES_TABLE_TEXTS;
  protected readonly icons = PrimeIcons;
  protected readonly Action = DocumentCategoryTableAction;
  protected readonly selectedRow = signal<DocumentCategory | null>(null);
  protected readonly menuVisible = signal(false);
  protected readonly rowActions = computed(() => this.actionsFor(this.selectedRow()));
  protected readonly tablePassThrough = computed(() => ({
    table: { 'aria-label': this.texts.caption(this.categoryName()) },
  }));

  protected actionsLabel(row: DocumentCategory): string {
    return this.rowTexts.subcategoryActions(row.name?.trim() || `#${row.id}`);
  }

  protected openActionsMenu(event: Event, row: DocumentCategory, menu: Menu): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  protected cellText(row: DocumentCategory, key: string): string | null {
    return key === 'name' || key === 'nameEs' ? row[key] : null;
  }

  private actionsFor(row: DocumentCategory | null): MenuItem[] {
    if (!row) return [];
    const view = this.action(this.rowTexts.view, PrimeIcons.EYE, DocumentCategoryTableAction.View);
    if (row.deletedAt) {
      return this.isCategoryActive()
        ? [
            view,
            this.action(
              this.rowTexts.restore,
              PrimeIcons.REFRESH,
              DocumentCategoryTableAction.Restore,
            ),
          ]
        : [view];
    }
    return [
      view,
      this.action(this.rowTexts.edit, PrimeIcons.PENCIL, DocumentCategoryTableAction.Edit),
      this.action(this.rowTexts.delete, PrimeIcons.TRASH, DocumentCategoryTableAction.Delete),
    ];
  }

  private action(label: string, icon: string, action: DocumentCategoryTableAction): MenuItem {
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
