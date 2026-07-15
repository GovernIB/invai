import {
  ChangeDetectionStrategy,
  Component,
  input,
  model,
  output,
  ViewEncapsulation,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TableColumn } from '@models/table.model';
import { PrimeIcons } from 'primeng/api';
import { BadgeModule } from 'primeng/badge';
import { Button } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { MultiSelectModule } from 'primeng/multiselect';
import { TooltipModule } from 'primeng/tooltip';
import {
  SECTION_ACTIONS_ADD_ARIA_LABEL,
  SECTION_ACTIONS_CLEAR_QUICK_SEARCH_ARIA_LABEL,
  SECTION_ACTIONS_COLUMNS_FILTER_PLACEHOLDER,
  SECTION_ACTIONS_COLUMNS_ARIA_LABEL,
  SECTION_ACTIONS_EXPORT_ARIA_LABEL,
  SECTION_ACTIONS_FILTERS_ARIA_LABEL,
  SECTION_ACTIONS_QUICK_SEARCH_ARIA_LABEL,
} from './section-actions.i18n';

@Component({
  selector: 'app-section-actions',
  standalone: true,
  imports: [Button, MultiSelectModule, FormsModule, BadgeModule, InputText, TooltipModule],
  templateUrl: './section-actions.component.html',
  styleUrl: './section-actions.component.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SectionActionsComponent {
  hideQuickSearch = input(false);
  hideCollapseButton = input(false);
  hideColumnsButton = input(false);
  hideExportButton = input(false);
  hideAddButton = input(false);
  isExportLoading = input(false);
  isSearchLoading = input(false);
  filtersSelected = input<number | null>(null);
  quickSearchPlaceholder = input('Cerca ràpida');
  quickSearchAriaLabel = input(SECTION_ACTIONS_QUICK_SEARCH_ARIA_LABEL);
  quickSearchClearAriaLabel = input(SECTION_ACTIONS_CLEAR_QUICK_SEARCH_ARIA_LABEL);
  columnsFilterPlaceholder = input(SECTION_ACTIONS_COLUMNS_FILTER_PLACEHOLDER);
  filtersButtonAriaLabel = input(SECTION_ACTIONS_FILTERS_ARIA_LABEL);
  columnsButtonAriaLabel = input(SECTION_ACTIONS_COLUMNS_ARIA_LABEL);
  exportButtonAriaLabel = input(SECTION_ACTIONS_EXPORT_ARIA_LABEL);
  addButtonAriaLabel = input(SECTION_ACTIONS_ADD_ARIA_LABEL);
  availableColumns = input<Partial<TableColumn>[]>();
  quickSearchValue = model('');
  selectedColumns = model<Partial<TableColumn>[]>();
  isFiltersCollapsed = model<boolean>();
  onAdd = output();
  onExport = output();
  onQuickSearchChange = output<string>();

  protected readonly PrimeIcons = PrimeIcons;

  onSearchInput(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    this.quickSearchValue.set(value);
    this.onQuickSearchChange.emit(value);
  }

  onClearSearch() {
    this.quickSearchValue.set('');
    this.onQuickSearchChange.emit('');
  }

  onToggleFilters() {
    this.isFiltersCollapsed.update((lastValue) => !lastValue);
  }

  onExportExcel() {
    this.onExport.emit();
  }

  onAddItem() {
    this.onAdd.emit();
  }
}
