import { FormBuilder } from '@angular/forms';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { createResponsibleNameForm } from '../../forms/responsible-forms.factory';
import { RESPONSIBLE_COMPANY_COPY } from '../../responsibles.i18n';
import { ResponsibleNameDialog } from './responsible-name-dialog';

describe('ResponsibleNameDialog', () => {
  let fixture: ComponentFixture<ResponsibleNameDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [ResponsibleNameDialog] }).compileComponents();
    fixture = TestBed.createComponent(ResponsibleNameDialog);
    const form = createResponsibleNameForm(new FormBuilder());
    form.setValue({ name: 'Plexus' });
    fixture.componentRef.setInput('visible', true);
    fixture.componentRef.setInput('form', form);
    fixture.componentRef.setInput('mode', 'view');
    fixture.componentRef.setInput('copy', RESPONSIBLE_COMPANY_COPY);
    fixture.componentRef.setInput('idPrefix', 'company-test');
    fixture.detectChanges();
  });

  it('renders view text as readonly and not disabled', () => {
    const input = fixture.nativeElement.querySelector('#company-test-name') as HTMLInputElement;

    expect(input.readOnly).toBe(true);
    expect(input.disabled).toBe(false);
    expect(input.value).toBe('Plexus');
  });
});
