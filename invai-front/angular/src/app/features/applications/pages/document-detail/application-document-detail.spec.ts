import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { BreadcrumbService } from '@core/components/breadcrumbs';
import { MessageService } from 'primeng/api';
import { firstValueFrom, Observable, of, throwError } from 'rxjs';
import { ApplicationDocumentDetailData, ApplicationDocumentVersion } from '../../application-documentation.model';
import {
  DOCUMENT_MIME_TYPES,
  DOCUMENT_STATUSES,
  documentDetail,
  documentVersion,
} from '../../application-documentation.testing';
import { ApplicationDocumentDetailForm } from '../../forms/application-document-detail-form.factory';
import { ApplicationDocumentMimeTypeCatalogService } from '../../services/application-document-mime-type-catalog.service';
import { ApplicationDocumentationService } from '../../services/application-documentation.service';
import { ApplicationDocumentDetail } from './application-document-detail';
import { ApplicationDocumentDetailLoader } from './application-document-detail.loader';
import { DOCUMENT_DETAIL_RESOLVE_KEY } from './application-document-detail.resolver';
import { DocumentVersionAction } from './application-document-versions-table';

interface PageInternals {
  form: ApplicationDocumentDetailForm;
  versionForm: FormGroup;
  detail: () => ApplicationDocumentDetailData | null;
  versions: () => { items: ApplicationDocumentVersion[]; total: number };
  isSaving: () => boolean;
  isEditing: () => boolean;
  versionDialogVisible: () => boolean;
  startEditing(): void;
  cancelEditing(): void;
  save(): void;
  decideLeave(leave: boolean): void;
  openNewVersion(): void;
  saveVersion(): void;
  versionPage(event: { first?: number; rows?: number; sortField?: string; sortOrder?: number }): void;
  versionAction(event: { action: DocumentVersionAction; params: ApplicationDocumentVersion }): void;
  selectVersionFile(event: Event): void;
}

describe('ApplicationDocumentDetail', () => {
  let fixture: ComponentFixture<ApplicationDocumentDetail>;
  let page: PageInternals;
  const breadcrumbs = { setCustomBreadcrumbs: vi.fn(), clear: vi.fn() };
  const documents = {
    updateDocument: vi.fn(),
    addDocumentVersion: vi.fn(),
    getDocumentVersionFile: vi.fn(),
  };
  const load = vi.fn();
  const getMimeTypes = vi.fn();

  function detailData(canManage = true): ApplicationDocumentDetailData {
    return {
      application: { id: 1, code: 'INV', name: 'INVAI', canManage },
      document: documentDetail(),
      categories: [
        { id: 1, label: 'Tècnica', subcategories: [{ id: 11, label: 'Manuals' }] },
        { id: 2, label: 'Normativa', subcategories: [] },
      ],
      statuses: DOCUMENT_STATUSES,
    };
  }

  async function create(readOnly = false, missing = false) {
    documents.updateDocument.mockReset().mockImplementation((id, input) =>
      of(documentDetail({ title: input.title })),
    );
    documents.addDocumentVersion.mockReset();
    documents.getDocumentVersionFile.mockReset();
    load.mockReset();
    const data: Record<string, unknown> = {
      [DOCUMENT_DETAIL_RESOLVE_KEY]: { applicationId: 1, documentId: 1, detail: missing ? null : detailData(!readOnly) },
    };
    TestBed.configureTestingModule({
      imports: [ApplicationDocumentDetail],
      providers: [
        provideRouter([]), MessageService, { provide: BreadcrumbService, useValue: breadcrumbs },
        { provide: ActivatedRoute, useValue: { data: of(data), snapshot: { data } } },
        { provide: ApplicationDocumentationService, useValue: documents },
        { provide: ApplicationDocumentDetailLoader, useValue: { load } },
        { provide: ApplicationDocumentMimeTypeCatalogService, useValue: { getAll: getMimeTypes } },
      ],
    });
    fixture = TestBed.createComponent(ApplicationDocumentDetail);
    page = fixture.componentInstance as unknown as PageInternals;
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  beforeEach(() => getMimeTypes.mockReset().mockReturnValue(of(DOCUMENT_MIME_TYPES)));
  afterEach(() => fixture?.destroy());

  // A change event of the native picker with the given file chosen.
  function chosen(file: File): Event {
    return { target: { files: [file], value: file.name } } as unknown as Event;
  }

  it('consumes resolved data without repeating initial requests and builds the navigation context', async () => {
    await create();
    fixture.detectChanges();
    expect(load).not.toHaveBeenCalled();
    expect(page.form.getRawValue()).toEqual({ title: 'Manual API v2', categoryId: 1, subcategoryId: 11, statusId: 1 });
    expect(breadcrumbs.setCustomBreadcrumbs).toHaveBeenCalledWith(expect.arrayContaining([
      expect.objectContaining({ label: 'Manual API v2' }),
    ]));
    expect(fixture.nativeElement.querySelector('h1').textContent.trim()).toBe('INVAI');
  });

  it('validates the document, saves it with its section and most specific category, and clears dirty state', async () => {
    await create();
    page.startEditing();
    page.form.controls.title.setValue(' ');
    page.form.markAsDirty();
    page.save();
    expect(page.form.touched).toBe(true);
    expect(documents.updateDocument).not.toHaveBeenCalled();

    page.form.patchValue({ title: ' Manual renovat ', statusId: 2 });
    page.save();
    expect(documents.updateDocument).toHaveBeenCalledWith(1, { title: 'Manual renovat', sectionId: 1, categoryId: 11, statusId: 2 });
    expect(page.form.pristine).toBe(true);
    expect(page.isEditing()).toBe(false);
    expect(page.detail()?.document.title).toBe('Manual renovat');

    page.startEditing();
    page.form.patchValue({ categoryId: 2, subcategoryId: null });
    page.save();
    expect(documents.updateDocument).toHaveBeenLastCalledWith(1, expect.objectContaining({ categoryId: 2 }));
  });

  it('retains the edited form after a failed save', async () => {
    await create();
    page.startEditing();
    documents.updateDocument.mockReturnValue(throwError(() => new Error('failed')));
    page.form.controls.title.setValue('Unsaved');
    page.form.markAsDirty();
    page.save();
    expect(page.form.dirty).toBe(true);
    expect(page.form.controls.title.value).toBe('Unsaved');
    expect(page.isSaving()).toBe(false);
    expect(page.isEditing()).toBe(true);
  });

  it('guards both discard and stay decisions', async () => {
    await create();
    expect(fixture.componentInstance.canDeactivate()).toBe(true);
    page.startEditing();
    page.form.markAsDirty();
    const stay = firstValueFrom(fixture.componentInstance.canDeactivate() as Observable<boolean>);
    page.decideLeave(false);
    expect(await stay).toBe(false);
    const leave = firstValueFrom(fixture.componentInstance.canDeactivate() as Observable<boolean>);
    page.decideLeave(true);
    expect(await leave).toBe(true);
  });

  it('keeps read-only values perceivable and prevents mutation', async () => {
    await create(true);
    const root = fixture.nativeElement as HTMLElement;
    page.startEditing();
    page.openNewVersion();
    expect(page.isEditing()).toBe(false);
    expect(page.versionDialogVisible()).toBe(false);
    expect(root.querySelector<HTMLInputElement>('#document-detail-title')!.readOnly).toBe(true);
    expect(root.querySelectorAll('.document-detail-form p-select')).toHaveLength(0);
    expect(root.querySelector('#document-detail-category-value')!.textContent!.trim()).toBe('Tècnica');
    expect(root.querySelector('#document-detail-subcategory-value')!.textContent!.trim()).toBe('Manuals');
    expect(root.querySelector('#document-detail-state-value')!.textContent!.trim()).toBe('Vigent');
    page.save();
    expect(documents.updateDocument).not.toHaveBeenCalled();
  });

  it('opens in view mode, edits enabled controls and cancels back to the saved snapshot', async () => {
    await create();
    const root = fixture.nativeElement as HTMLElement;
    const title = () => root.querySelector<HTMLInputElement>('#document-detail-title')!;
    expect(title().readOnly).toBe(true);
    expect(page.form.enabled).toBe(true);
    page.startEditing();
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    expect(title().readOnly).toBe(false);
    expect(root.querySelectorAll('.document-detail-form p-select')).toHaveLength(3);
    expect(document.activeElement).toBe(title());
    page.form.patchValue({ title: 'Unsaved', categoryId: 2, subcategoryId: null, statusId: 3 });
    page.form.markAsDirty();
    page.cancelEditing();
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    expect(page.form.getRawValue()).toEqual({ title: 'Manual API v2', categoryId: 1, subcategoryId: 11, statusId: 1 });
    expect(page.form.pristine).toBe(true);
    expect(title().readOnly).toBe(true);
    expect(document.activeElement?.getAttribute('aria-label')).toBe('Editar Document');
    expect(documents.updateDocument).not.toHaveBeenCalled();
  });

  it('sorts and pages the versions of the document itself, the newest first', async () => {
    await create();
    expect(page.versions().items.map(({ versionLabel }) => versionLabel)).toEqual(['2.1', '2.0']);

    page.versionPage({ first: 0, rows: 10, sortField: 'fileSize', sortOrder: 1 });
    expect(page.versions().items.map(({ fileSize }) => fileSize)).toEqual([1_800_000, 2_300_000]);

    page.versionPage({ first: 1, rows: 1, sortField: 'versionLabel', sortOrder: -1 });
    expect(page.versions()).toEqual({ items: [expect.objectContaining({ versionLabel: '2.0' })], total: 2 });
  });

  it('adds a version with a new label and shows the versions the back-end returns', async () => {
    await create();
    const file = new File(['%PDF'], 'manual-v3.pdf');
    page.openNewVersion();
    page.versionForm.setValue({ version: '2.1', file });
    page.saveVersion();
    expect(page.versionForm.controls['version'].hasError('duplicate')).toBe(true);
    expect(documents.addDocumentVersion).not.toHaveBeenCalled();

    documents.addDocumentVersion.mockReturnValue(of(documentDetail({
      attachments: [documentVersion({ id: 21, versionLabel: '3.0' }), documentVersion({ current: false })],
    })));
    page.versionForm.setValue({ version: ' 3.0 ', file });
    page.saveVersion();

    expect(documents.addDocumentVersion).toHaveBeenCalledWith(1, { versionLabel: '3.0' }, file);
    expect(page.versionDialogVisible()).toBe(false);
    expect(page.versions().items.map(({ versionLabel }) => versionLabel)).toEqual(['3.0', '2.1']);
  });

  it('refuses a version file of a format not accepted, keeping the chosen one and saying why', async () => {
    await create();
    page.openNewVersion();
    fixture.detectChanges();
    const manual = new File(['%PDF'], 'manual-v3.pdf');
    page.selectVersionFile(chosen(manual));
    const refused = chosen(new File(['MZ'], 'programa.exe'));
    page.selectVersionFile(refused);
    fixture.detectChanges();

    expect(page.versionForm.controls['file'].value).toBe(manual);
    // The native picker never keeps showing the refused file.
    expect((refused.target as HTMLInputElement).value).toBe('');
    const input = document.getElementById('document-version-file')!;
    expect(input.getAttribute('accept')).toBe('.pdf,.docx');
    expect(input.getAttribute('aria-invalid')).toBe('true');
    expect(input.getAttribute('aria-describedby')).toBe('document-version-file-hint document-version-file-error');
    expect(document.getElementById('document-version-file-error')!.textContent).toContain(
      "No s'ha adjuntat «programa.exe»: el format del fitxer no està admès.",
    );
    expect(document.getElementById('document-version-file-hint')!.textContent).toContain('Mida màxima: 5 MB.');
  });

  it('does not let choose a version file without the accepted formats and reads them again', async () => {
    getMimeTypes.mockReturnValue(throwError(() => new Error('offline')));
    await create();
    page.openNewVersion();
    fixture.detectChanges();

    const input = document.getElementById('document-version-file') as HTMLInputElement;
    expect(input.disabled).toBe(true);
    expect(document.getElementById('document-version-file-hint')!.textContent).toContain(
      "No s'han pogut carregar els formats admesos: ara no es pot adjuntar cap fitxer.",
    );
    page.selectVersionFile(chosen(new File(['%PDF'], 'manual.pdf')));
    expect(page.versionForm.controls['file'].value).toBeNull();

    getMimeTypes.mockReturnValue(of(DOCUMENT_MIME_TYPES));
    page.openNewVersion();
    fixture.detectChanges();
    expect(input.disabled).toBe(false);
  });

  it('shows a version in a new tab', async () => {
    await create();
    const { createObjectURL, revokeObjectURL } = URL;
    URL.createObjectURL = vi.fn(() => 'blob:manual');
    URL.revokeObjectURL = vi.fn();
    const open = vi.spyOn(window, 'open').mockReturnValue(null);
    documents.getDocumentVersionFile.mockReturnValue(of({ blob: new Blob(['%PDF']), fileName: 'manual.pdf' }));
    try {
      page.versionAction({ action: DocumentVersionAction.View, params: documentVersion() });

      expect(documents.getDocumentVersionFile).toHaveBeenCalledWith(1, 20, 'manual-api-v2.pdf', true);
      expect(open).toHaveBeenCalledWith('blob:manual', '_blank', 'noopener,noreferrer');
      // The URL lives until the page goes away.
      fixture.destroy();
      expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:manual');
    } finally {
      open.mockRestore();
      Object.assign(URL, { createObjectURL, revokeObjectURL });
    }
  });

  it('renders an error and back action for a missing document', async () => {
    await create(false, true);
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain("No s'ha pogut carregar el document.");
    expect(fixture.nativeElement.querySelectorAll('button').length).toBeGreaterThan(0);
  });
});
