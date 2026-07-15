import { Directive, HostBinding, input } from '@angular/core';

@Directive({
  selector: '[appSearchFilterGrid]',
  standalone: true,
})
export class SearchFilterGridDirective {
  filterColumns = input(3);
  filterTabletColumns = input(2);
  filterMobileColumns = input(1);

  @HostBinding('class.invai-search-filter-grid')
  protected readonly gridClass = true;

  @HostBinding('style.--invai-search-filter-columns')
  protected get columns(): string {
    return String(this.filterColumns());
  }

  @HostBinding('style.--invai-search-filter-tablet-columns')
  protected get tabletColumns(): string {
    return String(this.filterTabletColumns());
  }

  @HostBinding('style.--invai-search-filter-mobile-columns')
  protected get mobileColumns(): string {
    return String(this.filterMobileColumns());
  }
}
