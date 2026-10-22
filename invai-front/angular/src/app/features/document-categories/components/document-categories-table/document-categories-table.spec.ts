import { Component, signal, viewChild } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActionParams, PaginatedList } from '@models/table.model';
import { MenuItem } from 'primeng/api';

import { DOCUMENT_CATEGORIES_TABLE_COLUMNS } from '../../document-categories.constants';
import { DocumentCategory } from '../../document-categories.model';
import { documentCategory } from '../../document-categories.testing';
import { DocumentCategoriesTable, DocumentCategoryTableAction } from './document-categories-table';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

const MANUALS = documentCategory({ id: 11, name: 'Manuals', nameEs: 'Manuales', parentId: 1 });
const API = documentCategory({
  id: 12,
  name: 'API',
  nameEs: 'API',
  parentId: 1,
  deletedAt: '2026-10-01T10:00:00',
});
const TECHNICAL = documentCategory({ id: 1, subcategories: [MANUALS, API] });
const AUDITS = documentCategory({
  id: 21,
  name: 'Auditories',
  nameEs: 'Auditorías',
  parentId: 2,
  deletedAt: '2026-10-01T10:00:00',
});
const SECURITY = documentCategory({
  id: 2,
  name: 'Seguretat',
  nameEs: 'Seguridad',
  subcategories: [AUDITS],
  deletedAt: '2026-10-02T10:00:00',
});
const NORMS = documentCategory({ id: 3, name: 'Normativa', nameEs: 'Normativa' });

@Component({
  imports: [DocumentCategoriesTable],
  template: `
    <app-document-categories-table
      [columns]="columns()"
      [expandAll]="expandAll()"
      [itemsList]="items()"
      (onSelectAction)="actions.push($event)"
    />
  `,
})
class HostComponent {
  readonly columns = signal(DOCUMENT_CATEGORIES_TABLE_COLUMNS);
  readonly items = signal<PaginatedList<DocumentCategory>>({
    items: [TECHNICAL, SECURITY, NORMS],
    total: 3,
  });
  readonly expandAll = signal(false);
  readonly actions: ActionParams<DocumentCategory>[] = [];
  readonly table = viewChild.required(DocumentCategoriesTable);
}

describe('DocumentCategoriesTable', () => {
  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;

  const root = () => fixture.nativeElement as HTMLElement;
  const toggle = (name: string) =>
    root().querySelector<HTMLButtonElement>(`button[aria-label="Subcategories de «${name}»"]`);
  // The subcategory rows a category's toggle controls, which only exist while it is expanded.
  const subcategoryRows = (name: string) =>
    (toggle(name)?.getAttribute('aria-controls') ?? '')
      .split(' ')
      .filter(Boolean)
      .map((id) => root().querySelector<HTMLTableRowElement>(`#${id}`)!);
  const cellTexts = (row: HTMLTableRowElement) =>
    [...row.querySelectorAll('td')].map((cell) => cell.textContent?.trim());
  const categoryRow = (name: string) =>
    [...root().querySelectorAll<HTMLTableRowElement>('tr.invai-table-consultable-row')].find((row) =>
      row.textContent?.includes(name),
    )!;
  function openMenu(button: HTMLButtonElement): string[] {
    button.click();
    fixture.detectChanges();
    const table = host.table() as unknown as { rowActions: () => MenuItem[] };
    return table.rowActions().map(({ label }) => label ?? '');
  }

  function actionButton(kind: 'categoria' | 'subcategoria', name: string): HTMLButtonElement {
    return root().querySelector<HTMLButtonElement>(
      `button[aria-label="Accions de la ${kind} «${name}»"]`,
    )!;
  }

  async function render(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [HostComponent] }).compileComponents();
    fixture = TestBed.createComponent(HostComponent);
    host = fixture.componentInstance;
    await render();
  });

  it('offers to expand only the categories with subcategories and counts the active ones', () => {
    expect(toggle('Tècnica')?.getAttribute('aria-expanded')).toBe('false');
    expect(toggle('Tècnica')?.hasAttribute('aria-controls')).toBe(false);
    expect(toggle('Normativa')).toBeNull();
    expect(root().querySelector('tr.document-categories-table__subcategory')).toBeNull();

    expect(
      [...root().querySelectorAll('thead th')].map((header) => header.textContent?.trim()),
    ).toEqual(['Nom', 'Nom en castellà', 'Subcategories actives', 'Accions']);
    expect(cellTexts(categoryRow('Tècnica'))).toEqual(['Tècnica', 'Técnica', '1', '']);
    // The toggle sits in the first column, before the name.
    expect(categoryRow('Tècnica').querySelector('td')!.contains(toggle('Tècnica'))).toBe(true);
  });

  it('expands the subcategories as rows under the columns of their category', async () => {
    toggle('Tècnica')!.click();
    await render();

    expect(toggle('Tècnica')!.getAttribute('aria-expanded')).toBe('true');
    const rows = subcategoryRows('Tècnica');
    expect(rows.map(cellTexts)).toEqual([
      ['Manuals', 'Manuales', '—', ''],
      ['API', 'API', '—', ''],
    ]);
    // Only the last subcategory closes the group.
    expect(rows.map((row) => row.classList.contains('document-categories-table__subcategory--last'))).toEqual([
      false,
      true,
    ]);
    // The subcategories follow their category in the same table, with no header of their own.
    expect(categoryRow('Tècnica').nextElementSibling).toBe(rows[0]);
    expect(root().querySelectorAll('p-table')).toHaveLength(1);
    expect(root().querySelectorAll('thead')).toHaveLength(1);

    toggle('Tècnica')!.click();
    await render();

    expect(toggle('Tècnica')!.getAttribute('aria-expanded')).toBe('false');
    expect(root().querySelector('tr.document-categories-table__subcategory')).toBeNull();
  });

  it('follows the selected columns in the subcategory rows', async () => {
    host.columns.set(DOCUMENT_CATEGORIES_TABLE_COLUMNS.filter(({ key }) => key === 'nameEs'));
    toggle('Tècnica')!.click();
    await render();

    expect(cellTexts(categoryRow('Técnica'))).toEqual(['Técnica', '']);
    expect(categoryRow('Técnica').querySelector('td')!.contains(toggle('Tècnica'))).toBe(true);
    expect(subcategoryRows('Tècnica').map(cellTexts)).toEqual([
      ['Manuales', ''],
      ['API', ''],
    ]);
  });

  it('expands every category found by a text search', async () => {
    host.expandAll.set(true);
    await render();

    expect(subcategoryRows('Tècnica')).toHaveLength(2);
    expect(subcategoryRows('Seguretat')).toHaveLength(1);
  });

  it('keeps the expanded categories when the page is read again and collapses another page', async () => {
    toggle('Tècnica')!.click();
    await render();

    host.items.set({ items: [{ ...TECHNICAL }, SECURITY, NORMS], total: 3 });
    await render();
    expect(subcategoryRows('Tècnica')).toHaveLength(2);

    host.items.set({ items: [documentCategory({ id: 4, name: 'Contractació', subcategories: [MANUALS] })], total: 1 });
    await render();
    expect(toggle('Contractació')!.getAttribute('aria-expanded')).toBe('false');

    host.table().expand(4);
    await render();
    expect(subcategoryRows('Contractació')).toHaveLength(1);
  });

  it('offers the actions each record allows', async () => {
    host.expandAll.set(true);
    await render();

    expect(openMenu(actionButton('categoria', 'Tècnica'))).toEqual([
      'Consulta',
      'Edita',
      'Elimina',
    ]);
    expect(openMenu(actionButton('categoria', 'Seguretat'))).toEqual(['Consulta', 'Restaura']);
    expect(openMenu(actionButton('subcategoria', 'Manuals'))).toEqual([
      'Consulta',
      'Edita',
      'Elimina',
    ]);
    expect(openMenu(actionButton('subcategoria', 'API'))).toEqual(['Consulta', 'Restaura']);
    // A subcategory cannot be restored while its category is inactive.
    expect(openMenu(actionButton('subcategoria', 'Auditories'))).toEqual(['Consulta']);
  });

  it('exposes the popup menu relationship on each action button', () => {
    const button = actionButton('categoria', 'Tècnica');

    expect(button.getAttribute('aria-haspopup')).toBe('menu');
    expect(button.getAttribute('aria-controls')).toBeTruthy();
  });

  it('emits the selected action with its record', async () => {
    host.expandAll.set(true);
    await render();

    openMenu(actionButton('categoria', 'Tècnica'));
    const table = host.table() as unknown as { rowActions: () => MenuItem[] };
    table.rowActions()[1].command?.({} as never);
    openMenu(actionButton('subcategoria', 'API'));
    table.rowActions()[1].command?.({} as never);

    expect(host.actions).toEqual([
      { action: DocumentCategoryTableAction.Edit, params: TECHNICAL },
      { action: DocumentCategoryTableAction.Restore, params: API },
    ]);
  });

  it('opens categories and subcategories on double click or Enter but not from the toggle', async () => {
    toggle('Tècnica')!.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    categoryRow('Normativa').dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    toggle('Tècnica')!.click();
    await render();
    subcategoryRows('Tècnica')[0].dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));

    expect(host.actions).toEqual([
      { action: DocumentCategoryTableAction.View, params: NORMS },
      { action: DocumentCategoryTableAction.View, params: MANUALS },
    ]);
  });

  it('stripes the categories and their subcategories as rows of one table', async () => {
    host.expandAll.set(true);
    await render();

    const tables = [...root().querySelectorAll('p-table')];
    expect(tables).toHaveLength(1);
    expect(tables[0].classList).toContain('p-datatable-striped');
  });
});
