import { TestBed } from '@angular/core/testing';
import { SearchFiltersComponent } from './search-filters.component';

describe('SearchFiltersComponent', () => {
  it('exposes the expanded state on the native more-filters button', () => {
    const fixture = TestBed.createComponent(SearchFiltersComponent);
    fixture.detectChanges();
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button[aria-expanded]');
    expect(button.getAttribute('aria-expanded')).toBe('false');
    button.click();
    fixture.detectChanges();
    expect(button.getAttribute('aria-expanded')).toBe('true');
    button.click();
    fixture.detectChanges();
    expect(button.getAttribute('aria-expanded')).toBe('false');
    expect(fixture.nativeElement.querySelector('p-button[aria-expanded]')).toBeNull();
  });

  it('keeps search and reset outputs inside the filter panel', () => {
    const fixture = TestBed.createComponent(SearchFiltersComponent);
    const search = vi.fn();
    const reset = vi.fn();
    fixture.componentInstance.onSearch.subscribe(search);
    fixture.componentInstance.onReset.subscribe(reset);
    fixture.componentRef.setInput('hideMoreFiltersButton', true);
    fixture.detectChanges();
    const buttons = fixture.nativeElement.querySelectorAll('.invai-filter-panel button');
    expect(buttons).toHaveLength(2);
    buttons[0].click();
    buttons[1].click();
    expect(search).toHaveBeenCalledOnce();
    expect(reset).toHaveBeenCalledOnce();
  });
});
