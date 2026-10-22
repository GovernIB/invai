import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  input,
  signal,
  untracked,
  viewChild,
  ViewEncapsulation,
} from '@angular/core';
import { TableComponentBase } from '@shared/classes/table-component-base';
import { MenuItem, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Menu } from 'primeng/menu';
import { Skeleton } from 'primeng/skeleton';
import { Table, TableModule } from 'primeng/table';

import { DocumentCategory } from '../../document-categories.model';
import { DOCUMENT_CATEGORIES_TABLE_TEXTS } from './document-categories-table.i18n';

export enum DocumentCategoryTableAction {
  View = 1,
  Edit,
  Delete,
  Restore,
}

// Paginated categories; each one expands into rows of its subcategories under the same columns,
// which share the row actions of the categories (subcategories cannot have subcategories).
@Component({
  selector: 'app-document-categories-table',
  standalone: true,
  imports: [Button, Menu, Skeleton, TableModule],
  templateUrl: './document-categories-table.html',
  styleUrls: [
    '../../../../shared/styles/development-maintenance-table.scss',
    './document-categories-table.scss',
  ],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DocumentCategoriesTable extends TableComponentBase<DocumentCategory> {
  first = input(0);
  isInitialLoading = input(false);
  // While a text search is applied every category found is expanded, since the match may be one of
  // its subcategories.
  expandAll = input(false);

  private readonly table = viewChild.required<Table>('table');
  private readonly categoriesById = computed(
    () => new Map(this.value().map((category) => [category.id, category])),
  );

  protected readonly texts = DOCUMENT_CATEGORIES_TABLE_TEXTS;
  protected readonly icons = PrimeIcons;
  protected readonly Action = DocumentCategoryTableAction;
  protected readonly skeletonRows = Array.from({ length: this.PAGINATOR_ROWS });
  protected readonly isRefreshing = computed(() => this.isLoading() && !this.isInitialLoading());
  protected readonly expandedRowKeys = signal<Record<string, boolean>>({});
  protected readonly selectedRow = signal<DocumentCategory | null>(null);
  protected readonly menuVisible = signal(false);
  protected readonly rowActions = computed(() => this.actionsFor(this.selectedRow()));

  constructor() {
    super();
    // A refreshed page keeps the categories already expanded, so another page starts collapsed;
    // while a text search is applied, every category found that has subcategories is expanded.
    effect(() => {
      const categories = this.value();
      const expandAll = this.expandAll();
      untracked(() => {
        const expanded = this.expandedRowKeys();
        this.expandedRowKeys.set(
          Object.fromEntries(
            categories
              .filter((category) => this.subcategories(category).length)
              .map((category) => String(category.id))
              .filter((key) => expandAll || expanded[key])
              .map((key) => [key, true]),
          ),
        );
      });
    });
  }

  resetState(): void {
    this.table().reset();
  }

  // Shows the subcategories of a category, e.g. after one of them changed.
  expand(categoryId: number): void {
    this.expandedRowKeys.update((keys) => ({ ...keys, [String(categoryId)]: true }));
  }

  protected toggle(category: DocumentCategory): void {
    const key = String(category.id);
    const { [key]: expanded, ...others } = this.expandedRowKeys();
    this.expandedRowKeys.set(expanded ? others : { ...others, [key]: true });
  }

  protected subcategories(category: DocumentCategory): DocumentCategory[] {
    return category.subcategories ?? [];
  }

  // Categories and subcategories only show their names.
  protected cellText(row: DocumentCategory, key: string): string | null {
    return key === 'name' || key === 'nameEs' ? row[key] : null;
  }

  protected activeSubcategoryCount(category: DocumentCategory): number {
    return this.subcategories(category).filter(({ deletedAt }) => !deletedAt).length;
  }

  protected subcategoryRowId(subcategory: DocumentCategory): string {
    return `document-subcategory-${subcategory.id}-row`;
  }

  protected subcategoryRowIds(category: DocumentCategory): string {
    return this.subcategories(category)
      .map((subcategory) => this.subcategoryRowId(subcategory))
      .join(' ');
  }

  protected subcategoriesLabel(category: DocumentCategory): string {
    return this.texts.subcategoriesOf(this.nameOf(category));
  }

  protected actionsLabel(row: DocumentCategory): string {
    return row.parentId == null
      ? this.texts.categoryActions(this.nameOf(row))
      : this.texts.subcategoryActions(this.nameOf(row));
  }

  protected openActionsMenu(event: Event, row: DocumentCategory, menu: Menu): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  private actionsFor(row: DocumentCategory | null): MenuItem[] {
    if (!row) return [];
    const view = this.action(this.texts.view, PrimeIcons.EYE, DocumentCategoryTableAction.View);
    if (row.deletedAt) {
      return this.canRestore(row)
        ? [view, this.action(this.texts.restore, PrimeIcons.REFRESH, DocumentCategoryTableAction.Restore)]
        : [view];
    }
    return [
      view,
      this.action(this.texts.edit, PrimeIcons.PENCIL, DocumentCategoryTableAction.Edit),
      this.action(this.texts.delete, PrimeIcons.TRASH, DocumentCategoryTableAction.Delete),
    ];
  }

  // A subcategory can only be restored while its category is active.
  private canRestore(row: DocumentCategory): boolean {
    return row.parentId == null || !this.categoriesById().get(row.parentId)?.deletedAt;
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

  private nameOf(row: DocumentCategory): string {
    return row.name?.trim() || `#${row.id}`;
  }
}
