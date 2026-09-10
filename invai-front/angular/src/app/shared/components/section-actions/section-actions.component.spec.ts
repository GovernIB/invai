import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SectionActionsComponent } from './section-actions.component';

describe('SectionActionsComponent', () => {
  let fixture: ComponentFixture<SectionActionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [SectionActionsComponent] }).compileComponents();
    fixture = TestBed.createComponent(SectionActionsComponent);
    fixture.detectChanges();
  });

  it('should expose separate search and button groups for the responsive layout', () => {
    const root = fixture.nativeElement.querySelector('.section-actions');
    const search = fixture.nativeElement.querySelector('.section-actions__search');
    const buttons = fixture.nativeElement.querySelector('.section-actions__buttons');

    expect(root).toBeTruthy();
    expect(search).toBeTruthy();
    expect(buttons).toBeTruthy();
    expect(search.parentElement).toBe(root);
    expect(buttons.parentElement).toBe(root);
  });

  it('should preserve the compact actions layout when quick search is hidden', () => {
    fixture.componentRef.setInput('hideQuickSearch', true);
    fixture.detectChanges();

    const root = fixture.nativeElement.querySelector('.section-actions');

    expect(root.classList.contains('section-actions--without-search')).toBe(true);
    expect(fixture.nativeElement.querySelector('.section-actions__search')).toBeFalsy();
    expect(fixture.nativeElement.querySelector('.section-actions__buttons')).toBeTruthy();
  });

  it('should apply the shared table toolbar sizing contract', () => {
    const searchInput = fixture.nativeElement.querySelector(
      '.section-actions-search__input',
    ) as HTMLInputElement;
    const columnsControl = fixture.nativeElement.querySelector(
      '.custom-multiselect-button',
    ) as HTMLElement;
    const toolbarButtons = Array.from(
      fixture.nativeElement.querySelectorAll('.section-actions__buttons button'),
    ) as HTMLButtonElement[];

    expect(searchInput.classList).toContain('invai-table-toolbar-control');
    expect(columnsControl.classList).toContain('invai-table-toolbar-square-control');
    expect(toolbarButtons).toHaveLength(3);
    expect(
      toolbarButtons.every((button) =>
        button.classList.contains('invai-table-toolbar-button'),
      ),
    ).toBe(true);
    expect(toolbarButtons[0].classList).toContain('p-button-outlined');
    expect(toolbarButtons[1].classList).toContain('p-button-outlined');
    expect(toolbarButtons[2].classList).not.toContain('p-button-outlined');
  });

  it('should put the expanded state on the native filters button', () => {
    fixture.componentRef.setInput('isFiltersCollapsed', true);
    fixture.detectChanges();
    const filtersButton = fixture.nativeElement.querySelector(
      '.section-actions__buttons button',
    ) as HTMLButtonElement;
    expect(filtersButton.getAttribute('aria-expanded')).toBe('false');

    fixture.componentRef.setInput('isFiltersCollapsed', false);
    fixture.detectChanges();
    expect(filtersButton.getAttribute('aria-expanded')).toBe('true');
  });

  it('preserves accessible actions, search and disabled behavior on a primary frame', () => {
    fixture.componentRef.setInput('appearance', 'on-primary');
    fixture.componentRef.setInput('disableAddButton', true);
    fixture.componentRef.setInput('isFiltersCollapsed', true);
    fixture.detectChanges();
    const add = vi.fn();
    const search = vi.fn();
    fixture.componentInstance.onAdd.subscribe(add);
    fixture.componentInstance.onQuickSearchChange.subscribe(search);
    const buttons = fixture.nativeElement.querySelectorAll('.section-actions__buttons button');
    const addButton = buttons[buttons.length - 1] as HTMLButtonElement;

    expect(addButton.disabled).toBe(true);
    expect(addButton.getAttribute('aria-label')).toBeTruthy();
    addButton.click();
    expect(add).not.toHaveBeenCalled();
    fixture.componentRef.setInput('disableAddButton', false);
    fixture.detectChanges();
    addButton.click();
    expect(add).toHaveBeenCalledOnce();

    buttons[0].click();
    fixture.detectChanges();
    expect(buttons[0].getAttribute('aria-expanded')).toBe('true');
    const input = fixture.nativeElement.querySelector('input[type="text"]') as HTMLInputElement;
    input.value = 'Inventory';
    input.dispatchEvent(new Event('input'));
    expect(search).toHaveBeenCalledWith('Inventory');
  });

  it('should accept a unique input id for the column selector', () => {
    fixture.componentRef.setInput('columnsInputId', 'responsible-companies-columns');
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector('#responsible-companies-columns'),
    ).not.toBeNull();
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
