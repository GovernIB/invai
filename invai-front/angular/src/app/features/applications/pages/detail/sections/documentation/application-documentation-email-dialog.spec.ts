import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { CrudEntityDialog } from '@components/crud-entity-dialog/crud-entity-dialog';
import { MessageService } from 'primeng/api';
import { TableLazyLoadEvent } from 'primeng/table';
import { of, throwError } from 'rxjs';

import { ApplicationAttachment } from '../../../../application-documentation.model';
import {
  documentationPage,
  documentDetail,
  documentRow,
  documentVersion,
} from '../../../../application-documentation.testing';
import { ApplicationDocumentationEmailFormGroup } from '../../../../forms/application-documentation-email-form.factory';
import { ApplicationDocumentationService } from '../../../../services/application-documentation.service';
import { ApplicationDocumentationEmailDialog } from './application-documentation-email-dialog';
import {
  ApplicationDocumentationEmailSelection,
  ApplicationDocumentationEmailVersionChange,
} from './application-documentation-email.types';

const ROWS = [
  documentRow(),
  documentRow({ id: 2, title: 'Pla de proves', versionLabel: '1.0', currentAttachmentId: 30 }),
];

const PLAN = documentDetail({
  id: 2,
  title: 'Pla de proves',
  attachments: [documentVersion({ id: 30, versionLabel: '1.0', fileSize: 1_000 })],
});

interface DialogInternals {
  form: ApplicationDocumentationEmailFormGroup;
  selection: () => ReadonlyMap<number, ApplicationDocumentationEmailSelection>;
  toggle(attachment: ApplicationAttachment): void;
  changeVersion(change: ApplicationDocumentationEmailVersionChange): void;
  onRecipientAdded(): void;
  onQuickSearchChange(value: string): void;
  onPageChange(event: TableLazyLoadEvent): void;
  send(): void;
}

describe('ApplicationDocumentationEmailDialog', () => {
  let fixture: ComponentFixture<ApplicationDocumentationEmailDialog>;
  let dialog: DialogInternals;
  let add: ReturnType<typeof vi.spyOn>;
  let closed: ReturnType<typeof vi.fn<() => void>>;
  const documents = {
    getAttachments: vi.fn(),
    getDocument: vi.fn(),
    sendByEmail: vi.fn(),
  };

  const byId = (id: string) => document.getElementById(id);
  const render = () => fixture.detectChanges();

  beforeEach(async () => {
    // The overlays of PrimeNG listen to a breakpoint media query that jsdom does not implement.
    vi.stubGlobal(
      'matchMedia',
      vi.fn(() => ({ matches: false, addEventListener: vi.fn(), removeEventListener: vi.fn() })),
    );
    documents.getAttachments.mockReset().mockReturnValue(of(documentationPage(ROWS)));
    documents.getDocument
      .mockReset()
      .mockImplementation((id: number) => of(id === 2 ? PLAN : documentDetail()));
    documents.sendByEmail.mockReset().mockReturnValue(of(undefined));
    await TestBed.configureTestingModule({
      imports: [ApplicationDocumentationEmailDialog],
      providers: [MessageService, { provide: ApplicationDocumentationService, useValue: documents }],
    }).compileComponents();
    add = vi.spyOn(TestBed.inject(MessageService), 'add');
    fixture = TestBed.createComponent(ApplicationDocumentationEmailDialog);
    fixture.componentRef.setInput('applicationId', 7);
    fixture.componentRef.setInput('applicationCode', 'GESTIB');
    fixture.componentRef.setInput('visible', true);
    closed = vi.fn<() => void>();
    fixture.componentInstance.closed.subscribe(closed);
    render();
    await fixture.whenStable();
    render();
    dialog = fixture.componentInstance as unknown as DialogInternals;
  });

  afterEach(() => {
    fixture.destroy();
    vi.useRealTimers();
    vi.unstubAllGlobals();
  });

  function fillForm(recipients = ['ana@example.org']): void {
    dialog.form.controls.recipients.setValue(recipients);
    dialog.form.controls.message.setValue('Us adjunto el manual.');
  }

  it('proposes the subject with the application code and reads the documents by title', () => {
    expect(dialog.form.getRawValue()).toEqual({
      recipients: [],
      subject: "Documentació de l'aplicació GESTIB",
      message: '',
    });
    expect(documents.getAttachments).toHaveBeenCalledWith(7, {
      page: 0,
      size: 10,
      sort: 'title,asc',
      deleted: false,
    });
    expect(document.querySelector('.p-dialog h2')?.textContent?.trim()).toBe('Envia per correu');
    expect(byId('application-documentation-email-instructions')?.textContent).toContain(
      'Tots els camps són obligatoris',
    );
    expect(document.querySelector('label[for="application-documentation-email-recipients"]')).toBeTruthy();
    expect(byId('application-documentation-email-recipients')?.getAttribute('aria-describedby')).toBe(
      'application-documentation-email-recipients-hint',
    );
    expect(document.querySelectorAll('.application-documentation-email-table tbody tr')).toHaveLength(2);
  });

  it('offers the send action with its own label and icon instead of Add', () => {
    const send = [...document.querySelectorAll('button')].find(
      (button) => button.textContent?.trim() === 'Envia',
    );

    expect(send?.querySelector('.pi-send')).toBeTruthy();
    expect([...document.querySelectorAll('button')].some((b) => b.textContent?.trim() === 'Cancel·la')).toBe(true);
  });

  it('chooses a document with its current version and reads its versions only once', () => {
    dialog.toggle(ROWS[0]);
    render();

    expect(dialog.selection().get(1)).toEqual({
      documentId: 1,
      title: 'Manual API v2',
      versionId: 20,
      versions: [
        { id: 20, label: '2.1', fileSize: 2_300_000 },
        { id: 19, label: '2.0', fileSize: 1_800_000 },
      ],
    });
    expect(document.querySelector('.application-documentation-email__count')?.textContent?.trim()).toBe(
      '1 element seleccionat',
    );

    dialog.toggle(ROWS[0]);
    dialog.toggle(ROWS[0]);

    expect(dialog.selection().size).toBe(1);
    expect(documents.getDocument).toHaveBeenCalledOnce();
  });

  it('keeps the current version when the versions of a document cannot be read', () => {
    documents.getDocument.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 500 })));

    dialog.toggle(ROWS[0]);

    expect(dialog.selection().get(1)?.versions).toEqual([{ id: 20, label: '2.1', fileSize: null }]);
    expect(add).toHaveBeenCalledWith(
      expect.objectContaining({
        severity: 'warn',
        detail: "No s'han pogut carregar les versions de «Manual API v2»: s'enviarà la versió vigent.",
      }),
    );
  });

  it('does not send without recipients or documents and says what is missing', () => {
    dialog.send();
    render();

    expect(documents.sendByEmail).not.toHaveBeenCalled();
    expect(byId('application-documentation-email-recipients-error')?.textContent?.trim()).toBe(
      'Indica com a mínim un destinatari.',
    );
    expect(byId('application-documentation-email-recipients')?.getAttribute('aria-invalid')).toBe('true');
    expect(byId('application-documentation-email-recipients')?.getAttribute('aria-describedby')).toBe(
      'application-documentation-email-recipients-hint application-documentation-email-recipients-error',
    );
    expect(byId('application-documentation-email-selection-error')?.textContent?.trim()).toBe(
      'Selecciona com a mínim un adjunt.',
    );
    const group = document.querySelector('[role="group"][aria-labelledby="application-documentation-email-selection-title"]');
    expect(group?.getAttribute('aria-describedby')).toBe('application-documentation-email-selection-error');
  });

  it('names the addresses that are not valid', () => {
    dialog.form.controls.recipients.setValue(['ana@example.org', 'pere']);
    dialog.form.controls.recipients.markAsTouched();
    render();

    expect(byId('application-documentation-email-recipients-error')?.textContent?.trim()).toBe(
      'Aquestes adreces no són vàlides: pere.',
    );
  });

  it('keeps the added addresses trimmed, in lower case and once', () => {
    dialog.form.controls.recipients.setValue(['ana@example.org', ' Ana@Example.org ', 'PERE@caib.es']);

    dialog.onRecipientAdded();

    expect(dialog.form.controls.recipients.value).toEqual(['ana@example.org', 'pere@caib.es']);
  });

  describe('the addresses', () => {
    const ADDRESSES = ['ana@example.org', 'pere', 'joan@caib.es'];
    const recipientsInput = () =>
      byId('application-documentation-email-recipients') as HTMLInputElement;
    const chips = () => [...document.querySelectorAll<HTMLElement>('.p-autocomplete-chip-item')];
    const editor = () => document.querySelector<HTMLInputElement>('.p-autocomplete-chip-item input');
    const press = (target: EventTarget, key: string, code = key) =>
      target.dispatchEvent(new KeyboardEvent('keydown', { key, code, bubbles: true, cancelable: true }));
    const doubleClick = (target: EventTarget) => {
      target.dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));
      render();
    };
    const retype = (value: string) => {
      editor()!.value = value;
      editor()!.dispatchEvent(new Event('input', { bubbles: true }));
    };

    beforeEach(() => {
      dialog.form.controls.recipients.setValue(ADDRESSES);
      render();
    });

    it('adds the address typed with a space', () => {
      recipientsInput().value = 'Nou@caib.es';

      const typed = press(recipientsInput(), ' ', 'Space');
      render();

      expect(typed).toBe(false);
      expect(dialog.form.controls.recipients.value).toEqual([...ADDRESSES, 'nou@caib.es']);
      expect(recipientsInput().value).toBe('');
    });

    it('does not let an address start with a comma, a semicolon or a space', () => {
      expect([',', ';', ' '].map((key) => press(recipientsInput(), key))).toEqual([false, false, false]);
      expect(press(recipientsInput(), 'a')).toBe(true);
      expect(dialog.form.controls.recipients.value).toEqual(ADDRESSES);
    });

    it('edits the address double clicked inside its chip, without moving it', () => {
      doubleClick(chips()[1].querySelector('.p-chip-label')!);

      expect(chips()[1].contains(editor())).toBe(true);
      expect(editor()?.value).toBe('pere');
      expect(editor()?.getAttribute('aria-label')).toBe("Canvia l'adreça pere");
      expect(document.activeElement).toBe(editor());
      expect(dialog.form.controls.recipients.value).toEqual(ADDRESSES);

      retype('Pere@caib.es');
      press(editor()!, ' ', 'Space');
      render();

      expect(dialog.form.controls.recipients.value).toEqual([
        'ana@example.org',
        'pere@caib.es',
        'joan@caib.es',
      ]);
      expect(chips().map((chip) => chip.textContent?.trim())).toEqual([
        'ana@example.org',
        'pere@caib.es',
        'joan@caib.es',
      ]);
      expect(editor()).toBeNull();
      expect(document.activeElement).toBe(recipientsInput());
    });

    it('keeps the change of an address when leaving it', () => {
      doubleClick(chips()[0]);
      retype('anna@example.org');

      editor()!.dispatchEvent(new FocusEvent('blur'));
      render();

      expect(dialog.form.controls.recipients.value).toEqual(['anna@example.org', 'pere', 'joan@caib.es']);
    });

    it('takes out the address emptied', () => {
      doubleClick(chips()[1]);
      retype('  ');

      press(editor()!, 'Enter');
      render();

      expect(dialog.form.controls.recipients.value).toEqual(['ana@example.org', 'joan@caib.es']);
    });

    it('gives up the change with Escape without closing the dialog', () => {
      const escaped = vi.fn();
      document.addEventListener('keydown', escaped);
      doubleClick(chips()[0]);
      retype('altra@example.org');

      press(editor()!, 'Escape');
      render();
      document.removeEventListener('keydown', escaped);

      expect(dialog.form.controls.recipients.value).toEqual(ADDRESSES);
      expect(editor()).toBeNull();
      expect(escaped).not.toHaveBeenCalled();
      expect(closed).not.toHaveBeenCalled();
    });

    it('edits with Return the address reached with the arrows', () => {
      const list = document.querySelector<HTMLElement>('.p-autocomplete-input-multiple')!;
      recipientsInput().focus();
      press(recipientsInput(), 'ArrowLeft');
      press(list, 'ArrowLeft');

      press(list, 'Enter');
      render();

      expect(chips()[1].contains(editor())).toBe(true);
      expect(document.activeElement).toBe(editor());
    });
  });

  it('sends the chosen versions and closes once the mail server has accepted the e-mail', () => {
    fillForm([' Ana@Example.org', 'ana@example.org']);
    dialog.form.controls.message.setValue('   ');
    dialog.toggle(ROWS[0]);
    dialog.changeVersion({ documentId: 1, versionId: 19 });
    dialog.toggle(ROWS[1]);

    dialog.send();

    expect(documents.sendByEmail).toHaveBeenCalledWith({
      applicationId: 7,
      recipients: ['ana@example.org'],
      subject: "Documentació de l'aplicació GESTIB",
      message: null,
      attachments: [
        { documentId: 1, versionId: 19 },
        { documentId: 2, versionId: 30 },
      ],
    });
    expect(add).toHaveBeenCalledWith({
      severity: 'success',
      summary: 'Correu enviat',
      detail: "S'ha enviat el correu a 1 destinatari.",
    });
    expect(closed).toHaveBeenCalledOnce();
  });

  it.each([
    [
      new HttpErrorResponse({
        status: 502,
        error: { error: 'Error', message: "No s'ha pogut enviar el correu. Torneu-ho a provar més tard." },
      }),
      "No s'ha pogut enviar el correu. Torneu-ho a provar més tard.",
    ],
    [new HttpErrorResponse({ status: 403 }), 'No tens permís per enviar algun dels adjunts seleccionats.'],
    [new HttpErrorResponse({ status: 500 }), "No s'ha pogut enviar el correu."],
  ])('stays open and explains a failed send (%#)', (error, detail) => {
    documents.sendByEmail.mockReturnValue(throwError(() => error));
    fillForm();
    dialog.toggle(ROWS[0]);

    dialog.send();

    expect(add).toHaveBeenCalledWith({ severity: 'error', summary: 'Error', detail });
    expect(closed).not.toHaveBeenCalled();
  });

  it('leaves a refused request to the error dialog of the back-end message', () => {
    documents.sendByEmail.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: { error: 'Error de validació', message: 'Heu de seleccionar com a mínim un adjunt.' },
          }),
      ),
    );
    fillForm();
    dialog.toggle(ROWS[0]);

    dialog.send();

    expect(add).not.toHaveBeenCalledWith(expect.objectContaining({ severity: 'error' }));
    expect(closed).not.toHaveBeenCalled();
  });

  it('does not send attachments that add up to more than 15 MB', () => {
    documents.getDocument.mockReturnValue(
      of(documentDetail({ attachments: [documentVersion({ fileSize: 16 * 1024 * 1024 })] })),
    );
    fillForm();
    dialog.toggle(ROWS[0]);
    render();

    dialog.send();

    expect(documents.sendByEmail).not.toHaveBeenCalled();
    expect(byId('application-documentation-email-selection-error')?.textContent?.trim()).toBe(
      'Els adjunts seleccionats superen la mida màxima de 15 MB.',
    );
  });

  it('searches by title after a pause and keeps the chosen documents across pages', () => {
    dialog.toggle(ROWS[0]);
    vi.useFakeTimers();

    dialog.onQuickSearchChange(' plec ');
    vi.advanceTimersByTime(400);
    dialog.onPageChange({ first: 10, rows: 10 });

    expect(documents.getAttachments.mock.calls.map(([, params]) => params)).toEqual([
      { page: 0, size: 10, sort: 'title,asc', deleted: false },
      { page: 0, size: 10, sort: 'title,asc', deleted: false, search: 'plec' },
      { page: 1, size: 10, sort: 'title,asc', deleted: false, search: 'plec' },
    ]);
    expect(dialog.selection().has(1)).toBe(true);
  });

  it('keeps a click outside from closing it once it holds an address, a text or a document', () => {
    const shell = fixture.debugElement.query(By.directive(CrudEntityDialog))
      .componentInstance as CrudEntityDialog;
    const hasData = () => shell.hasUnsavedChanges()();
    expect(hasData()).toBe(false);

    dialog.toggle(ROWS[1]);
    expect(hasData()).toBe(true);

    dialog.toggle(ROWS[1]);
    expect(hasData()).toBe(false);

    dialog.form.controls.recipients.setValue(['ana@example.org']);
    dialog.onRecipientAdded();
    expect(hasData()).toBe(true);
  });

  it('closes through Cancel without sending', () => {
    const cancel = [...document.querySelectorAll('button')].find(
      (button) => button.textContent?.trim() === 'Cancel·la',
    )!;

    cancel.click();

    expect(closed).toHaveBeenCalledOnce();
    expect(documents.sendByEmail).not.toHaveBeenCalled();
  });
});
