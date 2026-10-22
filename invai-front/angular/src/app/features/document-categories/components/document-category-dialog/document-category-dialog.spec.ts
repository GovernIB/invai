import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';

import { createDocumentCategoryForm } from '../../forms/document-category-form.factory';
import {
  DocumentCategoryDialog,
  DocumentCategoryDialogMode,
  DocumentCategoryKind,
} from './document-category-dialog';

@Component({
  imports: [DocumentCategoryDialog],
  template: `
    <app-document-category-dialog
      categoryLabel="Tècnica"
      [visible]="true"
      [canEdit]="canEdit()"
      [canRestore]="canRestore()"
      [categoryOptions]="options"
      [form]="form"
      [kind]="kind()"
      [mode]="mode()"
    />
  `,
})
class HostComponent {
  readonly form = createDocumentCategoryForm(new FormBuilder());
  readonly options = [
    { id: 1, label: 'Tècnica' },
    { id: 2, label: 'Seguretat' },
  ];
  readonly kind = signal<DocumentCategoryKind>('category');
  readonly mode = signal<DocumentCategoryDialogMode>('create');
  readonly canEdit = signal(true);
  readonly canRestore = signal(false);
}

describe('DocumentCategoryDialog', () => {
  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;

  const body = () => document.body;
  const title = () => body().querySelector('.p-dialog h2')?.textContent?.trim();
  const button = (label: string) =>
    [...body().querySelectorAll<HTMLButtonElement>('.p-dialog button')].find(
      (element) => element.textContent?.trim() === label,
    );

  async function render(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [HostComponent] }).compileComponents();
    fixture = TestBed.createComponent(HostComponent);
    host = fixture.componentInstance;
  });

  afterEach(() => fixture.destroy());

  it('creates a category with both names and no category field', async () => {
    await render();

    expect(title()).toBe('Nova categoria documental');
    expect(body().querySelector('label[for="document-category-dialog-name"]')?.textContent?.trim()).toBe('Nom');
    expect(body().querySelector('label[for="document-category-dialog-name-es"]')?.textContent?.trim()).toBe(
      'Nom en castellà',
    );
    expect(body().querySelector('#document-category-dialog-name')?.getAttribute('maxlength')).toBe('150');
    expect(body().querySelector('#document-category-dialog-category')).toBeNull();
    expect(body().querySelector('#document-category-dialog-category-view-label')).toBeNull();
    expect(button('Afegir')).toBeDefined();
  });

  it('shows the fixed category of a new subcategory', async () => {
    host.kind.set('subcategory');
    await render();

    expect(title()).toBe('Nova subcategoria documental');
    const value = body().querySelector('[aria-labelledby="document-category-dialog-category-view-label"]');
    expect(value?.textContent?.trim()).toBe('Tècnica');
    expect(body().querySelector('#document-category-dialog-category-view-label')?.textContent?.trim()).toBe(
      'Categoria',
    );
    expect(body().querySelector('p-select')).toBeNull();
  });

  it('lets an edited subcategory move to another active category', async () => {
    host.kind.set('subcategory');
    host.mode.set('edit');
    host.form.reset({ name: 'Manuals', nameEs: 'Manuales', parentId: 1 });
    await render();

    expect(title()).toBe('Edició de la subcategoria documental');
    expect(body().querySelector('#document-category-dialog-category')).not.toBeNull();
    expect(body().querySelector('label[for="document-category-dialog-category"]')?.textContent?.trim()).toBe(
      'Categoria',
    );
    expect(body().querySelector('[aria-labelledby="document-category-dialog-category-view-label"]')).toBeNull();
  });

  it('shows a consulted record read-only and only offers what its state allows', async () => {
    host.kind.set('subcategory');
    host.mode.set('view');
    host.form.reset({ name: 'API', nameEs: 'API', parentId: 1 });
    await render();

    expect(title()).toBe('Consulta de la subcategoria documental');
    expect(body().querySelector<HTMLInputElement>('#document-category-dialog-name')?.readOnly).toBe(true);
    expect(button('Editar')).toBeDefined();

    host.canEdit.set(false);
    await render();
    expect(button('Editar')).toBeUndefined();
    expect(button('Restaurar')).toBeUndefined();

    host.canRestore.set(true);
    await render();
    expect(button('Restaurar')).toBeDefined();
  });

  it('connects the name errors to their fields', async () => {
    host.form.controls.name.markAsTouched();
    host.form.controls.nameEs.setValue('x'.repeat(151));
    host.form.controls.nameEs.markAsTouched();
    await render();

    const name = body().querySelector('#document-category-dialog-name')!;
    expect(name.getAttribute('aria-invalid')).toBe('true');
    expect(name.getAttribute('aria-describedby')).toBe('document-category-dialog-name-error');
    expect(body().querySelector('#document-category-dialog-name-error')?.textContent?.trim()).toBe(
      'Camp obligatori',
    );
    expect(body().querySelector('#document-category-dialog-name-es-error')?.textContent?.trim()).toBe(
      'El nom en castellà ha de tenir com a màxim 150 caràcters',
    );
  });
});
