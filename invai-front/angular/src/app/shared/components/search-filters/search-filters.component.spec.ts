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

  it('titles the panel one level below the page heading by default', () => {
    const fixture = TestBed.createComponent(SearchFiltersComponent);
    fixture.detectChanges();
    fixture.nativeElement.querySelector('button[aria-expanded]').click();
    fixture.detectChanges();
    const headings = [...fixture.nativeElement.querySelectorAll('h1, h2, h3, h4, h5, h6')];
    expect(headings.map((heading: HTMLElement) => heading.tagName)).toEqual(['H2', 'H2']);
  });

  it('nests the panel headings under the heading of the region that owns it', () => {
    const fixture = TestBed.createComponent(SearchFiltersComponent);
    fixture.componentRef.setInput('headingLevel', 4);
    fixture.componentRef.setInput('title', 'Filtres de rols');
    fixture.detectChanges();
    const headings = [...fixture.nativeElement.querySelectorAll('h1, h2, h3, h4, h5, h6')];
    expect(headings.map((heading: HTMLElement) => heading.tagName)).toEqual(['H4']);
    expect(headings[0].textContent.trim()).toBe('Filtres de rols');
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
