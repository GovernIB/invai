import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';

import {
  createDocumentCategoryPermissionsForm,
  resetDocumentCategoryPermissionsForm,
} from '../../forms/document-category-permissions-form.factory';
import { DocumentCategoryPermissionsTable } from './document-category-permissions-table';

const fb = new FormBuilder();

@Component({
  imports: [DocumentCategoryPermissionsTable],
  template: `<app-document-category-permissions-table [form]="form" />`,
})
class HostComponent {
  readonly form = createDocumentCategoryPermissionsForm(fb);

  constructor() {
    resetDocumentCategoryPermissionsForm(fb, this.form, [
      { roleId: 1, roleName: 'Administració funcional', read: true, upload: true, modify: true, delete: true },
      { roleId: 4, roleName: 'Consulta', read: true, upload: false, modify: false, delete: false },
    ]);
  }
}

describe('DocumentCategoryPermissionsTable', () => {
  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;

  const root = () => fixture.nativeElement as HTMLElement;
  const checkbox = (name: string) =>
    root().querySelector<HTMLInputElement>(`input[type="checkbox"][aria-label="${name}"]`)!;
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

  it('shows one row per role with every permission and its state', () => {
    expect(root().querySelector('table')?.getAttribute('aria-label')).toBe(
      "Permisos d'accés de cada rol",
    );
    expect([...root().querySelectorAll('thead th')].map((th) => th.textContent?.trim())).toEqual([
      'Rol',
      'Lectura',
      'Càrrega',
      'Modificació',
      'Eliminació (baixa lògica)',
      'Tots els permisos',
    ]);
    const consult = [...root().querySelectorAll('tbody tr')][1];
    expect(consult.querySelector('th[scope="row"]')?.textContent?.trim()).toBe('Consulta');
    expect([...consult.querySelectorAll('td')].map((cell) => cell.textContent?.trim())).toEqual([
      'Permès',
      'Denegat',
      'Denegat',
      'Denegat',
      'Accés total',
    ]);
    expect(checkbox('Lectura per a Consulta').checked).toBe(true);
    expect(checkbox('Tots els permisos per a Consulta').checked).toBe(false);
    expect(checkbox('Tots els permisos per a Administració funcional').checked).toBe(true);
  });

  it('labels each checkbox with its permission and role and toggles it from its state text', async () => {
    const upload = checkbox('Càrrega per a Consulta');
    root().querySelector<HTMLLabelElement>(`label[for="${upload.id}"]`)!.click();
    await render();

    expect(host.form.at(1).controls.upload.value).toBe(true);
    expect(host.form.dirty).toBe(true);
    expect(checkbox('Càrrega per a Consulta').closest('td')?.textContent?.trim()).toBe('Permès');
  });

  it('grants or denies every permission of a role at once', async () => {
    checkbox('Tots els permisos per a Consulta').click();
    await render();

    expect(host.form.at(1).getRawValue()).toMatchObject({
      read: true,
      upload: true,
      modify: true,
      delete: true,
    });
    expect(host.form.dirty).toBe(true);

    checkbox('Modificació per a Consulta').click();
    await render();
    expect(checkbox('Tots els permisos per a Consulta').checked).toBe(false);

    checkbox('Tots els permisos per a Administració funcional').click();
    await render();
    expect(host.form.at(0).controls.read.value).toBe(false);
    expect(host.form.at(0).controls.delete.value).toBe(false);
  });

  it('disables every checkbox while the form is disabled', async () => {
    host.form.disable();
    await render();

    const checkboxes = [...root().querySelectorAll<HTMLInputElement>('input[type="checkbox"]')];
    expect(checkboxes).toHaveLength(10);
    expect(checkboxes.every((input) => input.disabled)).toBe(true);
  });

  it('stripes the rows', () => {
    expect(root().querySelector('p-table')?.classList).toContain('p-datatable-striped');
  });
});
