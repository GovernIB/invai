import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, Validators } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { Select } from 'primeng/select';

import { ROLE_TRANSFER_COPY } from '../../responsibles.i18n';
import {
  selectedRoleTransferPersonValidator,
  transferableRoleSourceValidator,
} from '../../forms/responsible-forms.factory';
import {
  RoleTransferPersonControlValue,
  RoleTransferPersonOption,
} from '../../responsibles.model';
import { RoleTransferPersonField } from './role-transfer-person-field';

const OPTION: RoleTransferPersonOption = {
  id: 1,
  firstName: 'Maria',
  lastName: 'Tur',
  email: 'maria.tur@caib.es',
  label: 'Maria Tur',
  source: 'database',
  disabled: false,
};

const UNAVAILABLE_OPTION: RoleTransferPersonOption = {
  ...OPTION,
  id: null,
  source: 'soffid',
  disabled: true,
};

describe('RoleTransferPersonField', () => {
  let fixture: ComponentFixture<RoleTransferPersonField>;
  let control: FormControl<RoleTransferPersonControlValue>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [RoleTransferPersonField] }).compileComponents();
    fixture = TestBed.createComponent(RoleTransferPersonField);
    control = new FormControl<RoleTransferPersonControlValue>(null, [
      Validators.required,
      selectedRoleTransferPersonValidator,
      transferableRoleSourceValidator,
    ]);
    fixture.componentRef.setInput('kind', 'source');
    fixture.componentRef.setInput('control', control);
    fixture.componentRef.setInput('options', [OPTION]);
    fixture.detectChanges();
  });

  it('configures an accessible searchable select', () => {
    const select = fixture.debugElement.query(By.directive(Select)).componentInstance as Select;
    const combobox = fixture.nativeElement.querySelector('[role="combobox"]') as HTMLElement;

    expect(select.filter).toBe(true);
    expect(select.filterBy).toBe('label,email');
    expect(select.optionDisabled).toBe('disabled');
    expect(select.resetFilterOnHide).toBe(true);
    expect(select.ariaFilterLabel).toBe("Persona d'origen");
    expect(combobox.id).toBe('role-transfer-source');
    expect(combobox.getAttribute('aria-labelledby')).toBe('role-transfer-source-label');
    expect(combobox.getAttribute('aria-describedby')).toContain(
      'role-transfer-source-instruction',
    );
  });

  it('exposes the unavailable-source error if a null-id option is assigned programmatically', () => {
    control.setValue(UNAVAILABLE_OPTION);
    control.markAsTouched();
    fixture.detectChanges();

    expect(control.hasError('sourceWithoutLocalId')).toBe(true);
    const alert = fixture.nativeElement.querySelector('[role="alert"]') as HTMLElement;
    expect(
      (fixture.nativeElement.querySelector('[role="combobox"]') as HTMLElement).getAttribute(
        'aria-invalid',
      ),
    ).toBe('true');
    expect(alert.classList.contains('sr-only')).toBe(true);
    expect(alert.textContent).toContain('registre local');
  });

  it('does not render the destination required message below the select', () => {
    fixture.componentRef.setInput('kind', 'destination');
    control.setValue(null);
    control.markAsTouched();
    fixture.detectChanges();

    expect(control.hasError('required')).toBe(true);
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeNull();
    expect(fixture.nativeElement.textContent).not.toContain('Selecciona una persona de destinació.');
  });

  it('shows loading, instruction, failure, and empty copy only in their matching states', () => {
    fixture.componentRef.setInput('loading', true);
    fixture.detectChanges();

    let select = fixture.debugElement.query(By.directive(Select)).componentInstance as Select;
    const status = fixture.nativeElement.querySelector('[role="status"]') as HTMLElement;
    expect(select.emptyMessage).toContain('Cercant');
    expect(status.classList.contains('sr-only')).toBe(true);
    expect(status.textContent).toContain('Cercant');

    fixture.componentRef.setInput('loading', false);
    fixture.componentRef.setInput('searchError', true);
    fixture.detectChanges();

    select = fixture.debugElement.query(By.directive(Select)).componentInstance as Select;
    const alert = fixture.nativeElement.querySelector('[role="alert"]') as HTMLElement;
    expect(select.emptyMessage).toContain("No s'han pogut cercar");
    expect(alert.classList.contains('sr-only')).toBe(true);

    fixture.componentRef.setInput('searchError', false);
    fixture.componentRef.setInput('searched', false);
    fixture.detectChanges();
    select = fixture.debugElement.query(By.directive(Select)).componentInstance as Select;
    expect(select.emptyMessage).toContain('almenys tres caràcters');

    fixture.componentRef.setInput('searched', true);
    fixture.componentRef.setInput('options', []);
    fixture.detectChanges();
    select = fixture.debugElement.query(By.directive(Select)).componentInstance as Select;
    expect(select.emptyMessage).toBe(ROLE_TRANSFER_COPY.noPeople);
  });

  it('loads the initial options on open and debounces trimmed remote filters', () => {
    vi.useFakeTimers();
    const requested = vi.fn();
    fixture.componentInstance.searchRequested.subscribe(requested);

    (
      fixture.componentInstance as unknown as {
        onOpen(): void;
        onFilter(event: { originalEvent: Event; filter: string }): void;
      }
    ).onOpen();
    (
      fixture.componentInstance as unknown as {
        onFilter(event: { originalEvent: Event; filter: string }): void;
      }
    ).onFilter({ originalEvent: new Event('input'), filter: '  Maria  ' });

    expect(requested).toHaveBeenCalledWith('');
    expect(requested).not.toHaveBeenCalledWith('Maria');
    vi.advanceTimersByTime(300);
    expect(requested).toHaveBeenCalledWith('Maria');
    vi.useRealTimers();
  });
});
