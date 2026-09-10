import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { CrudEntityDialog } from '@components/crud-entity-dialog/crud-entity-dialog';

import {
  ApplicationProviderFormGroup,
  createApplicationProviderForm,
} from '../../forms/application-development-form.factory';
import { ApplicationProviderDialog } from './application-provider-dialog';

describe('ApplicationProviderDialog', () => {
  let fixture: ComponentFixture<ApplicationProviderDialog>;
  let form: ApplicationProviderFormGroup;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationProviderDialog],
    }).compileComponents();

    form = createApplicationProviderForm(new FormBuilder());
    form.setValue({
      companyName: 'Plexus SL',
      roleId: 3,
      startDate: new Date(2026, 4, 2),
      expireDate: null,
    });
    fixture = TestBed.createComponent(ApplicationProviderDialog);
    fixture.componentRef.setInput('visible', true);
    fixture.componentRef.setInput('form', form);
    fixture.componentRef.setInput('mode', 'view');
    fixture.componentRef.setInput('canEdit', true);
    fixture.componentRef.setInput('roleOptions', [
      { id: 3, label: 'Desenvolupament' },
    ]);
    fixture.detectChanges();
  });

  it('uses edit as its primary view action', () => {
    const dialog = fixture.debugElement.query(By.directive(CrudEntityDialog))
      .componentInstance as CrudEntityDialog;

    expect(dialog.viewPrimaryAction()).toBe('edit');
  });

  it('renders view data without disabled form widgets', () => {
    const company = fixture.nativeElement.querySelector(
      '#application-provider-dialog-company-name',
    ) as HTMLInputElement;

    expect(company.readOnly).toBe(true);
    expect(company.disabled).toBe(false);
    expect(fixture.nativeElement.querySelector('p-select')).toBeNull();
    expect(fixture.nativeElement.querySelector('p-datepicker')).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Desenvolupament');
  });
});
