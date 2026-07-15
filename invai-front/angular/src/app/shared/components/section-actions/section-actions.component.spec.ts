import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SectionActionsComponent } from './section-actions.component';

describe('SectionActionsComponent', () => {
  let fixture: ComponentFixture<SectionActionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [SectionActionsComponent] }).compileComponents();
    fixture = TestBed.createComponent(SectionActionsComponent);
    fixture.detectChanges();
  });

  it('should keep the search icon centered inside an accessible loading spinner', () => {
    const input = () => fixture.nativeElement.querySelector('input[type="text"]') as HTMLElement;
    const magnifier = fixture.nativeElement.querySelector('.section-actions-search__magnifier');

    expect(magnifier.classList.contains('pi-search')).toBe(true);
    expect(fixture.nativeElement.querySelector('.section-actions-search__spinner')).toBeFalsy();
    expect(input().getAttribute('aria-busy')).toBe('false');

    fixture.componentRef.setInput('isSearchLoading', true);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.section-actions-search__magnifier')).toBe(
      magnifier,
    );
    expect(fixture.nativeElement.querySelector('.section-actions-search__spinner')).toBeTruthy();
    expect(
      fixture.nativeElement.querySelector('.section-actions-search__icon--loading'),
    ).toBeTruthy();
    expect(input().getAttribute('aria-busy')).toBe('true');
  });

  it('should show an accessible clear button only when the search has a value', () => {
    expect(fixture.nativeElement.querySelector('.section-actions-search__clear')).toBeFalsy();

    fixture.componentRef.setInput('quickSearchValue', 'Invai');
    fixture.detectChanges();

    const clearButton = fixture.nativeElement.querySelector(
      '.section-actions-search__clear button',
    ) as HTMLButtonElement;
    expect(clearButton).toBeTruthy();
    expect(clearButton.classList.contains('p-button-icon-only')).toBe(true);
    expect(clearButton.querySelector('.pi-times')).toBeTruthy();
    expect(clearButton.getAttribute('aria-label')).toBe('Neteja la cerca ràpida');
  });

  it('should clear the search model and emit the empty value', () => {
    const component = fixture.componentInstance;
    const onQuickSearchChange = vi.fn();
    component.onQuickSearchChange.subscribe(onQuickSearchChange);
    fixture.componentRef.setInput('quickSearchValue', 'Invai');
    fixture.detectChanges();

    const clearButton = fixture.nativeElement.querySelector(
      '.section-actions-search__clear button',
    ) as HTMLButtonElement;
    clearButton.click();
    fixture.detectChanges();

    expect(component.quickSearchValue()).toBe('');
    expect(onQuickSearchChange).toHaveBeenCalledOnce();
    expect(onQuickSearchChange).toHaveBeenCalledWith('');
    expect(fixture.nativeElement.querySelector('.section-actions-search__clear')).toBeFalsy();
  });
});
