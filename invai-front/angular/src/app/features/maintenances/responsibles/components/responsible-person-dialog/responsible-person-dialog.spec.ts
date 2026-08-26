import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';

import { createResponsiblePersonForm } from '../../forms/responsible-forms.factory';
import { ResponsiblePersonDialog } from './responsible-person-dialog';

describe('ResponsiblePersonDialog', () => {
  let fixture: ComponentFixture<ResponsiblePersonDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResponsiblePersonDialog],
    }).compileComponents();
    fixture = TestBed.createComponent(ResponsiblePersonDialog);
    const form = createResponsiblePersonForm(new FormBuilder());
    form.setValue({
      companyId: 1,
      firstName: 'Maria',
      lastName: 'Tur Roig',
      email: 'maria.tur@invai.es',
    });
    fixture.componentRef.setInput('visible', true);
    fixture.componentRef.setInput('form', form);
    fixture.componentRef.setInput('mode', 'view');
    fixture.componentRef.setInput('companyOptions', [{ id: 1, label: 'Plexus' }]);
    fixture.componentRef.setInput('companyName', 'Plexus');
    fixture.detectChanges();
  });

  it('uses one static labelled company and readonly text fields in view mode', () => {
    const companyValue = fixture.nativeElement.querySelector(
      '[aria-labelledby="responsible-person-dialog-company-label"]',
    );
    const textInputs = fixture.nativeElement.querySelectorAll(
      'input[pinputtext]',
    ) as NodeListOf<HTMLInputElement>;

    expect(companyValue.textContent?.trim()).toBe('Plexus');
    expect(fixture.nativeElement.querySelector('p-select')).toBeNull();
    expect(Array.from(textInputs).every((input) => input.readOnly && !input.disabled)).toBe(true);
  });
});
