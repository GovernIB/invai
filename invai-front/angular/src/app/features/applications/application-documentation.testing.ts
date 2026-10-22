import { SpringPage } from '@models/page.model';

import {
  ApplicationAttachment,
  ApplicationDocument,
  ApplicationDocumentMimeType,
  ApplicationDocumentSection,
  ApplicationDocumentStatus,
  ApplicationDocumentVersion,
} from './application-documentation.model';

// Test fixtures shared by the application documentation specs, shaped as the back-end sends them.
export const DOCUMENT_SECTIONS: ApplicationDocumentSection[] = [
  { id: 1, code: 'GENERAL', name: 'General', nameEs: 'General' },
  { id: 3, code: 'SYSTEMS', name: 'Sistemes', nameEs: 'Sistemas' },
  { id: 4, code: 'DATABASE', name: 'Base de dades', nameEs: 'Base de datos' },
];

export const DOCUMENT_STATUSES: ApplicationDocumentStatus[] = [
  { id: 1, code: 'ACTIVE', name: 'Vigent', nameEs: 'Vigente' },
  { id: 2, code: 'IN_REVIEW', name: 'En revisió', nameEs: 'En revisión' },
  { id: 3, code: 'OBSOLETE', name: 'Obsolet', nameEs: 'Obsoleto' },
];

export const DOCUMENT_MIME_TYPES: ApplicationDocumentMimeType[] = [
  { id: 1, extension: 'pdf', mimeType: 'application/pdf', name: 'PDF' },
  { id: 3, extension: 'docx', mimeType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', name: 'Word' },
];

export function documentRow(overrides: Partial<ApplicationAttachment> = {}): ApplicationAttachment {
  return {
    id: 1,
    title: 'Manual API v2',
    section: DOCUMENT_SECTIONS[0],
    mimeType: { id: 1, name: 'PDF' },
    category: { id: 1, name: 'Tècnica', nameEs: 'Técnica', parentId: null },
    subcategory: { id: 11, name: 'Manuals', nameEs: 'Manuales', parentId: 1 },
    versionLabel: '2.1',
    currentAttachmentId: 20,
    status: DOCUMENT_STATUSES[0],
    deletedAt: null,
    ...overrides,
  };
}

export function documentVersion(overrides: Partial<ApplicationDocumentVersion> = {}): ApplicationDocumentVersion {
  return {
    id: 20,
    versionLabel: '2.1',
    fileName: 'manual-api-v2.pdf',
    mimeType: { id: 1, name: 'PDF' },
    fileSize: 2_300_000,
    uploadedAt: '2025-11-18T10:00:00',
    uploadedBy: 'jgarcia',
    current: true,
    changeNote: null,
    metadata: [],
    ...overrides,
  };
}

export function documentDetail(overrides: Partial<ApplicationDocument> = {}): ApplicationDocument {
  const row = documentRow();
  return {
    id: row.id,
    applicationId: 1,
    title: row.title,
    section: row.section,
    category: row.category,
    subcategory: row.subcategory,
    status: row.status,
    deletedAt: null,
    attachments: [
      documentVersion(),
      documentVersion({ id: 19, versionLabel: '2.0', fileSize: 1_800_000, uploadedAt: '2025-09-03T10:00:00', current: false }),
    ],
    ...overrides,
  };
}

export function documentationPage<T>(content: T[], overrides: Partial<SpringPage<T>> = {}): SpringPage<T> {
  return {
    content,
    empty: content.length === 0,
    first: true,
    last: true,
    number: 0,
    numberOfElements: content.length,
    pageable: {
      offset: 0,
      pageNumber: 0,
      pageSize: 10,
      paged: true,
      sort: { empty: true, sorted: false, unsorted: true },
      unpaged: false,
    },
    size: 10,
    sort: { empty: true, sorted: false, unsorted: true },
    totalElements: content.length,
    totalPages: 1,
    ...overrides,
  };
}
