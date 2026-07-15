import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';

import {
  createApplicationCreateForm,
  createApplicationDetailForm,
} from '../../forms/application-form.factory';
import { ApplicationFormFieldLabels, ApplicationFormFields } from './application-form-fields';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

describe('ApplicationFormFields', () => {
  let fixture: ComponentFixture<ApplicationFormFields>;
  const formBuilder = new FormBuilder();
  const labels: ApplicationFormFieldLabels = {
    application: 'Application',
    category: 'Category',
    informationSystem: 'Information system',
    scope: 'Scope',
    commission: 'Commission',
    prefix: 'Prefix',
    administrativeUnit: 'Administrative unit',
    description: 'Description',
    code: 'Code',
    creationDate: 'Creation date',
    modificationDate: 'Modification date',
    withdrawalDate: 'Withdrawal date',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [ApplicationFormFields] }).compileComponents();
    fixture = TestBed.createComponent(ApplicationFormFields);
    fixture.componentRef.setInput('labels', labels);
    fixture.componentRef.setInput('idPrefix', 'test-application');
    fixture.componentRef.setInput('requiredError', 'Required');
    fixture.componentRef.setInput('selectPlaceholder', 'Select');
  });

  it('renders create-only controls and accessible validation errors', () => {
    const form = createApplicationCreateForm(formBuilder);
    form.controls.application.markAsTouched();
    fixture.componentRef.setInput('controls', form.controls);
    fixture.componentRef.setInput('codeControl', form.controls.code);

    fixture.detectChanges();

    const input = fixture.nativeElement.querySelector(
      '#test-application-application',
    ) as HTMLInputElement;
    const prefixInput = fixture.nativeElement.querySelector(
      '#test-application-prefix',
    ) as HTMLInputElement;

    expect(fixture.nativeElement.querySelector('#test-application-code')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('#test-application-creation-date')).toBeNull();
    expect(prefixInput).toBeInstanceOf(HTMLInputElement);
    expect(
      fixture.nativeElement.querySelector('p-select[inputid="test-application-prefix"]'),
    ).toBeNull();
    expect(input.getAttribute('aria-describedby')).toBe('test-application-application-error');
    expect(fixture.nativeElement.querySelector('#test-application-application-error')).toBeTruthy();
  });

  it('renders audit controls without the create-only code control', () => {
    const form = createApplicationDetailForm(formBuilder);
    fixture.componentRef.setInput('controls', form.controls);
    fixture.componentRef.setInput('auditControls', form.controls);

    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('#test-application-code')).toBeNull();
    expect(fixture.nativeElement.querySelector('#test-application-creation-date')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('#test-application-modification-date')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('#test-application-withdrawal-date')).toBeTruthy();
  });

  it('renders an accessible minimum-length error for a short application code', () => {
    const form = createApplicationCreateForm(formBuilder);
    form.controls.code.setValue('123');
    form.controls.code.markAsTouched();
    fixture.componentRef.setInput('controls', form.controls);
    fixture.componentRef.setInput('codeControl', form.controls.code);
    fixture.componentRef.setInput('codeMinLengthError', 'At least 4 characters');

    fixture.detectChanges();

    const input = fixture.nativeElement.querySelector(
      '#test-application-code',
    ) as HTMLInputElement;
    const error = fixture.nativeElement.querySelector('#test-application-code-error');

    expect(input.getAttribute('aria-invalid')).toBe('true');
    expect(input.getAttribute('aria-describedby')).toBe('test-application-code-error');
    expect(error?.textContent.trim()).toBe('At least 4 characters');
  });

  it('uses dynamic selector options when they are provided', () => {
    const form = createApplicationCreateForm(formBuilder);
    const options = {
      categories: [{ label: 'Dynamic category', value: 1 }],
      informationSystems: [{ label: 'Dynamic system', value: 2 }],
      scopes: [{ label: 'Dynamic scope', value: 3 }],
      commissions: [{ label: 'Dynamic commission', value: 4 }],
      administrativeUnits: [{ label: 'Dynamic unit', value: 5 }],
    };

    fixture.componentRef.setInput('controls', form.controls);
    fixture.componentRef.setInput('codeControl', form.controls.code);
    fixture.componentRef.setInput('options', options);
    fixture.detectChanges();

    const component = fixture.componentInstance as unknown as {
      categoryOptions: unknown[];
      informationSystemOptions: unknown[];
      scopeOptions: unknown[];
      commissionOptions: unknown[];
      administrativeUnitOptions: unknown[];
    };

    expect(component.categoryOptions).toEqual(options.categories);
    expect(component.informationSystemOptions).toEqual(options.informationSystems);
    expect(component.scopeOptions).toEqual(options.scopes);
    expect(component.commissionOptions).toEqual(options.commissions);
    expect(component.administrativeUnitOptions).toEqual(options.administrativeUnits);
  });
});
