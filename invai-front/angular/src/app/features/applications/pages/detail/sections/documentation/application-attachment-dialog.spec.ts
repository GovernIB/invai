import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';

import {
  ApplicationAttachmentFormGroup,
  createApplicationAttachmentForm,
} from '../../../../forms/application-documentation-form.factory';
import { DOCUMENT_MIME_TYPES, DOCUMENT_SECTIONS } from '../../../../application-documentation.testing';
import { ApplicationAttachmentDialog } from './application-attachment-dialog';

describe('ApplicationAttachmentDialog', () => {
  let fixture: ComponentFixture<ApplicationAttachmentDialog>;
  let form: ApplicationAttachmentFormGroup;
  const byId = (id: string) => document.getElementById(id)!;
  const button = (text: string) =>
    [...document.querySelectorAll('button')].find((element) => element.textContent?.trim() === text)!;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [ApplicationAttachmentDialog] }).compileComponents();
    form = createApplicationAttachmentForm(new FormBuilder(), () => ['pdf', 'docx']);
    fixture = TestBed.createComponent(ApplicationAttachmentDialog);
    fixture.componentRef.setInput('visible', true);
    fixture.componentRef.setInput('mode', 'create');
    fixture.componentRef.setInput('form', form);
    fixture.componentRef.setInput('categoryOptions', [
      { id: 1, label: 'Tècnica', subcategories: [{ id: 11, label: 'Manuals' }] },
    ]);
    fixture.componentRef.setInput(
      'sectionOptions',
      DOCUMENT_SECTIONS.map(({ id, name }) => ({ value: id, label: name! })),
    );
    fixture.componentRef.setInput('mimeTypes', DOCUMENT_MIME_TYPES);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  afterEach(() => fixture.destroy());

  function drop(file: File): void {
    const event = new Event('drop', { bubbles: true, cancelable: true });
    Object.defineProperty(event, 'dataTransfer', { value: { files: [file] } });
    document.querySelector('.application-attachment-upload')!.dispatchEvent(event);
    fixture.detectChanges();
  }

  it('opens the native file chooser through the visible attach button', () => {
    const choose = vi.spyOn(byId('application-attachment-file'), 'click');
    button('Adjunta').click();
    expect(choose).toHaveBeenCalledOnce();
  });

  it('limits the file chooser to the accepted formats and states the maximum size', () => {
    expect(byId('application-attachment-file').getAttribute('accept')).toBe('.pdf,.docx');
    expect(button('Adjunta').getAttribute('aria-describedby')).toBe(
      'application-attachment-file-hint application-attachment-file-limits',
    );
    expect(byId('application-attachment-file-limits').textContent).toContain('Mida màxima: 5 MB.');
  });

  it('accepts a dropped file and marks the owner form as changed', () => {
    drop(new File(['test'], 'manual.pdf', { type: 'application/pdf' }));

    expect(form.controls.file.value?.name).toBe('manual.pdf');
    expect(form.dirty).toBe(true);
    expect(document.querySelector('[role="status"]')?.textContent).toContain('manual.pdf');
    expect(document.querySelector('[role="status"]')?.textContent).toContain('PDF');
  });

  it('names the document type after the accepted format', () => {
    drop(new File(['test'], 'guia.docx'));

    expect(document.querySelector('[role="status"]')?.textContent).toContain('Word');
  });

  it('refuses a file of a format not accepted before it reaches the form, and says why', () => {
    drop(new File(['MZ'], 'programa.exe'));

    expect(form.controls.file.value).toBeNull();
    expect(form.dirty).toBe(false);
    expect(button('Adjunta').getAttribute('aria-invalid')).toBe('true');
    expect(button('Adjunta').getAttribute('aria-describedby')).toContain('application-attachment-file-error');
    expect(byId('application-attachment-file-error').getAttribute('role')).toBe('alert');
    expect(byId('application-attachment-file-error').textContent?.trim()).toBe(
      "No s'ha adjuntat «programa.exe»: el format del fitxer no està admès.",
    );
  });

  it('keeps the chosen file when a refused one is dropped and announces each refusal', () => {
    const manual = new File(['test'], 'manual.pdf');
    drop(manual);
    drop(new File([new Uint8Array(5 * 1024 * 1024 + 1)], 'gran.pdf'));

    expect(form.controls.file.value).toBe(manual);
    expect(document.querySelector('[role="status"]')?.textContent).toContain('manual.pdf');
    const first = byId('application-attachment-file-error');
    expect(first.textContent).toContain("No s'ha adjuntat «gran.pdf»: supera la mida màxima de 5 MB.");

    drop(new File([new Uint8Array(5 * 1024 * 1024 + 1)], 'gran.pdf'));
    // A new alert, so that the same refusal is announced again.
    expect(byId('application-attachment-file-error')).not.toBe(first);

    drop(new File(['test'], 'guia.docx'));
    expect(form.controls.file.value?.name).toBe('guia.docx');
    expect(document.getElementById('application-attachment-file-error')).toBeNull();
  });

  it('does not let attach a file until the accepted formats are known', () => {
    fixture.componentRef.setInput('mimeTypes', []);
    fixture.componentRef.setInput('isCatalogsLoading', true);
    fixture.detectChanges();
    expect(button('Adjunta').disabled).toBe(true);
    expect(byId('application-attachment-file-hint').textContent).toContain("S'estan carregant els formats admesos.");

    fixture.componentRef.setInput('isCatalogsLoading', false);
    fixture.detectChanges();
    drop(new File(['test'], 'manual.pdf'));
    expect(form.controls.file.value).toBeNull();
    expect(byId('application-attachment-file-hint').textContent).toContain(
      "No s'han pogut carregar els formats admesos: ara no es pot adjuntar cap fitxer.",
    );
  });

  it('keeps the existing selection when the file picker is cancelled', () => {
    const file = new File(['test'], 'manual.pdf');
    form.controls.file.setValue(file);
    byId('application-attachment-file').dispatchEvent(new Event('change'));
    expect(form.controls.file.value).toBe(file);
  });

  it('removes a selected file and associates the error with the attach button', () => {
    drop(new File(['test'], 'manual.pdf'));
    document.querySelector<HTMLButtonElement>('button[aria-label="Elimina el fitxer seleccionat"]')!.click();
    fixture.detectChanges();

    expect(form.controls.file.value).toBeNull();
    expect(button('Adjunta').getAttribute('aria-invalid')).toBe('true');
    expect(button('Adjunta').getAttribute('aria-describedby')).toContain('application-attachment-file-error');
    expect(byId('application-attachment-file-error').textContent).toContain('Camp obligatori');
    expect(document.activeElement).toBe(button('Adjunta'));
  });

  it('prevents file changes and submission while saving', () => {
    const submit = vi.fn();
    fixture.componentInstance.submitForm.subscribe(submit);
    const existing = new File(['test'], 'existing.pdf');
    form.controls.file.setValue(existing);
    fixture.componentRef.setInput('isSaving', true);
    fixture.detectChanges();
    drop(new File(['test'], 'replacement.pdf'));
    button('Desa').click();

    expect(form.controls.file.value).toBe(existing);
    expect(button('Adjunta').disabled).toBe(true);
    expect(submit).not.toHaveBeenCalled();
  });

  it('reflects owner validation on the native combobox and its visible error', async () => {
    form.markAllAsTouched();
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    for (const field of ['section', 'category']) {
      const id = 'application-attachment-' + field;
      expect(byId(id).getAttribute('aria-invalid')).toBe('true');
      expect(byId(id).getAttribute('aria-describedby')).toBe(id + '-error');
      expect(byId(id + '-error').textContent).toContain('Camp obligatori');
    }
    const version = byId('application-attachment-version');
    expect([version.getAttribute('aria-invalid'), version.getAttribute('aria-describedby')]).toEqual([
      'true',
      'application-attachment-version-error',
    ]);
  });
});
