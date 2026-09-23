import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ApplicationAssignmentDetail, ApplicationAssignmentDetailDialog } from './application-assignment-detail-dialog';

const person = {
  id: 1, firstName: 'Maria', lastName: 'Tur', email: 'maria@example.test',
  company: { nif: null, id: 2, name: 'Example company', deletedAt: null }, personalCaib: false, deletedAt: null,
};

describe('ApplicationAssignmentDetailDialog', () => {
  async function render(detail: ApplicationAssignmentDetail, locale = 'ca') {
    await TestBed.configureTestingModule({
      imports: [ApplicationAssignmentDetailDialog],
      providers: [{ provide: LOCALE_ID, useValue: locale }],
    }).compileComponents();
    const fixture = TestBed.createComponent(ApplicationAssignmentDetailDialog);
    fixture.componentRef.setInput('detail', detail);
    fixture.detectChanges();
    return fixture;
  }

  it('shows all responsible details in labelled readonly controls with only Accept and Close', async () => {
    const observation = 'Long observation\n'.repeat(30);
    const fixture = await render({ kind: 'responsible', assignment: {
      id: 3, appResponsibleAuthorizedId: 91, person,
      responsibleType: { id: 1, name: 'Seguretat', nameEs: 'Seguridad', requiresPersonalCaib: true },
      jobTitle: 'Head of department', observation, deletedAt: null,
    } });
    const root = fixture.nativeElement as HTMLElement;
    const controls = [...root.querySelectorAll<HTMLInputElement | HTMLTextAreaElement>('input, textarea')];
    expect(controls.map((item) => item.value)).toEqual([
      'Maria', 'Tur', 'maria@example.test', 'Seguretat', 'No', 'Head of department', 'Example company', observation,
    ]);
    controls.forEach((control) => {
      expect(control.readOnly).toBe(true);
      expect(control.disabled).toBe(false);
      expect(control.labels).toHaveLength(1);
      expect(control.classList.contains('invai-form-readonly-control')).toBe(true);
    });
    expect(root.querySelector('select, form')).toBeNull();
    expect(root.querySelectorAll('button')).toHaveLength(2);
    const closed = vi.spyOn(fixture.componentInstance.closed, 'emit');
    root.querySelector<HTMLButtonElement>('button[aria-label="Accepta la consulta del responsable"]')!.click();
    expect(closed).toHaveBeenCalledOnce();
  });

  it('localizes authorization names and exposes empty values without edit controls', async () => {
    const fixture = await render({ kind: 'authorized', assignment: {
      id: 4, appResponsibleAuthorizedId: 91,
      person: { ...person, personalCaib: true, company: null, email: '' },
      authorizationTypes: [{ id: 1, name: 'Signatura', nameEs: 'Firma', deletedAt: null }],
      observation: null, deletedAt: null,
    } }, 'es');
    const root = fixture.nativeElement as HTMLElement;
    expect([...root.querySelectorAll<HTMLInputElement | HTMLTextAreaElement>('input, textarea')].map((item) => item.value)).toEqual([
      'Maria', 'Tur', '—', 'Sí', 'Firma', '—',
    ]);
    expect(root.querySelectorAll('button')).toHaveLength(2);
  });

  it.each(['responsible', 'authorized'] as const)('hides company for CAIB personnel in the %s consultation', async (kind) => {
    const assignment = {
      id: 5, appResponsibleAuthorizedId: 91,
      person: { ...person, personalCaib: true },
      responsibleType: { id: 1, name: 'Seguretat', nameEs: 'Seguridad', requiresPersonalCaib: true },
      jobTitle: 'Head of department', authorizationTypes: [], observation: null, deletedAt: null,
    };
    const detail: ApplicationAssignmentDetail = kind === 'responsible'
      ? { kind: 'responsible', assignment }
      : { kind: 'authorized', assignment };
    const fixture = await render(detail);
    const root = fixture.nativeElement as HTMLElement;
    expect(root.querySelector('input[id$="-company"]')).toBeNull();
    expect(root.querySelector<HTMLInputElement>('input[id$="-personal-caib"]')!.value).toBe('Sí');

    fixture.componentRef.setInput('detail', { kind, assignment: { ...assignment, person } });
    fixture.detectChanges();
    expect(root.querySelector<HTMLInputElement>('input[id$="-company"]')!.value).toBe(person.company.name);
  });
});
