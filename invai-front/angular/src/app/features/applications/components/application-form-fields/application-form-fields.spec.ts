import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { CommissionType } from '@features/commissions/commissions.model';
import { Select } from 'primeng/select';

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
    commissionSectionTitle: 'IT commission',
    commissionName: 'Name',
    commissionExpedientNumber: 'Expedient number',
    commissionApprovalDate: 'Commission date',
    commissionType: 'Commission type',
    prefix: 'Prefix',
    administrativeUnit: 'Administrative unit',
    conselleria: 'Conselleria',
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
    fixture.componentRef.setInput('prefixMaxLengthError', 'At most 3 characters');
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
    expect(prefixInput.maxLength).toBe(3);
    expect(
      (fixture.nativeElement.querySelector('#test-application-code') as HTMLInputElement).maxLength,
    ).toBe(10);
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

  it('assigns semantic desktop spans to fields according to their expected content', () => {
    const form = createApplicationCreateForm(formBuilder);
    fixture.componentRef.setInput('controls', form.controls);
    fixture.componentRef.setInput('codeControl', form.controls.code);

    fixture.detectChanges();

    expectDesktopSpan(fixture, 'application', 4);
    expectDesktopSpan(fixture, 'category', 3);
    expectDesktopSpan(fixture, 'information-system', 3);
    expectDesktopSpan(fixture, 'scope', 2);
    expectDesktopSpan(fixture, 'prefix', 2);
    expectDesktopSpan(fixture, 'code', 2);
    expectDesktopSpan(fixture, 'administrative-unit', 4);
    expectDesktopSpan(fixture, 'conselleria', 4);
    expectDesktopSpan(fixture, 'commission', 4);
    expectDesktopSpan(fixture, 'commission-expedient-number', 3);
    expectDesktopSpan(fixture, 'commission-approval-date', 3);
    expectDesktopSpan(fixture, 'commission-type', 2);

    const descriptionField = fixture.nativeElement
      .querySelector('#test-application-description')
      .closest('.application-form-fields__field') as HTMLElement;
    expect(descriptionField.classList.contains('application-form-fields__field--full')).toBe(true);
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

  it('renders accessible maximum-length errors for prefix and code', () => {
    const form = createApplicationCreateForm(formBuilder);
    form.controls.prefix.setValue('INVAI');
    form.controls.prefix.markAsTouched();
    form.controls.code.setValue('12345678901');
    form.controls.code.markAsTouched();
    fixture.componentRef.setInput('controls', form.controls);
    fixture.componentRef.setInput('codeControl', form.controls.code);
    fixture.componentRef.setInput('codeMaxLengthError', 'At most 10 characters');

    fixture.detectChanges();

    const prefixInput = fixture.nativeElement.querySelector(
      '#test-application-prefix',
    ) as HTMLInputElement;
    const codeInput = fixture.nativeElement.querySelector(
      '#test-application-code',
    ) as HTMLInputElement;
    const prefixError = fixture.nativeElement.querySelector('#test-application-prefix-error');
    const codeError = fixture.nativeElement.querySelector('#test-application-code-error');

    expect(prefixInput.getAttribute('aria-invalid')).toBe('true');
    expect(prefixInput.getAttribute('aria-describedby')).toBe('test-application-prefix-error');
    expect(prefixError?.textContent.trim()).toBe('At most 3 characters');
    expect(codeInput.getAttribute('aria-invalid')).toBe('true');
    expect(codeInput.getAttribute('aria-describedby')).toBe('test-application-code-error');
    expect(codeError?.textContent.trim()).toBe('At most 10 characters');
  });

  it('uses dynamic selector options when they are provided', () => {
    const form = createApplicationCreateForm(formBuilder);
    const options = {
      categories: [{ label: 'Dynamic category', value: 1 }],
      informationSystems: [{ label: 'Dynamic system', value: 2 }],
      scopes: [{ label: 'Dynamic scope', value: 3 }],
      commissions: [
        {
          label: 'Dynamic commission',
          value: 4,
          expedientNumber: 'EXP-4',
          approvalDate: '2026-07-14',
          commissionType: CommissionType.TECNICA,
        },
      ],
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

  it('filters every maintenance-backed selector with an accessible label', () => {
    const form = createApplicationCreateForm(formBuilder);
    fixture.componentRef.setInput('controls', form.controls);
    fixture.componentRef.setInput('codeControl', form.controls.code);
    fixture.detectChanges();

    const selects = fixture.debugElement
      .queryAll(By.directive(Select))
      .map(({ componentInstance }) => componentInstance as Select);

    expect(selects).toHaveLength(5);
    expect(selects.every((select) => select.filter === true)).toBe(true);
    expect(selects.every((select) => Boolean(select.ariaFilterLabel))).toBe(
      true,
    );
  });

  it('renders the commission group and emits the selected commission metadata', () => {
    const form = createApplicationCreateForm(formBuilder);
    const commission = {
      label: 'Technical commission',
      value: 4,
      expedientNumber: 'EXP-4',
      approvalDate: '2026-07-14',
      commissionType: CommissionType.TECNICA,
    };
    const selected = vi.fn();

    form.controls.commissionType.setValue(CommissionType.TECNICA);
    fixture.componentRef.setInput('controls', form.controls);
    fixture.componentRef.setInput('options', { commissions: [commission] });
    fixture.componentInstance.commissionSelected.subscribe(selected);
    fixture.detectChanges();

    const component = fixture.componentInstance as unknown as {
      selectCommission: (id: number) => void;
    };
    component.selectCommission(4);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('fieldset legend')?.textContent.trim()).toBe(
      'IT commission',
    );
    expect(selected).toHaveBeenCalledWith(commission);
    expect(
      fixture.nativeElement.querySelector('#test-application-commission-expedient-number'),
    ).toBeTruthy();
    expect(
      fixture.nativeElement.querySelector('#test-application-commission-approval-date'),
    ).toBeTruthy();
    expect(
      (fixture.nativeElement.querySelector('#test-application-commission-type') as HTMLInputElement)
        .value,
    ).toBe('Tècnica');
  });
});

function expectDesktopSpan(
  fixture: ComponentFixture<ApplicationFormFields>,
  field: string,
  span: number,
): void {
  const fieldElement = fixture.nativeElement
    .querySelector(`#test-application-${field}`)
    .closest('.application-form-fields__field') as HTMLElement;

  expect(
    fieldElement.classList.contains(`application-form-fields__field--desktop-${span}`),
  ).toBe(true);
}
