import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { Select } from 'primeng/select';

import { documentRow } from '../../../../application-documentation.testing';
import { ApplicationDocumentationEmailSelection } from './application-documentation-email.types';
import { ApplicationDocumentationEmailTable } from './application-documentation-email-table';

const ROWS = [
  documentRow(),
  documentRow({ id: 2, title: 'Pla de proves', versionLabel: '1.0', currentAttachmentId: 30 }),
  documentRow({ id: 3, title: 'Esborrany sense fitxer', versionLabel: null, currentAttachmentId: null }),
];

const CHOSEN: ApplicationDocumentationEmailSelection = {
  documentId: 1,
  title: 'Manual API v2',
  versionId: 20,
  versions: [
    { id: 20, label: '2.1', fileSize: 2_300_000 },
    { id: 19, label: '2.0', fileSize: 1_800_000 },
  ],
};

describe('ApplicationDocumentationEmailTable', () => {
  let fixture: ComponentFixture<ApplicationDocumentationEmailTable>;
  let toggled: ReturnType<typeof vi.fn<(row: (typeof ROWS)[number]) => void>>;

  beforeEach(async () => {
    // The overlay of PrimeNG Select listens to a breakpoint media query that jsdom does not implement.
    vi.stubGlobal(
      'matchMedia',
      vi.fn(() => ({ matches: false, addEventListener: vi.fn(), removeEventListener: vi.fn() })),
    );
    await TestBed.configureTestingModule({ imports: [ApplicationDocumentationEmailTable] }).compileComponents();
    fixture = TestBed.createComponent(ApplicationDocumentationEmailTable);
    fixture.componentRef.setInput('itemsList', { items: ROWS, total: ROWS.length });
    fixture.componentRef.setInput('selection', new Map());
    toggled = vi.fn<(row: (typeof ROWS)[number]) => void>();
    fixture.componentInstance.selectionChange.subscribe(toggled);
    fixture.detectChanges();
  });

  afterEach(() => vi.unstubAllGlobals());

  const root = () => fixture.nativeElement as HTMLElement;
  const rows = () => [...root().querySelectorAll<HTMLTableRowElement>('tbody tr')];
  const checkbox = (row: number) => rows()[row].querySelector<HTMLInputElement>('input[type="checkbox"]')!;

  // ngModel writes the checked state of the checkboxes once the change detection settles.
  async function choose(selection: ApplicationDocumentationEmailSelection[]): Promise<void> {
    fixture.componentRef.setInput('selection', new Map(selection.map((item) => [item.documentId, item])));
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  it('renders a striped catalog with a named checkbox per document and its format and version', () => {
    expect(root().querySelector('.p-datatable-striped')).toBeTruthy();
    expect(root().querySelector('.p-datatable-mask')).toBeNull();
    expect(checkbox(0).getAttribute('aria-label')).toBe('Envia «Manual API v2»');
    expect(rows()[0].textContent).toContain('PDF');
    expect(rows()[0].textContent).toContain('2.1');
    expect(root().querySelector('th .sr-only')?.textContent?.trim()).toBe('Selecció');
  });

  it('toggles a document with a click or Return on its row, and once with its checkbox', () => {
    rows()[1].click();
    rows()[1].dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    checkbox(1).click();

    expect(toggled).toHaveBeenCalledTimes(3);
    expect(toggled).toHaveBeenLastCalledWith(ROWS[1]);
    expect(rows()[1].getAttribute('tabindex')).toBe('0');
  });

  it('does not let a document without a current version be chosen', () => {
    rows()[2].click();

    expect(toggled).not.toHaveBeenCalled();
    expect(checkbox(2).disabled).toBe(true);
    expect(rows()[2].hasAttribute('tabindex')).toBe(false);
  });

  it('marks a chosen document and offers its versions in a named select', async () => {
    await choose([CHOSEN]);

    expect(rows()[0].classList).toContain('application-documentation-email-table__selected');
    expect(checkbox(0).checked).toBe(true);
    const select = fixture.debugElement.query(By.directive(Select)).componentInstance as Select;
    expect(select.ariaLabel).toBe("Versió de «Manual API v2» que s'envia");
    expect(select.options).toEqual([
      { value: 20, label: '2.1' },
      { value: 19, label: '2.0' },
    ]);
    expect(rows()[1].querySelector('p-select')).toBeNull();
  });

  it('offers the current version alone while the versions are read', async () => {
    await choose([{ ...CHOSEN, versions: null }]);

    const select = fixture.debugElement.query(By.directive(Select)).componentInstance as Select;
    expect(select.options).toEqual([{ value: 20, label: '2.1' }]);
    expect(select.loading).toBe(true);
  });

  it('emits the version picked for a document without toggling it', async () => {
    const changed = vi.fn();
    fixture.componentInstance.versionChange.subscribe(changed);
    await choose([CHOSEN]);

    fixture.debugElement.query(By.directive(Select)).triggerEventHandler('ngModelChange', 19);
    rows()[0].querySelector<HTMLElement>('p-select')!.click();
    rows()[0].querySelector<HTMLElement>('.p-select-dropdown')!.click();

    expect(changed).toHaveBeenCalledWith({ documentId: 1, versionId: 19 });
    expect(toggled).not.toHaveBeenCalled();
  });

  it('keeps the rows on screen and marks the table busy while a page is read', () => {
    fixture.componentRef.setInput('isLoading', true);
    fixture.detectChanges();

    expect(root().querySelector('.invai-table-loading-container')?.getAttribute('aria-busy')).toBe('true');
    expect(root().querySelector('.invai-table-loading-shield')).toBeTruthy();
    expect(rows()).toHaveLength(3);
    expect(root().querySelector('.p-datatable-mask')).toBeNull();
  });

  it('says so when there is nothing to send', () => {
    fixture.componentRef.setInput('itemsList', { items: [], total: 0 });
    fixture.detectChanges();

    expect(rows()[0].textContent?.trim()).toBe('No hi ha cap adjunt per enviar.');
    expect(rows()[0].querySelector('td')?.getAttribute('colspan')).toBe('4');
  });
});
