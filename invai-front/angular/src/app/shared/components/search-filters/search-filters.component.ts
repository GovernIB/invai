import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  input,
  output,
  signal,
  ViewEncapsulation,
} from '@angular/core';
import { PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import {
  SEARCH_FILTERS_HIDE_FILTERS,
  SEARCH_FILTERS_MORE_FILTERS,
  SEARCH_FILTERS_MORE_FILTERS_TITLE,
  SEARCH_FILTERS_RESET,
  SEARCH_FILTERS_SEARCH,
  SEARCH_FILTERS_TITLE,
} from './search-filters.i18n';

@Component({
  selector: 'app-search-filters',
  standalone: true,
  imports: [Button],
  templateUrl: './search-filters.component.html',
  styleUrl: './search-filters.component.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SearchFiltersComponent {
  title = input(SEARCH_FILTERS_TITLE);
  hideMoreFiltersButton = input(false);
  isLoading = input<boolean>(false);
  isDisable = input<boolean>(false);
  onSearch = output();
  onReset = output();

  protected isMoreFiltersCollapsed = signal(true);

  protected readonly PrimeIcons = PrimeIcons;
  protected readonly searchLabel = SEARCH_FILTERS_SEARCH;
  protected readonly resetLabel = SEARCH_FILTERS_RESET;
  protected readonly moreFiltersTitle = SEARCH_FILTERS_MORE_FILTERS_TITLE;

  constructor() {
    effect(
      () => {
        if (this.hideMoreFiltersButton()) {
          this.isMoreFiltersCollapsed.set(true);
        }
      },
      { allowSignalWrites: true },
    );
  }

  protected iconMoreFilters = computed(() =>
    this.isMoreFiltersCollapsed() ? PrimeIcons.FILTER : PrimeIcons.FILTER_SLASH,
  );

  protected labelMoreFilters = computed(() =>
    this.isMoreFiltersCollapsed() ? SEARCH_FILTERS_MORE_FILTERS : SEARCH_FILTERS_HIDE_FILTERS,
  );

  search() {
    this.onSearch.emit();
  }

  reset() {
    this.onReset.emit();
  }

  onToggleMoreFiltersCollapsed() {
    this.isMoreFiltersCollapsed.set(!this.isMoreFiltersCollapsed());
  }
}
