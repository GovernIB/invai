import { HttpErrorResponse } from '@angular/common/http';
import { LOCALE_ID } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { DocumentCategoryCatalogService } from '@features/document-categories/services/document-category-catalog.service';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';

import { ApplicationAttachment } from '../../../../application-documentation.model';
import {
  DOCUMENT_MIME_TYPES,
  DOCUMENT_SECTIONS,
  DOCUMENT_STATUSES,
  documentationPage,
  documentRow,
} from '../../../../application-documentation.testing';
import {
  ApplicationAttachmentFiltersFormGroup,
  ApplicationAttachmentFormGroup,
} from '../../../../forms/application-documentation-form.factory';
import { ApplicationDocumentMimeTypeCatalogService } from '../../../../services/application-document-mime-type-catalog.service';
import { ApplicationDocumentSectionCatalogService } from '../../../../services/application-document-section-catalog.service';
import { ApplicationDocumentStatusCatalogService } from '../../../../services/application-document-status-catalog.service';
import { ApplicationDocumentationService } from '../../../../services/application-documentation.service';
import { ApplicationAttachmentsList } from './application-attachments-list';
import { ApplicationDocumentationAction } from './application-documentation-table-action';

const TREE = [
  { id: 1, label: 'Tècnica', subcategories: [{ id: 11, label: 'Manuals' }] },
  { id: 2, label: 'Normativa', subcategories: [] },
];

const ROWS = [
  documentRow(),
  documentRow({ id: 2, title: 'Plec de prescripcions tècniques', subcategory: null, currentAttachmentId: 30 }),
];

interface ListInternals {
  entityForm: ApplicationAttachmentFormGroup;
  filtersForm: ApplicationAttachmentFiltersFormGroup;
  mimeTypeOptions: () => { value: number; label: string }[];
  subcategoryFilterOptions: () => { id: number; label: string }[];
  isDialogVisible: () => boolean;
  isDeleteDialogVisible: () => boolean;
  categoryOptions: () => typeof TREE;
  sectionOptions: () => { value: number; label: string }[];
  stateOptions: () => { value: number; label: string }[];
  openCreateDialog(): void;
  onTableAction(event: { action: number; params: ApplicationAttachment }): void;
  onFilterSearch(): void;
  submit(): void;
  confirmDelete(): void;
  onExport(): void;
}

describe('ApplicationAttachmentsList', () => {
  let fixture: ComponentFixture<ApplicationAttachmentsList>;
  let list: ListInternals;
  let add: ReturnType<typeof vi.spyOn>;
  let navigate: ReturnType<typeof vi.spyOn>;
  const getActiveCategoryTree = vi.fn();
  const getSections = vi.fn();
  const documents = {
    getAttachments: vi.fn(),
    createDocument: vi.fn(),
    deleteDocument: vi.fn(),
    getDocumentVersionFile: vi.fn(),
  };

  async function create(canManage = true, withInitialPage = true): Promise<void> {
    fixture = TestBed.createComponent(ApplicationAttachmentsList);
    fixture.componentRef.setInput('applicationId', 1);
    fixture.componentRef.setInput('canManage', canManage);
    if (withInitialPage) fixture.componentRef.setInput('initialPage', documentationPage(ROWS));
    fixture.detectChanges();
    list = fixture.componentInstance as unknown as ListInternals;
  }

  const items = () => fixture.componentInstance.itemsList();
  const lastQuery = () => documents.getAttachments.mock.lastCall;
  const fillForm = (file = new File(['%PDF'], 'pla.pdf')) =>
    list.entityForm.setValue({
      title: ' Pla de proves ',
      sectionId: 3,
      file,
      categoryId: 1,
      subcategoryId: 11,
      versionLabel: ' 1.0 ',
    });

  beforeEach(async () => {
    getActiveCategoryTree.mockReset().mockReturnValue(of(TREE));
    getSections.mockReset().mockReturnValue(of(DOCUMENT_SECTIONS));
    documents.getAttachments.mockReset().mockReturnValue(of(documentationPage(ROWS)));
    documents.createDocument.mockReset().mockReturnValue(of({ id: 9 }));
    documents.deleteDocument.mockReset().mockReturnValue(of(undefined));
    documents.getDocumentVersionFile.mockReset();
    await TestBed.configureTestingModule({
      imports: [ApplicationAttachmentsList],
      providers: [
        MessageService,
        provideRouter([]),
        { provide: LOCALE_ID, useValue: 'ca' },
        { provide: ApplicationDocumentationService, useValue: documents },
        { provide: DocumentCategoryCatalogService, useValue: { getActiveCategoryTree } },
        { provide: ApplicationDocumentSectionCatalogService, useValue: { getAll: getSections } },
        { provide: ApplicationDocumentStatusCatalogService, useValue: { getAll: () => of(DOCUMENT_STATUSES) } },
        { provide: ApplicationDocumentMimeTypeCatalogService, useValue: { getAll: () => of(DOCUMENT_MIME_TYPES) } },
      ],
    })
      .overrideComponent(ApplicationAttachmentsList, { set: { template: '' } })
      .compileComponents();
    add = vi.spyOn(TestBed.inject(MessageService), 'add');
    navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
  });

  it('shows the resolved page without reading it again', async () => {
    await create();

    expect(documents.getAttachments).not.toHaveBeenCalled();
    expect([items().items, items().total]).toEqual([ROWS, 2]);
    // The state of the documents is shown by default, unlike the deactivation status of entities.
    expect(fixture.componentInstance.sortedSelectedColumns().map(({ key }) => key)).toContain('state');
  });

  it('reads the first page itself, leaving out the deleted documents, when the resolver could not', async () => {
    await create(true, false);

    expect(lastQuery()).toEqual([1, expect.objectContaining({ page: 0, deleted: false })]);
    expect(items().total).toBe(2);
  });

  it('offers the sections and states of the catalogs, sorted by their label', async () => {
    await create();

    expect(list.sectionOptions().map(({ label }) => label)).toEqual(['Base de dades', 'General', 'Sistemes']);
    expect(list.stateOptions()).toEqual([
      { value: 2, label: 'En revisió' },
      { value: 3, label: 'Obsolet' },
      { value: 1, label: 'Vigent' },
    ]);
  });

  it('filters by section and state and sorts by the keys of the back-end', async () => {
    await create();

    list.filtersForm.patchValue({ sectionId: 3, statusId: 2 });
    list.onFilterSearch();
    expect(lastQuery()?.[1]).toMatchObject({ sectionId: 3, statusId: 2, deleted: false });

    fixture.componentInstance.onSearch({ first: 10, rows: 10, sortField: 'versionLabel', sortOrder: -1 });
    expect(lastQuery()?.[1]).toMatchObject({ page: 1, sort: 'versionLabel,desc', deleted: false });
  });

  it('offers each file format with its extension, as several formats share a name', async () => {
    await create();

    expect(list.mimeTypeOptions()).toEqual([
      { value: 1, label: 'PDF (.pdf)' },
      { value: 3, label: 'Word (.docx)' },
    ]);
  });

  it('reads the categories the first time the filters are shown', async () => {
    await create();
    expect(getActiveCategoryTree).not.toHaveBeenCalled();

    fixture.componentInstance.isFiltersCollapsed.set(false);
    fixture.detectChanges();

    expect(getActiveCategoryTree).toHaveBeenCalledTimes(1);
  });

  it('filters by format and by the most specific category chosen', async () => {
    await create();
    fixture.componentInstance.isFiltersCollapsed.set(false);
    fixture.detectChanges();
    const { categoryId, subcategoryId } = list.filtersForm.controls;

    categoryId.setValue(1);
    expect(subcategoryId.enabled).toBe(true);
    expect(list.subcategoryFilterOptions()).toEqual([{ id: 11, label: 'Manuals' }]);
    list.filtersForm.patchValue({ mimeTypeId: 3, subcategoryId: 11 });
    list.onFilterSearch();
    expect(lastQuery()?.[1]).toMatchObject({ mimeTypeId: 3, categoryId: 11, deleted: false });
    expect(fixture.componentInstance.selectedFilters()).toBe(3);

    // A category without subcategories discards the one chosen and leaves none to choose.
    categoryId.setValue(2);
    expect([subcategoryId.value, subcategoryId.disabled]).toEqual([null, true]);
    list.onFilterSearch();
    expect(lastQuery()?.[1]).toMatchObject({ categoryId: 2 });

    fixture.componentInstance.reset();
    expect(lastQuery()?.[1]).not.toHaveProperty('categoryId', expect.anything());
    expect(lastQuery()?.[1]).not.toHaveProperty('mimeTypeId', expect.anything());
    expect(subcategoryId.disabled).toBe(true);
  });

  it('creates an active document in the most specific category with its file', async () => {
    await create();
    list.openCreateDialog();
    expect(list.categoryOptions()).toEqual(TREE);

    list.submit();
    expect(list.entityForm.touched).toBe(true);
    expect(documents.createDocument).not.toHaveBeenCalled();

    const file = new File(['%PDF'], 'pla.pdf');
    fillForm(file);
    list.submit();

    expect(documents.createDocument).toHaveBeenCalledWith(
      { applicationId: 1, title: 'Pla de proves', sectionId: 3, categoryId: 11, statusId: 1, versionLabel: '1.0' },
      file,
    );
    expect(list.isDialogVisible()).toBe(false);
    expect(add).toHaveBeenCalledWith(expect.objectContaining({ severity: 'success', summary: 'Adjunt afegit' }));
    expect(documents.getAttachments).toHaveBeenCalledTimes(1);
  });

  it('does not send a file whose format the back-end does not accept', async () => {
    await create();
    list.openCreateDialog();

    fillForm(new File(['MZ'], 'programa.exe'));
    list.submit();

    expect(list.entityForm.controls.file.hasError('fileType')).toBe(true);
    expect(documents.createDocument).not.toHaveBeenCalled();
  });

  it('leaves the back-end message to the error dialog and reports other failures itself', async () => {
    await create();
    list.openCreateDialog();
    fillForm();
    documents.createDocument.mockReturnValueOnce(
      throwError(() => new HttpErrorResponse({ status: 400, error: { error: 'Bad Request', message: 'Versió duplicada' } })),
    );
    list.submit();
    expect(add).not.toHaveBeenCalled();
    expect(list.isDialogVisible()).toBe(true);

    documents.createDocument.mockReturnValueOnce(throwError(() => new HttpErrorResponse({ status: 500 })));
    list.submit();
    expect(add).toHaveBeenCalledWith(expect.objectContaining({ detail: "No s'ha pogut desar l'adjunt." }));
  });

  it('reads the catalogs again when the dialog opens after they failed', async () => {
    getSections.mockReturnValueOnce(throwError(() => new Error('offline')));
    await create();
    expect(add).toHaveBeenCalledWith(
      expect.objectContaining({ detail: "No s'han pogut carregar les seccions, els estats i els formats dels documents." }),
    );
    expect(list.sectionOptions()).toEqual([]);

    list.openCreateDialog();

    expect(list.sectionOptions()).toHaveLength(3);
  });

  it('navigates to the document page to consult it', async () => {
    await create();

    list.onTableAction({ action: ApplicationDocumentationAction.View, params: ROWS[0] });

    expect(navigate).toHaveBeenCalledWith(['/', 'aplicacions', 1, 'documentation', 1]);
    expect(getActiveCategoryTree).not.toHaveBeenCalled();
  });

  it('downloads the current version of a document', async () => {
    const { createObjectURL, revokeObjectURL } = URL;
    URL.createObjectURL = vi.fn(() => 'blob:manual');
    URL.revokeObjectURL = vi.fn();
    const click = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined);
    documents.getDocumentVersionFile.mockReturnValue(of({ blob: new Blob(['%PDF']), fileName: 'manual.pdf' }));
    try {
      await create();

      list.onTableAction({ action: ApplicationDocumentationAction.Download, params: ROWS[0] });

      expect(documents.getDocumentVersionFile).toHaveBeenCalledWith(1, 20, 'Manual API v2');
      expect(click).toHaveBeenCalledTimes(1);
      expect((click.mock.contexts[0] as HTMLAnchorElement).download).toBe('manual.pdf');
    } finally {
      click.mockRestore();
      Object.assign(URL, { createObjectURL, revokeObjectURL });
    }
  });

  it('reports a failed download and that exporting is not available yet', async () => {
    documents.getDocumentVersionFile.mockReturnValue(throwError(() => new Error('offline')));
    await create();

    list.onTableAction({ action: ApplicationDocumentationAction.Download, params: ROWS[0] });
    list.onExport();

    expect((add.mock.calls as [{ detail?: string }][]).map(([message]) => message.detail)).toEqual([
      "No s'ha pogut descarregar el fitxer.",
      "L'exportació encara no està implementada.",
    ]);
  });

  it('deletes a document after confirming it', async () => {
    await create();

    list.onTableAction({ action: ApplicationDocumentationAction.Delete, params: ROWS[0] });
    expect(list.isDeleteDialogVisible()).toBe(true);
    list.confirmDelete();

    expect(documents.deleteDocument).toHaveBeenCalledWith(1);
    expect(add).toHaveBeenCalledWith(expect.objectContaining({ summary: 'Adjunt eliminat' }));
    expect(documents.getAttachments).toHaveBeenCalledTimes(1);
  });

  it('does not add or delete without permission to manage the application', async () => {
    await create(false);

    list.openCreateDialog();
    list.onTableAction({ action: ApplicationDocumentationAction.Delete, params: ROWS[0] });

    expect(list.isDialogVisible()).toBe(false);
    expect(list.isDeleteDialogVisible()).toBe(false);
  });
});
