import { Component, signal, viewChild } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActionParams, PaginatedList } from '@models/table.model';
import { MenuItem } from 'primeng/api';

import { DOCUMENT_SUBCATEGORIES_TABLE_COLUMNS } from '../../document-categories.constants';
import { DocumentCategory } from '../../document-categories.model';
import { documentCategory } from '../../document-categories.testing';
import { DocumentCategoryTableAction } from '../document-categories-table/document-categories-table';
import { DocumentSubcategoriesTable } from './document-subcategories-table';

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
  nameEs: 'Interfaz',
  parentId: 1,
  deletedAt: '2026-10-01T10:00:00',
});

@Component({
  imports: [DocumentSubcategoriesTable],
  template: `
    <app-document-subcategories-table
      categoryName="Tècnica"
      [columns]="columns"
      [isCategoryActive]="isCategoryActive()"
      [itemsList]="items()"
      (onSelectAction)="actions.push($event)"
    />
  `,
})
class HostComponent {
  readonly columns = DOCUMENT_SUBCATEGORIES_TABLE_COLUMNS;
  readonly items = signal<PaginatedList<DocumentCategory>>({ items: [MANUALS, API], total: 2 });
  readonly isCategoryActive = signal(true);
  readonly actions: ActionParams<DocumentCategory>[] = [];
  readonly table = viewChild.required(DocumentSubcategoriesTable);
}

describe('DocumentSubcategoriesTable', () => {
  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;

  const root = () => fixture.nativeElement as HTMLElement;
  const names = () =>
    [...root().querySelectorAll('tbody tr')].map((row) => row.querySelector('td')?.textContent?.trim());
  const actionButton = (name: string) =>
    root().querySelector<HTMLButtonElement>(`button[aria-label="Accions de la subcategoria «${name}»"]`)!;
  function openMenu(name: string): string[] {
    actionButton(name).click();
    fixture.detectChanges();
    const table = host.table() as unknown as { rowActions: () => MenuItem[] };
    return table.rowActions().map(({ label }) => label ?? '');
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

  it('names the table after its category and sorts the subcategories in the browser', async () => {
    expect(root().querySelector('table')?.getAttribute('aria-label')).toBe('Subcategories de «Tècnica»');
    expect(names()).toEqual(['Manuals', 'API']);

    root().querySelector<HTMLElement>('thead th')!.click();
    await render();

    expect(names()).toEqual(['API', 'Manuals']);
  });

  it('offers the actions each subcategory allows', async () => {
    expect(openMenu('Manuals')).toEqual(['Consulta', 'Edita', 'Elimina']);
    expect(openMenu('API')).toEqual(['Consulta', 'Restaura']);

    host.isCategoryActive.set(false);
    await render();
    expect(openMenu('API')).toEqual(['Consulta']);
  });

  it('emits the selected action and opens a subcategory on double click', () => {
    openMenu('API');
    const table = host.table() as unknown as { rowActions: () => MenuItem[] };
    table.rowActions()[1].command?.({} as never);
    root()
      .querySelector('tbody tr')!
      .dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));

    expect(host.actions).toEqual([
      { action: DocumentCategoryTableAction.Restore, params: API },
      { action: DocumentCategoryTableAction.View, params: MANUALS },
    ]);
  });

  it('says when the category has no subcategories', async () => {
    host.items.set({ items: [], total: 0 });
    await render();

    expect(root().querySelector('tbody')?.textContent?.trim()).toBe(
      'Aquesta categoria no té subcategories.',
    );
  });
});
