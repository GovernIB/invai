import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { DocumentCategoryCatalogService } from '@features/document-categories/services/document-category-catalog.service';
import { firstValueFrom, of } from 'rxjs';

import { ApplicationDocument } from '../../application-documentation.model';
import { ApplicationStatusCode } from '../../applications.model';
import { ApplicationDocumentStatusCatalogService } from '../../services/application-document-status-catalog.service';
import { ApplicationDocumentationService } from '../../services/application-documentation.service';
import { ApplicationsService } from '../../services/applications.service';
import { ApplicationDocumentDetailLoader } from './application-document-detail.loader';

const STATUSES = [{ id: 1, code: 'ACTIVE', name: 'Vigent', nameEs: 'Vigente' }];

describe('ApplicationDocumentDetailLoader', () => {
  let document: ApplicationDocument;
  let application: { code: string | null; name: string | null; status: ApplicationStatusCode | null };

  beforeEach(() => {
    document = {
      id: 9,
      applicationId: 7,
      title: 'Manual',
      section: { id: 1, code: 'GENERAL', name: 'General', nameEs: 'General' },
      category: { id: 1, name: 'Tècnica', nameEs: 'Técnica', parentId: null },
      subcategory: { id: 11, name: 'Manuals', nameEs: 'Manuales', parentId: 1 },
      status: STATUSES[0],
      deletedAt: null,
      attachments: [],
    };
    application = { code: '0007', name: 'INVAI', status: ApplicationStatusCode.ACTIVE };
    TestBed.configureTestingModule({
      providers: [
        { provide: LOCALE_ID, useValue: 'ca' },
        { provide: ApplicationDocumentationService, useValue: { getDocument: vi.fn(() => of(document)) } },
        { provide: ApplicationsService, useValue: { getById: vi.fn(() => of(application)) } },
        {
          provide: DocumentCategoryCatalogService,
          useValue: {
            getActiveCategoryTree: vi.fn(() =>
              of([{ id: 1, label: 'Tècnica', subcategories: [{ id: 12, label: 'API' }] }]),
            ),
          },
        },
        { provide: ApplicationDocumentStatusCatalogService, useValue: { getAll: vi.fn(() => of(STATUSES)) } },
      ],
    });
  });

  it('joins the document, its application, the categories and the states', async () => {
    const detail = await load();

    expect(detail.application).toEqual({ id: 7, code: '0007', name: 'INVAI', canManage: true });
    expect(detail.document).toBe(document);
    expect(detail.statuses).toBe(STATUSES);
    // The inactive subcategory of the document stays selectable.
    expect(detail.categories[0].subcategories).toEqual([
      { id: 12, label: 'API' },
      { id: 11, label: 'Manuals' },
    ]);
  });

  it('adds a deactivated category of the document to the options', async () => {
    document.category = { id: 5, name: 'Antiga', nameEs: 'Antigua', parentId: null };
    document.subcategory = null;

    expect((await load()).categories.map(({ label }) => label)).toEqual(['Tècnica', 'Antiga']);
  });

  it('only lets an active application change a document that is not deleted', async () => {
    application.status = ApplicationStatusCode.INACTIVE;
    expect((await load()).application.canManage).toBe(false);

    application.status = ApplicationStatusCode.ACTIVE;
    document.deletedAt = '2026-10-20T10:00:00';
    expect((await load()).application.canManage).toBe(false);
  });

  it('does not show a document of another application', async () => {
    document.applicationId = 8;
    await expect(load()).rejects.toThrow();
  });

  function load() {
    return firstValueFrom(TestBed.inject(ApplicationDocumentDetailLoader).load(7, 9));
  }
});
